package pipe.gui;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.xml.crypto.Data;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import dk.aau.cs.TCTL.visitors.CTLQueryVisitor;
import dk.aau.cs.debug.Logger;
import dk.aau.cs.gui.TabContent;
import dk.aau.cs.io.LoadedModel;
import dk.aau.cs.io.TapnXmlLoader;
import dk.aau.cs.io.TimedArcPetriNetNetworkWriter;
import dk.aau.cs.io.queries.XMLQueryLoader;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.Variable;
import dk.aau.cs.util.FormatException;
import dk.aau.cs.verification.*;
import dk.aau.cs.verification.VerifyTAPN.VerifyPNUnfoldOptions;
import pipe.dataLayer.DataLayer;
import pipe.dataLayer.TAPNQuery.SearchOption;
import dk.aau.cs.Messenger;
import dk.aau.cs.TCTL.visitors.RenameAllPlacesVisitor;
import dk.aau.cs.TCTL.visitors.RenameAllTransitionsVisitor;
import dk.aau.cs.approximation.ApproximationWorker;
import dk.aau.cs.approximation.OverApproximation;
import dk.aau.cs.approximation.UnderApproximation;
import dk.aau.cs.model.tapn.TAPNQuery;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.model.tapn.simulation.TAPNNetworkTrace;
import dk.aau.cs.model.tapn.simulation.TimedArcPetriNetTrace;
import dk.aau.cs.util.Tuple;
import dk.aau.cs.util.UnsupportedModelException;
import dk.aau.cs.verification.VerifyTAPN.ModelReduction;
import dk.aau.cs.verification.VerifyTAPN.VerifyPN;
import dk.aau.cs.verification.VerifyTAPN.VerifyPNOptions;
import pipe.dataLayer.Template;

public abstract class RunVerificationBase extends SwingWorker<VerificationResult<TAPNNetworkTrace>, Void> {

	protected ModelChecker modelChecker;
    protected ModelChecker unfoldingEngine;

	protected VerificationOptions options;
	protected TimedArcPetriNetNetwork model;
	protected DataLayer guiModel;
	protected TAPNQuery query;
	protected TAPNQuery clonedQuery;
	protected pipe.dataLayer.TAPNQuery dataLayerQuery;
    protected HashMap<TimedArcPetriNet, DataLayer> guiModels;
	protected String reducedNetFilePath;
	protected boolean reduceNetOnly;
	protected boolean reducedNetOpened = false;
	
	protected Messenger messenger;
    //if the unfolded net is too big, do not try to load it
    private final int maxNetSize = 4000;

	public RunVerificationBase(ModelChecker modelChecker, ModelChecker unfoldingEngine, Messenger messenger, HashMap<TimedArcPetriNet, DataLayer> guiModels,String reducedNetFilePath, boolean reduceNetOnly) {
		super();
		this.modelChecker = modelChecker;
		this.unfoldingEngine = unfoldingEngine;
		this.messenger = messenger;
		this.guiModels = guiModels;
		this.reducedNetFilePath = reducedNetFilePath;
		this.reduceNetOnly = reduceNetOnly;
	}

    public RunVerificationBase(ModelChecker modelChecker, ModelChecker unfoldingEngine, Messenger messenger, HashMap<TimedArcPetriNet, DataLayer> guiModels) {
        this(modelChecker, unfoldingEngine, messenger, guiModels, "",false);
    }

	
	public void execute(VerificationOptions options, TimedArcPetriNetNetwork model, TAPNQuery query, pipe.dataLayer.TAPNQuery dataLayerQuery) {
		this.model = model;
		this.options = options;
		this.query = query;
		this.dataLayerQuery = dataLayerQuery;
		execute();
	}

	@Override
	protected VerificationResult<TAPNNetworkTrace> doInBackground() throws Exception {
        TabContent.TAPNLens lens =  new TabContent.TAPNLens(!model.isUntimed(), false, model.isColored());
		ITAPNComposer composer = new TAPNComposer(messenger, guiModels, lens,  false, true);
		Tuple<TimedArcPetriNet, NameMapping> transformedModel = composer.transformModel(model);
		guiModel = composer.getGuiModel();
		if (options.enabledOverApproximation())
		{
			OverApproximation overaprx = new OverApproximation();
			overaprx.modifyTAPN(transformedModel.value1(), options.approximationDenominator());
		}
		else if (options.enabledUnderApproximation())
		{
			UnderApproximation underaprx = new UnderApproximation();
			underaprx.modifyTAPN(transformedModel.value1(), options.approximationDenominator());
		}

		clonedQuery = new TAPNQuery(query.getProperty().copy(), query.getExtraTokens());
		MapQueryToNewNames(clonedQuery, transformedModel.value2());
		
		if (dataLayerQuery != null){
			clonedQuery.setCategory(dataLayerQuery.getCategory()); // Used by the CTL engine
		}
		
		if(options.enabledStateequationsCheck()) {
			if ((query.queryType() == QueryType.EF || query.queryType() == QueryType.AG) && !query.hasDeadlock() &&
                !(options instanceof VerifyPNOptions)) {

                VerifyPN verifypn = new VerifyPN(new FileFinder(), new MessengerImpl());
				if (!verifypn.supportsModel(transformedModel.value1(), options)) {
					// Skip over-approximation if model is not supported.
					// Prevents verification from displaying error.
				}

				if(model.isColored() && (!model.isUntimed() | options.traceOption() != pipe.dataLayer.TAPNQuery.TraceOption.NONE)){
                    transformedModel = unfoldColoredNet((TAPNComposer) composer, transformedModel, clonedQuery);
                }else {
                    if (!verifypn.setup()) {
                        messenger.displayInfoMessage("Over-approximation check is skipped because VerifyPN is not available.", "VerifyPN unavailable");
                    } else {
                        VerificationResult<TimedArcPetriNetTrace> overapprox_result = null;
                        if (dataLayerQuery != null) {
                            overapprox_result = verifypn.verify(
                                new VerifyPNOptions(
                                    options.extraTokens(),
                                    options.traceOption(),
                                    SearchOption.OVERAPPROXIMATE,
                                    true,
                                    ModelReduction.AGGRESSIVE,
                                    options.enabledOverApproximation(),
                                    options.enabledUnderApproximation(),
                                    options.approximationDenominator(),
                                    dataLayerQuery.getCategory(),
                                    dataLayerQuery.getAlgorithmOption(),
                                    dataLayerQuery.isSiphontrapEnabled(),
                                    dataLayerQuery.isQueryReductionEnabled()? pipe.dataLayer.TAPNQuery.QueryReductionTime.UnlimitedTime: pipe.dataLayer.TAPNQuery.QueryReductionTime.NoTime,
                                    dataLayerQuery.isStubbornReductionEnabled(),
                                    model.isColored() && (!model.isUntimed() || options.traceOption() != pipe.dataLayer.TAPNQuery.TraceOption.NONE),
                                    reducedNetFilePath,
                                    dataLayerQuery.isTarOptionEnabled()
                                ),
                                transformedModel,
                                clonedQuery, composer.getGuiModel());
                        } else {
                            overapprox_result = verifypn.verify(
                                new VerifyPNOptions(
                                    options.extraTokens(),
                                    options.traceOption(),
                                    SearchOption.OVERAPPROXIMATE,
                                    true,
                                    ModelReduction.AGGRESSIVE,
                                    options.enabledOverApproximation(),
                                    options.enabledUnderApproximation(),
                                    options.approximationDenominator(),
                                    pipe.dataLayer.TAPNQuery.QueryCategory.Default,
                                    pipe.dataLayer.TAPNQuery.AlgorithmOption.CERTAIN_ZERO,
                                    false,
                                    dataLayerQuery.isQueryReductionEnabled()? pipe.dataLayer.TAPNQuery.QueryReductionTime.UnlimitedTime: pipe.dataLayer.TAPNQuery.QueryReductionTime.NoTime,
                                    false,
                                    model.isColored() && (!model.isUntimed() || options.traceOption() != pipe.dataLayer.TAPNQuery.TraceOption.NONE),
                                    reducedNetFilePath,
                                    dataLayerQuery.isTarOptionEnabled()
                                ),
                                transformedModel,
                                clonedQuery,
                                composer.getGuiModel());
                        }

                        if (overapprox_result.getQueryResult() != null) {
                            if (!overapprox_result.error() && model.isUntimed() || (
                                (query.queryType() == QueryType.EF && !overapprox_result.getQueryResult().isQuerySatisfied()) ||
                                    (query.queryType() == QueryType.AG && overapprox_result.getQueryResult().isQuerySatisfied()))
                            ) {
                                VerificationResult<TAPNNetworkTrace> value = new VerificationResult<TAPNNetworkTrace>(
                                    overapprox_result.getQueryResult(),
                                    decomposeTrace(overapprox_result.getTrace(), transformedModel.value2()),
                                    overapprox_result.verificationTime(),
                                    overapprox_result.stats(),
                                    true
                                );
                                value.setNameMapping(transformedModel.value2());
                                return value;
                            }
                        }
                    }
                }
			}
		} else if(model.isColored() && (!model.isUntimed() | options.traceOption() != pipe.dataLayer.TAPNQuery.TraceOption.NONE)){
            transformedModel = unfoldColoredNet((TAPNComposer) composer, transformedModel, clonedQuery);
        }
		ApproximationWorker worker = new ApproximationWorker();

		return worker.normalWorker(options, modelChecker, transformedModel, composer, clonedQuery, this, model, guiModel);
	}

    private Tuple<TimedArcPetriNet, NameMapping> unfoldColoredNet(TAPNComposer composer, Tuple<TimedArcPetriNet, NameMapping> transformedModel, TAPNQuery clonedQuery) {
        TimedArcPetriNetNetwork network = new TimedArcPetriNetNetwork();
        ArrayList<Template> templates = new ArrayList<Template>(1);
        ArrayList<pipe.dataLayer.TAPNQuery> queries = new ArrayList<pipe.dataLayer.TAPNQuery>(1);

        network.add(transformedModel.value1());
        for (ColorType ct :model.colorTypes()) {
            if (!network.isNameUsedForColorType(ct.getName()))
                network.add(ct);
        }
        for (Variable variable: model.variables()) {
            if (!network.isNameUsedForVariable(variable.getName()))
                network.add(variable);
        }
        templates.add(new Template(transformedModel.value1(), composer.getGuiModel(), new Zoomer()));

        TimedArcPetriNetNetworkWriter writerTACPN = new TimedArcPetriNetNetworkWriter(network, templates, queries, model.constants());
        File modelOut = null;
        File queryOut = null;
        File modelFile = null;
        File queryFile = null;
        try{
            modelOut = File.createTempFile("modelOut", ".tapn");
            queryOut = File.createTempFile("queryOut", ".xml");
            modelFile = File.createTempFile("modelIn", ".tapn");
            queryFile = File.createTempFile("queryIn", ".xml");
            writerTACPN.savePNML(modelFile);
        } catch (IOException e){
            e.printStackTrace();
        } catch (ParserConfigurationException e){
            e.printStackTrace();
        } catch (TransformerException e){
            e.printStackTrace();
        }

        try{
            PrintStream queryStream = new PrintStream(queryFile);
            CTLQueryVisitor XMLVisitor = new CTLQueryVisitor();
            queryStream.append(XMLVisitor.getXMLQueryFor(clonedQuery.getProperty(), null));
            queryStream.close();
        } catch(FileNotFoundException e) {
            System.err.append("An error occurred while exporting the model to verifytapn. Verification cancelled.");
            return null;
        }



        VerificationOptions unfoldTACPNOptions = new VerifyPNUnfoldOptions(modelOut.getAbsolutePath(), queryOut.getAbsolutePath(), "ff", SearchOption.OVERAPPROXIMATE, false, false, 1);
        ProcessRunner runner = new ProcessRunner(unfoldingEngine.getPath(), createUnfoldArgumentString(modelFile.getAbsolutePath(), queryFile.getAbsolutePath(), unfoldTACPNOptions));
        runner.run();
        String errorOutput = readOutput(runner.errorOutput());
        int netSize = readUnfoldedSize(runner.standardOutput());
        Logger.log(errorOutput);
        Logger.log(netSize);

        transformedModel = getUnfoldedModel(modelOut, queryOut, netSize);

        return transformedModel;
    }

    private TAPNNetworkTrace decomposeTrace(TimedArcPetriNetTrace trace, NameMapping mapping) {
		if (trace == null) {
			return null;
		}

		TAPNTraceDecomposer decomposer = new TAPNTraceDecomposer(trace, model, mapping);
		return decomposer.decompose();
	}

	private Tuple<TimedArcPetriNet, NameMapping> getUnfoldedModel(File modelFile, File queryFile, int netSize){
        TapnXmlLoader tapnLoader = new TapnXmlLoader();
        File fileOut = new File(modelFile.getAbsolutePath());
        LoadedModel loadedModel = null;
        TabContent oldTab = CreateGui.getCurrentTab();

        try {
            loadedModel = tapnLoader.load(fileOut);
            pipe.dataLayer.TAPNQuery unfoldedQuery = getQuery(queryFile, loadedModel.network());
            clonedQuery = new TAPNQuery(unfoldedQuery.getProperty().copy(), clonedQuery.getExtraTokens());
            model = loadedModel.network();

            TabContent newTab = new TabContent(loadedModel.network(), loadedModel.templates(),loadedModel.queries(),new TabContent.TAPNLens(oldTab.getLens().isTimed(), oldTab.getLens().isGame(), false));
            newTab.setInitialName(oldTab.getTabTitle().replace(".tapn", "") + "-unfolded");
            newTab.addQuery(unfoldedQuery);
            guiModel = newTab.getModel();

            if(options.traceOption() != pipe.dataLayer.TAPNQuery.TraceOption.NONE && netSize < maxNetSize) {
                Thread thread = new Thread(){
                    public void run(){
                        CreateGui.getApp().guiFrameController.ifPresent(o -> o.openTab(newTab));
                    }
                };
                thread.start();
                while(thread.isAlive()){
                    if(isCancelled()){
                        thread.stop();
                    }
                }
            } else if (netSize >= maxNetSize){
                options.setTraceOption(pipe.dataLayer.TAPNQuery.TraceOption.NONE);
                JOptionPane.showMessageDialog(CreateGui.getApp(), "Cannot show trace, as the unfolded net is too large to be loaded");
            }
        } catch (FormatException e) {
            e.printStackTrace();
        } catch (ThreadDeath d){
            return null;
        }

        TAPNComposer composer = new TAPNComposer(new MessengerImpl(), true);
        Tuple<TimedArcPetriNet, NameMapping> transformedModel = composer.transformModel(loadedModel.network());
        return transformedModel;
    }

    private static pipe.dataLayer.TAPNQuery getQuery(File queryFile, TimedArcPetriNetNetwork network) {
        XMLQueryLoader queryLoader = new XMLQueryLoader(queryFile, network);
        List<pipe.dataLayer.TAPNQuery> queries = new ArrayList<pipe.dataLayer.TAPNQuery>();
        queries.addAll(queryLoader.parseQueries().getQueries());
        return queries.get(0);
    }


	private void MapQueryToNewNames(TAPNQuery query, NameMapping mapping) {
		RenameAllPlacesVisitor placeVisitor = new RenameAllPlacesVisitor(mapping);
		RenameAllTransitionsVisitor transitionVisitor = new RenameAllTransitionsVisitor(mapping);
		query.getProperty().accept(placeVisitor, null);
		query.getProperty().accept(transitionVisitor, null);
	}

	@Override
	protected void done() {
		if (!isCancelled()) {
			VerificationResult<TAPNNetworkTrace> result = null;

			try {
				result = get();
			} catch (InterruptedException e) {
				e.printStackTrace();
				showErrorMessage(e.getMessage());
				return;
			} catch (ExecutionException e) {
				if(!(e.getCause() instanceof UnsupportedModelException)){
					e.printStackTrace();
				}
				showErrorMessage(e.getMessage());
				return;
			}
			firePropertyChange("state", StateValue.PENDING, StateValue.DONE);
			showResult(result);

		} else {
		    unfoldingEngine.kill();
			modelChecker.kill();
			messenger.displayInfoMessage("Verification was interrupted by the user. No result found!", "Verification Cancelled");

		}
	}
    private String createUnfoldArgumentString(String modelFile, String queryFile, VerificationOptions options) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(modelFile);
        buffer.append(" ");
        buffer.append(queryFile);
        buffer.append(" ");
        buffer.append(options.toString());
        return buffer.toString();
    }
    @SuppressWarnings("Duplicates")
    private String readOutput(BufferedReader reader) {
        try {
            if (!reader.ready())
                return "";
        } catch (IOException e1) {
            return "";
        }
        StringBuffer buffer = new StringBuffer();
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
                buffer.append(System.getProperty("line.separator"));
            }
        } catch (IOException e) {
        }

        return buffer.toString();
    }

    private int readUnfoldedSize(BufferedReader reader){
        try {
            if (!reader.ready())
                return 0;
        } catch (IOException e1) {
            return 0;
        }
        int numElements = 0;
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                if(line.startsWith("Size of unfolded net: ")){
                    Pattern p = Pattern.compile("\\d+");
                    Matcher m = p.matcher(line);
                    while (m.find()) {
                        numElements += Integer.parseInt(m.group());
                    }
                }
            }
        } catch (IOException e) {
        }

        return numElements;
    }

	private String error;
	private void showErrorMessage(String errorMessage) {
		error = errorMessage;
		//The invoke later will make sure all the verification is finished before showing the error
		SwingUtilities.invokeLater(() -> {
			messenger.displayErrorMessage("The engine selected in the query dialog cannot verify this model.\nPlease choose another engine.\n" + error);
			CreateGui.getCurrentTab().editSelectedQuery();
		});
	}

	protected abstract void showResult(VerificationResult<TAPNNetworkTrace> result);
}
