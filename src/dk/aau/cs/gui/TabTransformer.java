package dk.aau.cs.gui;

import dk.aau.cs.TCTL.TCTLAtomicPropositionNode;
import dk.aau.cs.TCTL.TCTLConstNode;
import dk.aau.cs.TCTL.TCTLEFNode;
import dk.aau.cs.TCTL.TCTLPlaceNode;
import dk.aau.cs.TCTL.visitors.RenameAllPlacesVisitor;
import dk.aau.cs.TCTL.visitors.RenameAllTransitionsVisitor;
import dk.aau.cs.io.LoadedModel;
import dk.aau.cs.io.TapnXmlLoader;
import dk.aau.cs.io.TimedArcPetriNetNetworkWriter;
import dk.aau.cs.io.queries.XMLQueryLoader;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.ColoredTimeInterval;
import dk.aau.cs.model.CPN.Expressions.*;
import dk.aau.cs.model.CPN.Variable;
import dk.aau.cs.model.tapn.*;
import dk.aau.cs.util.FormatException;
import dk.aau.cs.util.Tuple;
import dk.aau.cs.verification.NameMapping;
import dk.aau.cs.verification.ProcessRunner;
import dk.aau.cs.verification.TAPNComposer;
import dk.aau.cs.verification.VerificationOptions;
import dk.aau.cs.verification.VerifyTAPN.VerifyPNUnfoldOptions;
import pipe.dataLayer.DataLayer;
import pipe.dataLayer.Template;
import pipe.gui.MessengerImpl;
import pipe.gui.Zoomer;
import pipe.gui.graphicElements.*;
import pipe.gui.graphicElements.tapn.TimedInputArcComponent;
import pipe.gui.graphicElements.tapn.TimedOutputArcComponent;
import pipe.gui.graphicElements.tapn.TimedTransportArcComponent;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class TabTransformer {
    private static String unfoldpath = "";

    static public void removeTimingInformation(TabContent tab){
        for(Template template : tab.allTemplates()){
            ArrayList<TimedTransportArcComponent> transportArcComponents = new ArrayList<TimedTransportArcComponent>();
            // Make place token age invariant infinite
            for(TimedPlace place : template.model().places()){
                place.setInvariant(TimeInvariant.LESS_THAN_INFINITY);
                place.getCtiList().clear();
            }
            // Make transitions non-urgent
            for(TimedTransition transition : template.model().transitions()){
                transition.setUrgent(false);
                //TODO: what is default guard?
                transition.setGuard(null);
            }

            for(Arc arc : template.guiModel().getArcs()){
                // Make output arc guards infinite
                if(arc instanceof TimedInputArcComponent && !(arc instanceof TimedTransportArcComponent)){
                    TimedInputArcComponent arcComp = (TimedInputArcComponent) arc;
                    arcComp.underlyingTimedInputArc().setColorTimeIntervals(new ArrayList<ColoredTimeInterval>());
                }
                if(arc instanceof TimedOutputArcComponent) {
                    TimedOutputArcComponent arcComp = (TimedOutputArcComponent) arc;
                    arcComp.setGuardAndWeight(TimeInterval.ZERO_INF, arcComp.getWeight());
                }

                // Add and process transport arcs in separate list to avoid delete errors
                if(arc instanceof TimedTransportArcComponent){
                    TimedTransportArcComponent arcComp = (TimedTransportArcComponent) arc;
                    arcComp.underlyingTransportArc().setColorTimeIntervals(new ArrayList<ColoredTimeInterval>());
                    transportArcComponents.add(arcComp);
                }
            }

            // Replace transport arcs with regular arcs
            for(TimedTransportArcComponent arc : transportArcComponents){
                // Input arc
                if(arc.getSource() instanceof Place) {
                    TimedPlace source = template.model().getPlaceByName(arc.getSource().getName());
                    TimedTransition destination = template.model().getTransitionByName(arc.getTarget().getName());

                    TimedInputArc addedArc = new TimedInputArc(source, destination, TimeInterval.ZERO_INF, arc.getWeight(), arc.underlyingTransportArc().getInputExpression());


                    // GUI
                    DataLayer guiModel = template.guiModel();
                    Place guiSource = guiModel.getPlaceByName(arc.getSource().getName());
                    Transition guiTarget = guiModel.getTransitionByName(arc.getTarget().getName());
                    TimedInputArcComponent newArc = new TimedInputArcComponent(
                        new TimedOutputArcComponent(
                            guiSource,
                            guiTarget,
                            arc.getWeight().value(),
                            arc.getSource().getName() + "_to_" + arc.getTarget().getName()
                        ),
                        tab.getLens()
                    );

                    // Build ArcPath
                    Place oldGuiSource = guiModel.getPlaceByName(arc.getSource().getName());
                    Transition oldGuiTarget = guiModel.getTransitionByName(arc.getTarget().getName());
                    ArcPath newArcPath = createArcPath(guiModel, oldGuiSource, oldGuiTarget, newArc);

                    // Set arcPath, guiModel and connectors
                    newArc.setUnderlyingArc(addedArc);
                    newArc.setArcPath(newArcPath);
                    newArc.updateArcPosition();
                    guiModel.addPetriNetObject(newArc);

                    //Change the partner

                    TimedOutputArcComponent arc2 = convertPartner(arc.getConnectedTo(), template, guiModel);

                    removeTransportArc(arc, guiModel);

                    //Add arc to model and GUI
                    template.model().add(addedArc);
                    template.model().add(arc2.underlyingArc());

                }

            }
        }
    }
    static void removeTransportArc(TimedTransportArcComponent arc, DataLayer guiModel){
        // Delete the transport arc
        arc.underlyingTransportArc().delete();
        TimedTransportArcComponent partner = arc.getConnectedTo();

        guiModel.removePetriNetObject(arc);
        guiModel.removePetriNetObject(partner);
    }
    static TimedOutputArcComponent convertPartner(TimedTransportArcComponent arc, Template template, DataLayer guiModel) {
        //Add new arc

        TimedPlace destination = template.model().getPlaceByName(arc.getTarget().getName());
        TimedTransition source = template.model().getTransitionByName(arc.getSource().getName());

        TimedOutputArc addedArc = new TimedOutputArc(source, destination, arc.getWeight(), arc.underlyingTransportArc().getOutputExpression());
        //template.model().add(addedArc);

        // GUI

        Place guiTarget = guiModel.getPlaceByName(arc.getTarget().getName());
        Transition guiSource = guiModel.getTransitionByName(arc.getSource().getName());
        TimedOutputArcComponent newArc = new TimedOutputArcComponent(
            guiSource,
                guiTarget,
                arc.getWeight().value(),
                arc.getSource().getName() + "_to_" + arc.getTarget().getName()
        );

        // Build ArcPath
        Place oldGuiTarget = guiModel.getPlaceByName(arc.getTarget().getName());
        Transition oldGuiSource = guiModel.getTransitionByName(arc.getSource().getName());
        ArcPath newArcPath = createArcPath(guiModel, oldGuiSource, oldGuiTarget, newArc);

        // Set arcPath, guiModel and connectors
        newArc.setUnderlyingArc(addedArc);
        newArc.setArcPath(newArcPath);
        newArc.updateArcPosition();
        guiModel.addPetriNetObject(newArc);

        return newArc;

    }

    private static ArcPath createArcPath(DataLayer currentGuiModel, PlaceTransitionObject source, PlaceTransitionObject target, Arc arc) {
        Arc guiArc = currentGuiModel.getArcByEndpoints(source, target);
        ArcPath arcPath = guiArc.getArcPath();
        int arcPathPointsNum = arcPath.getNumPoints();

        // Build ArcPath
        ArcPath newArcPath = new ArcPath(arc);
        newArcPath.purgePathPoints();
        for(int k = 0; k < arcPathPointsNum; k++) {
            ArcPathPoint point = arcPath.getArcPathPoint(k);
            newArcPath.addPoint(
                    point.getPoint().x,
                    point.getPoint().y,
                    point.getPointType()
            );
        }

        return newArcPath;
    }

    static public void removeGameInformation(TabContent tab) {
        for (Template template : tab.allTemplates()) {
            for (TimedTransition transition : template.model().transitions()) {
                if (transition.isUncontrollable()) {
                    transition.setUncontrollable(false);
                }
            }
        }
    }
    static public void removeColorInformation(TabContent tab) {
        tab.network().setColorTypes(new ArrayList<ColorType>());
        tab.network().setVariables(new ArrayList<Variable>());
        for (Template template : tab.allTemplates()) {
            for(TimedPlace place : template.model().places()){
                place.setCtiList(new ArrayList<>());
                place.setColorType(ColorType.COLORTYPE_DOT);
                int numberOfTokens = place.tokens().size();

                //kind of hack to convert from coloredTokens to uncolored
                for(TimedToken token : place.tokens()){
                    token.setColor(place.getColorType().getFirstColor());
                }
            }

            for (TimedTransition transition : template.model().transitions()) {
                //TODO: what is the default guard
                transition.setGuard(null);
            }

            for(TimedInputArc arc : template.model().inputArcs()){
                arc.setColorTimeIntervals(new ArrayList<>());
                int expressionWeight = arc.getArcExpression().weight();
                ColorType ct = arc.source().getColorType();
                UserOperatorExpression userOperatorExpression = new UserOperatorExpression(ct.getFirstColor());
                Vector<ColorExpression> vecColorExpr = new Vector<ColorExpression>();
                vecColorExpr.add(userOperatorExpression);
                NumberOfExpression numbExpr = new NumberOfExpression(1, vecColorExpr);
                arc.setExpression(numbExpr);
                arc.setWeight(new IntWeight(expressionWeight));
            }

            for(TimedOutputArc arc : template.model().outputArcs()){
                ColorType ct = arc.destination().getColorType();
                int expressionWeight = arc.getExpression().weight();
                UserOperatorExpression userOperatorExpression = new UserOperatorExpression(ct.getFirstColor());
                Vector<ColorExpression> vecColorExpr = new Vector<ColorExpression>();
                vecColorExpr.add(userOperatorExpression);
                NumberOfExpression numbExpr = new NumberOfExpression(1, vecColorExpr);
                arc.setExpression(numbExpr);
                arc.setWeight(new IntWeight(expressionWeight));
            }

            for(TransportArc arc : template.model().transportArcs()){
                ColorType ct = arc.source().getColorType();
                ArcExpression oldInputExpr = arc.getInputExpression();
                UserOperatorExpression userOperatorExpression = new UserOperatorExpression(ct.getFirstColor());
                Vector<ColorExpression> vecColorExpr = new Vector<ColorExpression>();
                vecColorExpr.add(userOperatorExpression);
                NumberOfExpression numbExpr = new NumberOfExpression(1, vecColorExpr);
                arc.setInputExpression(numbExpr);
                arc.setOutputExpression(numbExpr);
                arc.setWeight(new IntWeight(oldInputExpr.weight()));
            }
        }
    }

    static public void addColorInformation(TabContent tab){
        for (Template template : tab.allTemplates()) {
            for(TimedInputArc arc : template.model().inputArcs()){
                arc.setColorTimeIntervals(new ArrayList<>());
                ColorType ct = arc.source().getColorType();
                UserOperatorExpression userOperatorExpression = new UserOperatorExpression(ct.getFirstColor());
                Vector<ColorExpression> vecColorExpr = new Vector<ColorExpression>();
                vecColorExpr.add(userOperatorExpression);
                NumberOfExpression numbExpr = new NumberOfExpression(arc.getWeight().value(), vecColorExpr);
                arc.setExpression(numbExpr);
                arc.setWeight(new IntWeight(1));
            }

            for(TimedOutputArc arc : template.model().outputArcs()){
                ColorType ct = arc.destination().getColorType();
                UserOperatorExpression userOperatorExpression = new UserOperatorExpression(ct.getFirstColor());
                Vector<ColorExpression> vecColorExpr = new Vector<ColorExpression>();
                vecColorExpr.add(userOperatorExpression);
                NumberOfExpression numbExpr = new NumberOfExpression(arc.getWeight().value(), vecColorExpr);
                arc.setExpression(numbExpr);
                arc.setWeight(new IntWeight(1));
            }

            for(TransportArc arc : template.model().transportArcs()){
                ColorType ct = arc.source().getColorType();
                UserOperatorExpression userOperatorExpression = new UserOperatorExpression(ct.getFirstColor());
                Vector<ColorExpression> vecColorExpr = new Vector<ColorExpression>();
                vecColorExpr.add(userOperatorExpression);
                NumberOfExpression numbExpr = new NumberOfExpression(arc.getWeight().value(), vecColorExpr);
                arc.setInputExpression(numbExpr);
                arc.setOutputExpression(numbExpr);
                arc.setWeight(new IntWeight(1));

            }
        }
    }

    public static TabContent unfoldTab(TabContent oldTab) {
        TAPNComposer composer = new TAPNComposer(new MessengerImpl(), oldTab.getGuiModels(), true, true);
        Tuple<TimedArcPetriNet, NameMapping> transformedModel = composer.transformModel(oldTab.network());

        File modelFile = null;
        File queryFile = null;
        File modelOut = null;
        File queryOut = null;
        try {
            modelFile = File.createTempFile("modelInUnfold", ".tapn");
            queryFile = File.createTempFile("queryInUnfold", ".q");
            modelOut = File.createTempFile("modelOut", ".tapn");
            queryOut = File.createTempFile("queryOut", ".xml");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            TimedArcPetriNetNetwork network = new TimedArcPetriNetNetwork();
            ArrayList<Template> templates = new ArrayList<Template>(1);
            ArrayList<pipe.dataLayer.TAPNQuery> queries = new ArrayList<pipe.dataLayer.TAPNQuery>(1);


            network.add(transformedModel.value1());
            for (ColorType ct :oldTab.network().colorTypes()) {
                network.add(ct);
            }
            for (Variable variable: oldTab.network().variables()) {
                network.add(variable);
            }
            templates.add(new Template(transformedModel.value1(), composer.getGuiModel(), new Zoomer()));
            TimedArcPetriNetNetworkWriter writerTACPN = new TimedArcPetriNetNetworkWriter(network, templates, queries, oldTab.network().constants());


            writerTACPN.savePNML(modelFile);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        List<TAPNQuery> clonedQueries = new Vector<dk.aau.cs.model.tapn.TAPNQuery>();
        if (oldTab.queries().iterator().hasNext()) {
            for (pipe.dataLayer.TAPNQuery query : oldTab.queries()) {
                dk.aau.cs.model.tapn.TAPNQuery clonedQuery = new dk.aau.cs.model.tapn.TAPNQuery(query.getProperty().copy(), query.getCapacity());
                mapQueryToNewNames(clonedQuery, transformedModel.value2());
                clonedQueries.add(clonedQuery);
            }
        }
        else {
            String templateName = oldTab.network().activeTemplates().get(0).name();
            String placeName = oldTab.network().activeTemplates().get(0).places().get(0).name();
            TCTLAtomicPropositionNode atomicStartNode = new TCTLAtomicPropositionNode(new TCTLPlaceNode(templateName, placeName), ">=", new TCTLConstNode(1));
            TCTLEFNode efNode = new TCTLEFNode(atomicStartNode);
            dk.aau.cs.model.tapn.TAPNQuery test = new dk.aau.cs.model.tapn.TAPNQuery(efNode, 1000);
            mapQueryToNewNames(test, transformedModel.value2());
            clonedQueries.add(test);
        }

        ProcessRunner runner;
        //TODO::  implement possibility of there not being any queries, and make it possible to send all queries to the engine (requires the engine to be modified)
        OutputStream os;
        try {
            os = new FileOutputStream(queryFile);
            os.write(clonedQueries.get(0).toString().getBytes(), 0, clonedQueries.get(0).toString().length());
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        VerificationOptions unfoldTACPNOptions = new VerifyPNUnfoldOptions(modelOut.getAbsolutePath(), queryOut.getAbsolutePath(), "ff", false);
        runner = new ProcessRunner(getunfoldPath(), createUnfoldArgumentString(modelFile.getAbsolutePath(), queryFile.getAbsolutePath(), unfoldTACPNOptions));
        runner.run();

        //String errorOutput = readOutput(runner.errorOutput());
        //String standardOutput = readOutput(runner.standardOutput());
       // Logger.log(errorOutput);

        TapnXmlLoader tapnLoader = new TapnXmlLoader();
        File fileOut = new File(modelOut.getAbsolutePath());
        TabContent newTab;
        try {
            LoadedModel loadedModel = tapnLoader.load(fileOut);
            newTab = new TabContent(loadedModel.network(), loadedModel.templates(),loadedModel.queries(),new TabContent.TAPNLens(oldTab.getLens().isTimed(), oldTab.getLens().isGame(), false));
            newTab.setInitialName(oldTab.getTabTitle().replace(".tapn", "") + "-unfolded");
            newTab.addQuery(getQuery(queryOut, loadedModel.network()));
            return newTab;
        } catch (FormatException e) {
            e.printStackTrace();
        }
        return  null;
    }

    private static pipe.dataLayer.TAPNQuery getQuery(File queryFile, TimedArcPetriNetNetwork network) {
        XMLQueryLoader queryLoader = new XMLQueryLoader(queryFile, network);
        List<pipe.dataLayer.TAPNQuery> queries = new ArrayList<pipe.dataLayer.TAPNQuery>();
        queries.addAll(queryLoader.parseQueries());
        return queries.get(0);
    }

    public static void mapQueryToNewNames(dk.aau.cs.model.tapn.TAPNQuery query, NameMapping mapping) {
        RenameAllPlacesVisitor placeVisitor = new RenameAllPlacesVisitor(mapping);
        RenameAllTransitionsVisitor transitionVisitor = new RenameAllTransitionsVisitor(mapping);
        query.getProperty().accept(placeVisitor, null);
        query.getProperty().accept(transitionVisitor, null);
    }

    public static String createUnfoldArgumentString(String modelFile, String queryFile, VerificationOptions options) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(modelFile);
        buffer.append(" ");
        buffer.append(queryFile);
        buffer.append(" ");
        buffer.append(options.toString());
        return buffer.toString();
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
                buffer.append(line);
                buffer.append(System.getProperty("line.separator"));
            }
        } catch (IOException e) {
        }

        return buffer.toString();
    }

    public static String getunfoldPath() {
        if (unfoldpath.isEmpty()) {
            File f = new File(new File(System.getProperty("user.dir")).getParent() + File.separator + "bin" + File.separator + "UnfoldTACPN");
            File f2 = new File(System.getProperty("user.dir") + File.separator + "bin"+ File.separator + "UnfoldTACPN");
            if (f.exists()) {
                unfoldpath = f.getAbsolutePath();
                return unfoldpath;
            }
            else if (f2.exists()) {
                unfoldpath = f2.getAbsolutePath();
                return unfoldpath;
            }
            else {
                FileDialog dialog = new FileDialog((Frame)null, "Select File to Open");
                dialog.setMode(FileDialog.LOAD);
                dialog.setVisible(true);
                unfoldpath = dialog.getDirectory() + dialog.getFile();
                return unfoldpath;
            }
        } else {
            return unfoldpath;
        }
    }
}
