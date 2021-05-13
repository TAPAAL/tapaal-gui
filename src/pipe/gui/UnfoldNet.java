package pipe.gui;

import dk.aau.cs.Messenger;
import dk.aau.cs.TCTL.TCTLAtomicPropositionNode;
import dk.aau.cs.TCTL.TCTLConstNode;
import dk.aau.cs.TCTL.TCTLEFNode;
import dk.aau.cs.TCTL.TCTLPlaceNode;
import dk.aau.cs.TCTL.visitors.CTLQueryVisitor;
import dk.aau.cs.debug.Logger;
import dk.aau.cs.gui.TabContent;
import dk.aau.cs.io.*;
import dk.aau.cs.io.queries.XMLQueryLoader;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.Variable;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.util.FormatException;
import dk.aau.cs.util.Tuple;
import dk.aau.cs.verification.*;
import dk.aau.cs.verification.VerifyTAPN.VerifyCPNExporter;
import dk.aau.cs.verification.VerifyTAPN.VerifyPN;
import dk.aau.cs.verification.VerifyTAPN.VerifyPNUnfoldOptions;
import pipe.dataLayer.DataLayer;
import pipe.dataLayer.TAPNQuery;
import pipe.dataLayer.Template;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dk.aau.cs.gui.TabTransformer.createUnfoldArgumentString;
import static dk.aau.cs.gui.TabTransformer.mapQueryToNewNames;

public class UnfoldNet extends SwingWorker<Tuple<TimedArcPetriNet, NameMapping>, Void> {

    protected ModelChecker modelChecker;
    protected HashMap<TimedArcPetriNet, DataLayer> guiModels;
    protected Messenger messenger;
    protected TimedArcPetriNetNetwork model;
    protected Iterable<TAPNQuery> queries;
    protected TabContent oldTab;

    //if the unfolded net is too big, do not try to load it
    private final int maxNetSize = 4000;

    public UnfoldNet(ModelChecker modelChecker, Messenger messenger, HashMap<TimedArcPetriNet, DataLayer> guiModels) {
        super();
        this.modelChecker = modelChecker;
        this.messenger = messenger;
        this.guiModels = guiModels;
    }

    public void execute(TimedArcPetriNetNetwork model, Iterable<TAPNQuery> queries, TabContent oldTab) {
        this.model = model;
        this.queries = queries;
        this.oldTab = oldTab;
        execute();
    }

    @Override
    protected Tuple<TimedArcPetriNet, NameMapping> doInBackground() throws Exception {
        TabContent.TAPNLens lens =  new TabContent.TAPNLens(!model.isUntimed(), false, model.isColored());
        TAPNComposer composer = new TAPNComposer(new MessengerImpl(), guiModels, lens, true, true);
        Tuple<TimedArcPetriNet, NameMapping> transformedModel = composer.transformModel(model);
        boolean dummyQuery = false;

        File modelFile = null;
        File queryFile = null;
        File modelOut = null;
        File queryOut = null;
        try {
            modelFile = File.createTempFile("modelInUnfold", ".tapn");
            queryFile = File.createTempFile("queryInUnfold", ".xml");
            modelOut = File.createTempFile("modelOut", ".xml");
            queryOut = File.createTempFile("queryOut", ".xml");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            TimedArcPetriNetNetwork network = new TimedArcPetriNetNetwork();
            ArrayList<Template> templates = new ArrayList<Template>(1);
            ArrayList<pipe.dataLayer.TAPNQuery> queries = new ArrayList<pipe.dataLayer.TAPNQuery>(1);


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



        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        List<TAPNQuery> clonedQueries = new Vector<TAPNQuery>();
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
            pipe.dataLayer.TAPNQuery test = new pipe.dataLayer.TAPNQuery("placeholder", 1000, efNode, null, null, null, false, false, false, false, null, null);
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
                formattedQueries = XMLVisitor.getXMLQueryFor(query.getProperty(), query.getName());
            }
            queryStream.append(formattedQueries);
            queryStream.close();
        } catch(FileNotFoundException e) {
            System.err.append("An error occurred while exporting the model to verifytapn. Verification cancelled.");
            return null;
        }
        VerificationOptions unfoldTACPNOptions;
        if(lens.isTimed()){
            //TODO: implement timed options
            return null;
        } else{
            unfoldTACPNOptions = new VerifyPNUnfoldOptions(modelOut.getAbsolutePath(), queryOut.getAbsolutePath(), clonedQueries.size());
        }


        runner = new ProcessRunner(modelChecker.getPath(), createUnfoldArgumentString(modelFile.getAbsolutePath(), queryFile.getAbsolutePath(), unfoldTACPNOptions));
        runner.run();

        //String errorOutput = readOutput(runner.errorOutput());
        int netSize = readUnfoldedSize(runner.standardOutput());

        if(netSize > maxNetSize){
            //We make a thread so the workers doesn't cancel itself before showing the dialog
            new Thread(new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(CreateGui.getApp(), "The unfolded net is too large to be loaded");
                }
            }).start();
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
        } catch (FormatException e) {
            e.printStackTrace();
        } catch (ThreadDeath d){
            return null;
        }

        TAPNComposer newComposer = new TAPNComposer(new MessengerImpl(), true);
        return newComposer.transformModel(loadedModel.network());
    }

    private static List<pipe.dataLayer.TAPNQuery> getQueries(File queryFile, TimedArcPetriNetNetwork network) {
        XMLQueryLoader queryLoader = new XMLQueryLoader(queryFile, network);
        List<pipe.dataLayer.TAPNQuery> queries = new ArrayList<pipe.dataLayer.TAPNQuery>();
        queries.addAll(queryLoader.parseQueries().getQueries());
        return queries;
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

    @SuppressWarnings("Duplicates")
    public static String readOutput(BufferedReader reader) {
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
                if(line.startsWith("Size of unfolded net: ")){
                    LinkedList<String> numbers = new LinkedList<String>();

                    Pattern p = Pattern.compile("\\d+");
                    Matcher m = p.matcher(line);
                    while (m.find()) {
                        numbers.add(m.group());
                    }
                }
                buffer.append(line);
                buffer.append(System.getProperty("line.separator"));
            }
        } catch (IOException e) {
        }

        return buffer.toString();
    }
}
