package pipe.gui;

import dk.aau.cs.Messenger;
import dk.aau.cs.TCTL.TCTLAtomicPropositionNode;
import dk.aau.cs.TCTL.TCTLConstNode;
import dk.aau.cs.TCTL.TCTLEFNode;
import dk.aau.cs.TCTL.TCTLPlaceNode;
import dk.aau.cs.TCTL.visitors.CTLQueryVisitor;
import dk.aau.cs.gui.TabContent;
import dk.aau.cs.gui.smartDraw.SmartDrawDialog;
import dk.aau.cs.io.*;
import dk.aau.cs.io.queries.XMLQueryLoader;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.Variable;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.model.tapn.simulation.TAPNNetworkTrace;
import dk.aau.cs.util.FormatException;
import dk.aau.cs.util.Tuple;
import dk.aau.cs.util.UnsupportedModelException;
import dk.aau.cs.verification.*;
import dk.aau.cs.verification.VerifyTAPN.VerifyDTAPNUnfoldOptions;
import dk.aau.cs.verification.VerifyTAPN.VerifyPNUnfoldOptions;
import pipe.dataLayer.DataLayer;
import pipe.dataLayer.TAPNQuery;
import pipe.dataLayer.Template;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dk.aau.cs.gui.TabTransformer.createUnfoldArgumentString;
import static dk.aau.cs.gui.TabTransformer.mapQueryToNewNames;

public class UnfoldNet extends SwingWorker<String, Void> {

    protected ModelChecker modelChecker;
    protected HashMap<TimedArcPetriNet, DataLayer> guiModels;
    protected Messenger messenger;
    protected TimedArcPetriNetNetwork model;
    protected Iterable<TAPNQuery> queries;
    protected TabContent oldTab;
    protected boolean partition;
    protected boolean computeColorFixpoint;
    protected boolean symmetricVars;

    //if the unfolded net is too big, do not try to load it
    private final int maxNetSize = 4000;

    public UnfoldNet(ModelChecker modelChecker, Messenger messenger, HashMap<TimedArcPetriNet, DataLayer> guiModels, boolean partition, boolean computeColorFixpoint, boolean useSymmetricVars) {
        super();
        this.modelChecker = modelChecker;
        this.messenger = messenger;
        this.guiModels = guiModels;
        this.partition = partition;
        this.computeColorFixpoint = computeColorFixpoint;
        symmetricVars = useSymmetricVars;
    }

    public void execute(TimedArcPetriNetNetwork model, Iterable<TAPNQuery> queries, TabContent oldTab) {
        this.model = model;
        this.queries = queries;
        this.oldTab = oldTab;
        execute();
    }

    @Override
    protected String doInBackground() throws Exception {
        TabContent.TAPNLens lens =  new TabContent.TAPNLens(!model.isUntimed(), false, model.isColored());
        TAPNComposer composer = new TAPNComposer(new MessengerImpl(), guiModels, lens, true, true);
        Tuple<TimedArcPetriNet, NameMapping> transformedModel = composer.transformModel(model);
        boolean dummyQuery = false;
        StringBuilder error = new StringBuilder();

        File modelFile = null;
        File queryFile = null;
        File modelOut = null;
        File queryOut = null;
        try {
            modelFile = lens.isTimed()? File.createTempFile("modelInUnfold", ".xml"): File.createTempFile("modelInUnfold", ".tapn");
            queryFile = lens.isTimed()? File.createTempFile("queryInUnfold", ".q"): File.createTempFile("queryInUnfold", ".xml");
            modelOut = File.createTempFile("modelOut", ".xml");
            queryOut = File.createTempFile("queryOut", ".xml");
        } catch (IOException e) {
            e.printStackTrace();
            error.append(e.getMessage());
            return error.toString();
        }
        try {
            TimedArcPetriNetNetwork network = new TimedArcPetriNetNetwork();
            ArrayList<Template> templates = new ArrayList<>(1);
            ArrayList<pipe.dataLayer.TAPNQuery> queries = new ArrayList<>(1);


            network.add(transformedModel.value1());
            for (ColorType ct : model.colorTypes()) {
                network.add(ct);
            }
            for (Variable variable: model.variables()) {
                network.add(variable);
            }
            templates.add(new Template(transformedModel.value1(), composer.getGuiModel(), new Zoomer()));
            if(lens.isTimed()){
                TimedArcPetriNetNetworkWriter writerTACPN = new TimedArcPetriNetNetworkWriter(network, templates, queries, model.constants());
                writerTACPN.savePNML(modelFile);
            } else{
                var guiModels = new HashMap<TimedArcPetriNet, DataLayer>();
                guiModels.put(transformedModel.value1(),composer.getGuiModel());
                PNMLWriter writerTACPN = new PNMLWriter(network,guiModels, lens);
                writerTACPN.savePNML(modelFile);
            }



        } catch (IOException | ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
            error.append(e.getMessage());
            return error.toString();
        }
        List<TAPNQuery> clonedQueries = new Vector<>();
        if (queries.iterator().hasNext()) {
            for (pipe.dataLayer.TAPNQuery query : queries) {
                pipe.dataLayer.TAPNQuery clonedQuery = query.copy();
                mapQueryToNewNames(clonedQuery, transformedModel.value2());
                clonedQueries.add(clonedQuery);
            }
        }
        else {
            String templateName = model.activeTemplates().get(0).name();
            String placeName = model.activeTemplates().get(0).places().get(0).name();
            TCTLAtomicPropositionNode atomicStartNode = new TCTLAtomicPropositionNode(new TCTLPlaceNode(templateName, placeName), ">=", new TCTLConstNode(1));
            TCTLEFNode efNode = new TCTLEFNode(atomicStartNode);
            pipe.dataLayer.TAPNQuery test = new pipe.dataLayer.TAPNQuery("placeholder", 1000, efNode, null, null, null, false, false, false, false, null, null, lens.isColored());
            mapQueryToNewNames(test, transformedModel.value2());
            clonedQueries.add(test);
            dummyQuery = true;
        }

        ProcessRunner runner;
        try{
            PrintStream queryStream = new PrintStream(queryFile);
            CTLQueryVisitor XMLVisitor = new CTLQueryVisitor();
            String formattedQueries = "";
            for(pipe.dataLayer.TAPNQuery query : clonedQueries){
                if (query.getCategory() == TAPNQuery.QueryCategory.CTL || !lens.isTimed()) {
                    formattedQueries = XMLVisitor.getXMLQueryFor(query.getProperty(), query.getName());
                } else if (lens.isGame()) {
                    queryStream.append("control: ").append(query.getProperty().toString());
                } else {
                    queryStream.append(query.getProperty().toString());
                }
            }
            queryStream.append(formattedQueries);
            queryStream.close();
        } catch(FileNotFoundException e) {
            System.err.append("An error occurred while exporting the model to verifytapn. Verification cancelled.");
            error.append("An error occurred while exporting the model to verifytapn. Verification cancelled.");
            error.append(e.getMessage());
            return error.toString();
        }
        VerificationOptions unfoldTACPNOptions;
        if(lens.isTimed()){
            unfoldTACPNOptions = new VerifyDTAPNUnfoldOptions(modelOut.getAbsolutePath(), queryOut.getAbsolutePath(), model.marking().size()*2);
        } else{
            unfoldTACPNOptions = new VerifyPNUnfoldOptions(modelOut.getAbsolutePath(), queryOut.getAbsolutePath(), clonedQueries.size(), partition, computeColorFixpoint, symmetricVars);
        }


        runner = new ProcessRunner(modelChecker.getPath(), createUnfoldArgumentString(modelFile.getAbsolutePath(), queryFile.getAbsolutePath(), unfoldTACPNOptions));
        runner.run();

        //String errorOutput = readOutput(runner.errorOutput());
        int netSize = readUnfoldedSize(runner.standardOutput());

        if(netSize > maxNetSize){
            //We make a thread so the workers doesn't cancel itself before showing the dialog
            new Thread(() -> JOptionPane.showMessageDialog(CreateGui.getApp(), "The unfolded net is too large to be loaded")).start();
            cancel(true);
            return null;
        }

        File fileOut = new File(modelOut.getAbsolutePath());
        TabContent newTab;
        LoadedModel loadedModel = null;
        try {
            if(lens.isTimed()){
                loadedModel = new TapnXmlLoader().load(fileOut);
            } else{
                loadedModel = new PNMLoader().load(fileOut);
            }
            newTab = new TabContent(loadedModel.network(), loadedModel.templates(),loadedModel.queries(),new TabContent.TAPNLens(oldTab.getLens().isTimed(), oldTab.getLens().isGame(), false));
            newTab.setInitialName(oldTab.getTabTitle().replace(".tapn", "") + "-unfolded");
            if(!dummyQuery){
                for(pipe.dataLayer.TAPNQuery query : getQueries(queryOut, loadedModel.network())){
                    newTab.addQuery(query);
                }
            }

            Thread thread = new Thread(() -> CreateGui.getApp().guiFrameController.ifPresent(o -> o.openTab(newTab)));
            thread.start();
            while(thread.isAlive()){
                if(isCancelled()){
                    thread.stop();
                }
            }
        } catch (FormatException e) {
            e.printStackTrace();
            error.append(e.getMessage());
            return error.toString();
        } catch (ThreadDeath d){
            error.append(d.getMessage());
            return error.toString();
        }

        if(runner.error()){
            error.append(runner.errorOutput());
            return error.toString();
        }
        return null;
    }

    public static List<pipe.dataLayer.TAPNQuery> getQueries(File queryFile, TimedArcPetriNetNetwork network) {
        XMLQueryLoader queryLoader = new XMLQueryLoader(queryFile, network);
        return new ArrayList<>(queryLoader.parseQueries().getQueries());
    }

    private int readUnfoldedSize(BufferedReader reader){
        try {
            if (!reader.ready())
                return 0;
        } catch (IOException e1) {
            return 0;
        }
        int numElements = 0;
        String line;
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
            System.out.println("Got exception: " + e.getMessage());
        }

        return numElements;
    }

    @Override
    protected void done() {
        if (!isCancelled()) {
            String result = null;

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

            if(result != null){
                showErrorMessage(result);
            } else {
                firePropertyChange("state", StateValue.PENDING, StateValue.DONE);
            }

        } else {
            modelChecker.kill();
            messenger.displayInfoMessage("Unfolding was interrupted by the user", "Unfolding Cancelled");

        }
    }
    void showErrorMessage(String error){
        JOptionPane.showMessageDialog(CreateGui.getApp(), "The unfolding failed with error:\n" + error, "Unfolding Error", JOptionPane.ERROR_MESSAGE);
    }
}
