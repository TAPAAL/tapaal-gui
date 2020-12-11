package pipe.gui;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import dk.aau.cs.debug.Logger;
import dk.aau.cs.gui.TabTransformer;
import dk.aau.cs.io.TimedArcPetriNetNetworkWriter;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.Variable;
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

	protected VerificationOptions options;
	protected TimedArcPetriNetNetwork model;
	protected TAPNQuery query;
	protected pipe.dataLayer.TAPNQuery dataLayerQuery;
    protected HashMap<TimedArcPetriNet, DataLayer> guiModels;
	
	
	protected Messenger messenger;

	public RunVerificationBase(ModelChecker modelChecker, Messenger messenger, HashMap<TimedArcPetriNet, DataLayer> guiModels) {
		super();
		this.modelChecker = modelChecker;
		this.messenger = messenger;
		this.guiModels = guiModels;
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
		ITAPNComposer composer = new TAPNComposer(messenger, guiModels, false, true);
		Tuple<TimedArcPetriNet, NameMapping> transformedModel = composer.transformModel(model);
        File modelOut = null;
        File queryOut = null;
        modelOut = File.createTempFile("modelOut", ".xml");
        queryOut = File.createTempFile("queryOut", ".q");

		//this is needed to get the declarations for colored nets
        transformedModel.value1().setParentNetwork(model);

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

		TAPNQuery clonedQuery = new TAPNQuery(query.getProperty().copy(), query.getExtraTokens());
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


				if(model.isColored()){
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
                    templates.add(new Template(transformedModel.value1(), ((TAPNComposer)composer).getGuiModel(), new Zoomer()));

                    TimedArcPetriNetNetworkWriter writerTACPN = new TimedArcPetriNetNetworkWriter(network, templates, queries, model.constants());

                    File modelFile = File.createTempFile("modelIn", ".tapn");
                    File queryFile = File.createTempFile("queryIn", ".q");
                    writerTACPN.savePNML(modelFile);
                    OutputStream os;
                    try {
                        os = new FileOutputStream(queryFile);
                        os.write(clonedQuery.toString().getBytes(), 0, clonedQuery.toString().length());
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    VerificationOptions unfoldTACPNOptions = new VerifyPNUnfoldOptions(modelOut.getAbsolutePath(), queryOut.getAbsolutePath(), "tt", true, true);
                    ProcessRunner runner = new ProcessRunner(TabTransformer.getunfoldPath(), createUnfoldArgumentString(modelFile.getAbsolutePath(), queryFile.getAbsolutePath(), unfoldTACPNOptions));
                    runner.run();
                    String errorOutput = readOutput(runner.errorOutput());
                    String standardOutput = readOutput(runner.standardOutput());
                    Logger.log(errorOutput);
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
                                    dataLayerQuery.isQueryReductionEnabled(),
                                    dataLayerQuery.isStubbornReductionEnabled()
                                ),
                                transformedModel,
                                clonedQuery
                            );
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
                                    true,
                                    false
                                ),
                                transformedModel,
                                clonedQuery
                            );
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
		} else if(model.isColored()){
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
            templates.add(new Template(transformedModel.value1(), ((TAPNComposer)composer).getGuiModel(), new Zoomer()));

            TimedArcPetriNetNetworkWriter writerTACPN = new TimedArcPetriNetNetworkWriter(network, templates, queries, model.constants());

            File modelFile = File.createTempFile("modelIn", ".tapn");
            File queryFile = File.createTempFile("queryIn", ".q");
            writerTACPN.savePNML(modelFile);
            OutputStream os;
            try {
                os = new FileOutputStream(queryFile);
                os.write(clonedQuery.toString().getBytes(), 0, clonedQuery.toString().length());
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            VerificationOptions unfoldTACPNOptions = new VerifyPNUnfoldOptions(modelOut.getAbsolutePath(), queryOut.getAbsolutePath(), "tt", true, true);
            ProcessRunner runner = new ProcessRunner(TabTransformer.getunfoldPath(), createUnfoldArgumentString(modelFile.getAbsolutePath(), queryFile.getAbsolutePath(), unfoldTACPNOptions));
            runner.run();
            String errorOutput = readOutput(runner.errorOutput());
            String standardOutput = readOutput(runner.standardOutput());
            Logger.log(errorOutput);
        }
		
		ApproximationWorker worker = new ApproximationWorker();

		return worker.normalWorker(options, modelChecker, transformedModel, composer, clonedQuery, this, model, modelOut.getAbsolutePath(), queryOut.getAbsolutePath());
	}

	private TAPNNetworkTrace decomposeTrace(TimedArcPetriNetTrace trace, NameMapping mapping) {
		if (trace == null) {
			return null;
		}

		TAPNTraceDecomposer decomposer = new TAPNTraceDecomposer(trace, model, mapping);
		return decomposer.decompose();
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
