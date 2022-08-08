package net.tapaal.gui.petrinet.verification;

import dk.aau.cs.Messenger;
import dk.aau.cs.TCTL.*;
import dk.aau.cs.TCTL.visitors.CTLQueryVisitor;
import net.tapaal.gui.petrinet.TAPNLens;
import pipe.gui.petrinet.PetriNetTab;
import dk.aau.cs.io.*;
import dk.aau.cs.io.queries.XMLQueryLoader;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.Variable;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.util.FormatException;
import dk.aau.cs.util.Tuple;
import dk.aau.cs.util.UnsupportedModelException;
import dk.aau.cs.verification.*;
import dk.aau.cs.verification.VerifyTAPN.VerifyDTAPNUnfoldOptions;
import dk.aau.cs.verification.VerifyTAPN.VerifyPNUnfoldOptions;
import pipe.gui.petrinet.dataLayer.DataLayer;
import net.tapaal.gui.petrinet.Template;
import pipe.gui.Constants;
import pipe.gui.MessengerImpl;
import pipe.gui.TAPAALGUI;
import pipe.gui.canvas.Zoomer;
import pipe.gui.petrinet.graphicElements.PetriNetObject;

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

import static net.tapaal.gui.petrinet.TabTransformer.createUnfoldArgumentString;
import static net.tapaal.gui.petrinet.TabTransformer.mapQueryToNewNames;

public class UnfoldNet extends SwingWorker<String, Void> {

    protected final ModelChecker modelChecker;
    protected final HashMap<TimedArcPetriNet, DataLayer> guiModels;
    protected final Messenger messenger;
    protected TimedArcPetriNetNetwork model;
    protected Iterable<TAPNQuery> queries;
    protected PetriNetTab oldTab;
    protected final boolean partition;
    protected final boolean computeColorFixpoint;
    protected final boolean symmetricVars;

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

    public void execute(TimedArcPetriNetNetwork model, Iterable<TAPNQuery> queries, PetriNetTab oldTab) {
        this.model = model;
        this.queries = queries;
        this.oldTab = oldTab;
        execute();
    }

    @Override
    protected String doInBackground() throws Exception {
        TAPNLens lens = oldTab.getLens();
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
            queryFile = File.createTempFile("queryInUnfold", ".xml");
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
            ArrayList<TAPNQuery> queries = new ArrayList<>(1);


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

        // This list is a workarround for issue #1968474
        // Should be removed when a better solution can be found when further refactoring is possible
        List<TAPNQuery.QueryCategory> queryCategories = new ArrayList<>();
        List<TAPNQuery> clonedQueries = new Vector<>();
        if (queries.iterator().hasNext()) {
            for (TAPNQuery query : queries) {
                TAPNQuery clonedQuery = query.copy();
                mapQueryToNewNames(clonedQuery, transformedModel.value2());
                clonedQueries.add(clonedQuery);
                queryCategories.add(query.getCategory());
            }
        }
        else {
            // XXX It seems this exists only to make sure that there is at least one query? -- kyrke 2022-05-08
            //  was firstTemplate,fistPlace=1 changed to just be E<>true, to fix issue #1971420
            TCTLEFNode efNode = new TCTLEFNode(new TCTLTrueNode());
            TAPNQuery test = new TAPNQuery("placeholder", 1000, efNode, null, null, null, false, false, false, false, null, null, lens.isColored());
            mapQueryToNewNames(test, transformedModel.value2());
            clonedQueries.add(test);
            dummyQuery = true;
        }

        ProcessRunner runner;
        try{
            PrintStream queryStream = new PrintStream(queryFile);
            CTLQueryVisitor XMLVisitor = new CTLQueryVisitor();
            String formattedQueries = "";
            for(TAPNQuery query : clonedQueries){
                formattedQueries = XMLVisitor.getXMLQueryFor(query.getProperty(), query.getName(), lens.isGame());
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
            unfoldTACPNOptions = new VerifyDTAPNUnfoldOptions(modelOut.getAbsolutePath(), queryOut.getAbsolutePath(), model.marking().size()*2, clonedQueries.size());
        } else{
            unfoldTACPNOptions = new VerifyPNUnfoldOptions(modelOut.getAbsolutePath(), queryOut.getAbsolutePath(), clonedQueries.size(), partition, computeColorFixpoint, symmetricVars);
        }


        runner = new ProcessRunner(modelChecker.getPath(), createUnfoldArgumentString(modelFile.getAbsolutePath(), queryFile.getAbsolutePath(), unfoldTACPNOptions));
        runner.run();

        //String errorOutput = readOutput(runner.errorOutput());
        int netSize = readUnfoldedSize(runner.standardOutput());

        if(netSize > maxNetSize){
            //We make a thread so the workers doesn't cancel itself before showing the dialog
            new Thread(() -> JOptionPane.showMessageDialog(TAPAALGUI.getApp(), "The unfolded net is too large to be loaded")).start();
            cancel(true);
            return null;
        }

        File fileOut = new File(modelOut.getAbsolutePath());
        PetriNetTab newTab;
        LoadedModel loadedModel = null;
        try {
            if(lens.isTimed()){
                loadedModel = new TapnEngineXmlLoader().load(fileOut);
            } else{
                loadedModel = new PNMLoader().load(fileOut);
            }
            // addLocation(loadedModel, composer); // We can not get coords from server
            newTab = new PetriNetTab(loadedModel.network(), loadedModel.templates(),loadedModel.queries(),new TAPNLens(oldTab.getLens().isTimed(), oldTab.getLens().isGame(), false));
            newTab.setInitialName(oldTab.getTabTitle().replace(".tapn", "") + "-unfolded");
            if(!dummyQuery){
                for(TAPNQuery query : getQueries(queryOut, loadedModel.network(), queryCategories)){
                    for(TAPNQuery oldQuery : queries){
                        if(query.getName().equals(oldQuery.getName())){
                            query.copyOptions(oldQuery);
                            newTab.addQuery(query);
                            break;
                        }
                    }
                }
            }

            Thread thread = new Thread(() -> TAPAALGUI.getAppGuiController().openTab(newTab));
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

    public static List<TAPNQuery> getQueries(File queryFile, TimedArcPetriNetNetwork network, TAPNQuery.QueryCategory queryCategory) {
        return getQueries(queryFile, network, List.of(queryCategory));
    }
    public static List<TAPNQuery> getQueries(File queryFile, TimedArcPetriNetNetwork network, List<TAPNQuery.QueryCategory> queryCategories) {
        XMLQueryLoader queryLoader = new XMLQueryLoader(queryFile, network, queryCategories);
        return new ArrayList<>(queryLoader.parseQueries().getQueries());
    }

    //XXX: old function to layout the model (before engine supported it)
    private void addLocation(LoadedModel loadedModel, TAPNComposer composer) {
        for (Template net : loadedModel.templates()) {
            int shifterX = calculateShift(net, true);
            int shifterY = calculateShift(net, false);

            if (shifterY > shifterX) shifterY = 0;
            else shifterX = 0;

            int counterX = shifterX;
            int counterY = shifterY;

            for (PetriNetObject object : net.guiModel().getPetriNetObjects()) {
                for (PetriNetObject modelObject : composer.getGuiModel().getPetriNetObjects()) {
                    if (object.getName().startsWith(modelObject.getName())) {
                        object.setOriginalX(modelObject.getOriginalX() + shifterX);
                        object.setOriginalY(modelObject.getOriginalY() + shifterY);
                        break;
                    }
                }
                shifterX += counterX;
                shifterY += counterY;
            }
        }
    }

    private int calculateShift(Template net, boolean calculatingX) {
        int min = Integer.MAX_VALUE;
        int max = 0;
        for (PetriNetObject object : net.guiModel().getPetriNetObjects()) {
            if (calculatingX) {
                if (object.getX() > max) {
                    max = object.getX();
                }
                if (object.getX() < min) {
                    min = object.getX();
                }
            } else {
                if (object.getY() > max) {
                    max = object.getY();
                }
                if (object.getY() < min) {
                    min = object.getY();
                }
            }
        }

        return max - min + Constants.PLACE_TRANSITION_HEIGHT + 10;
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
                firePropertyChange("unfolding", StateValue.PENDING, StateValue.DONE);
            }

        } else {
            modelChecker.kill();
            messenger.displayInfoMessage("Unfolding was interrupted by the user", "Unfolding Cancelled");

        }
    }
    void showErrorMessage(String error){
        JOptionPane.showMessageDialog(TAPAALGUI.getApp(), "The unfolding failed with error:\n" + error, "Unfolding Error", JOptionPane.ERROR_MESSAGE);
    }
}
