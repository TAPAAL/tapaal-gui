package net.tapaal.gui.petrinet;

import dk.aau.cs.TCTL.LTLANode;
import dk.aau.cs.TCTL.LTLFNode;
import dk.aau.cs.TCTL.LTLGNode;
import dk.aau.cs.TCTL.StringPosition;
import dk.aau.cs.TCTL.TCTLAFNode;
import dk.aau.cs.TCTL.TCTLAGNode;
import dk.aau.cs.TCTL.TCTLAbstractProperty;
import dk.aau.cs.TCTL.TCTLAbstractStateProperty;
import dk.aau.cs.TCTL.TCTLAndListNode;
import dk.aau.cs.TCTL.TCTLEFNode;
import dk.aau.cs.TCTL.TCTLEGNode;
import dk.aau.cs.TCTL.TCTLNotNode;
import dk.aau.cs.TCTL.TCTLOrListNode;
import dk.aau.cs.TCTL.TCTLStatePlaceHolder;
import dk.aau.cs.TCTL.visitors.RenameAllPlacesVisitor;
import dk.aau.cs.TCTL.visitors.RenameAllTransitionsVisitor;
import dk.aau.cs.verification.VerifyTAPN.VerifyDTAPN;
import net.tapaal.gui.petrinet.smartdraw.SmartDrawDialog;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.ColoredTimeInterval;
import dk.aau.cs.model.CPN.Expressions.*;
import dk.aau.cs.model.CPN.Variable;
import dk.aau.cs.model.tapn.*;
import dk.aau.cs.verification.*;
import dk.aau.cs.verification.VerifyTAPN.VerifyPN;
import net.tapaal.gui.petrinet.verification.TAPNQuery;
import pipe.gui.petrinet.dataLayer.DataLayer;
import pipe.gui.*;
import pipe.gui.petrinet.graphicElements.*;
import pipe.gui.petrinet.graphicElements.tapn.TimedInhibitorArcComponent;
import pipe.gui.petrinet.graphicElements.tapn.TimedInputArcComponent;
import pipe.gui.petrinet.graphicElements.tapn.TimedOutputArcComponent;
import pipe.gui.petrinet.graphicElements.tapn.TimedTransportArcComponent;
import net.tapaal.gui.petrinet.verification.UnfoldNet;
import net.tapaal.gui.petrinet.verification.RunningVerificationDialog;
import pipe.gui.petrinet.PetriNetTab;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.sun.jna.Platform;

public class TabTransformer {

    static public void removeTimingInformation(PetriNetTab tab){
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
            }

            for(Arc arc : template.guiModel().getArcs()){
                // Make output arc guards infinite
                if(arc instanceof TimedInputArcComponent && !(arc instanceof TimedTransportArcComponent) && (!(arc instanceof TimedInhibitorArcComponent))){
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

    static public void removeGameInformation(PetriNetTab tab) {
        for (Template template : tab.allTemplates()) {
            for (TimedTransition transition : template.model().transitions()) {
                if (transition.isUncontrollable()) {
                    transition.setUncontrollable(false);
                }
            }
        }
    }
    static public void removeDistributionInformation(PetriNetTab tab) {
        for (Template template : tab.allTemplates()) {
            for (TimedTransition transition : template.model().transitions()) {
                if (transition.hasCustomDistribution()) {
                    transition.setDistribution(SMCDistribution.defaultDistribution());
                }
            }
        }
    }

    /**
     * Converts between SMC and reachability queries
     */
    public static void convertQueriesToOrFromSmc(Iterable<TAPNQuery> queries) {
        for (TAPNQuery query : queries) {
            TCTLAbstractProperty property = query.getProperty();
            query.setProperty(smcConverter(property));
        }  
    }

    private static TCTLAbstractProperty smcConverter(TCTLAbstractProperty property) {
        TCTLAbstractStateProperty child = getFirstChildOfProperty(property);
        if (property instanceof LTLFNode) {
            return new TCTLEFNode(child);
        } else if (property instanceof LTLGNode) {
            return new TCTLAGNode(child);
        } else if (property instanceof TCTLEFNode) {
            return new LTLFNode(child);
        }  else if (property instanceof TCTLEGNode) {
            return new LTLGNode(child);
        } else if (property instanceof TCTLAFNode) {
            return new LTLFNode(child);
        } else if (property instanceof TCTLAGNode) {
            return new LTLGNode(child);
        } else if (property instanceof TCTLAndListNode) {
            TCTLAndListNode andNode = (TCTLAndListNode) property;
            List<TCTLAbstractStateProperty> properties = andNode.getProperties();
            List<TCTLAbstractStateProperty> convertedProperties = new ArrayList<>();
            for (TCTLAbstractStateProperty prop : properties) {
                convertedProperties.add((TCTLAbstractStateProperty) smcConverter(prop));
            }

            return new TCTLAndListNode(convertedProperties);
        } else if (property instanceof TCTLOrListNode) {
            TCTLOrListNode orNode = (TCTLOrListNode) property;
            List<TCTLAbstractStateProperty> properties = orNode.getProperties();
            List<TCTLAbstractStateProperty> convertedProperties = new ArrayList<>();
            for (TCTLAbstractStateProperty prop : properties) {
                convertedProperties.add((TCTLAbstractStateProperty) smcConverter(prop));
            }

            return new TCTLOrListNode(convertedProperties);
        } else if (property instanceof TCTLNotNode) {
            TCTLNotNode notNode = (TCTLNotNode) property;
            return new TCTLNotNode((TCTLAbstractStateProperty) smcConverter(notNode.getProperty()));
        } else {
            return property;
        }
    }

    private static TCTLAbstractStateProperty getFirstChildOfProperty(TCTLAbstractProperty property) {
        StringPosition[] children = property.getChildren();
        for (int i = 0; i < children.length; ++i) {
            TCTLAbstractProperty child = children[i].getObject();
            if (child instanceof TCTLAbstractStateProperty) {
                return (TCTLAbstractStateProperty) child;
            }
        }

        return new TCTLStatePlaceHolder();
    }

    static public void removeColorInformation(PetriNetTab tab) {
        tab.network().setColorTypes(List.of(ColorType.COLORTYPE_DOT));
        tab.network().setVariables(new ArrayList<Variable>());
        for (Template template : tab.allTemplates()) {
            for(TimedPlace place : template.model().places()){
                place.setCtiList(new ArrayList<>());
                place.setColorType(ColorType.COLORTYPE_DOT);
                int numberOfTokens = place.tokens().size();

                //kind of hack to convert from coloredTokens to uncolored
                if (place.numberOfTokens() > 0) {
                    Vector<ColorExpression> v = new Vector<>();
                    v.add(new DotConstantExpression());
                    Vector<ArcExpression> numbOfExpression = new Vector<>();
                    numbOfExpression.add(new NumberOfExpression(place.numberOfTokens(), v));
                    place.setTokenExpression(new AddExpression(numbOfExpression));
                } else {
                    place.resetNumberOfTokens();
                }
            }

            for (TimedTransition transition : template.model().transitions()) {
                transition.setGuard(null);
            }

            for(TimedInputArc arc : template.model().inputArcs()){
                arc.setColorTimeIntervals(new ArrayList<>());
                int expressionWeight = arc.getArcExpression().weight();
                UserOperatorExpression userOperatorExpression = new DotConstantExpression();
                Vector<ColorExpression> vecColorExpr = new Vector<ColorExpression>();
                vecColorExpr.add(userOperatorExpression);
                NumberOfExpression numbExpr = new NumberOfExpression(expressionWeight, vecColorExpr);
                arc.setExpression(numbExpr);
                arc.setWeight(new IntWeight(expressionWeight));
            }

            for(TimedOutputArc arc : template.model().outputArcs()){
                int expressionWeight = arc.getExpression().weight();
                UserOperatorExpression userOperatorExpression = new DotConstantExpression();
                Vector<ColorExpression> vecColorExpr = new Vector<ColorExpression>();
                vecColorExpr.add(userOperatorExpression);
                NumberOfExpression numbExpr = new NumberOfExpression(expressionWeight, vecColorExpr);
                arc.setExpression(numbExpr);
                arc.setWeight(new IntWeight(expressionWeight));
            }

            for(TransportArc arc : template.model().transportArcs()){
                ArcExpression oldInputExpr = arc.getInputExpression();
                arc.setColorTimeIntervals(new ArrayList<>());
                UserOperatorExpression userOperatorExpression = new DotConstantExpression();
                Vector<ColorExpression> vecColorExpr = new Vector<ColorExpression>();
                vecColorExpr.add(userOperatorExpression);
                NumberOfExpression numbExpr = new NumberOfExpression(oldInputExpr.weight(), vecColorExpr);
                arc.setInputExpression(numbExpr);
                arc.setOutputExpression(numbExpr);
                arc.setWeight(new IntWeight(oldInputExpr.weight()));
            }
        }
    }

    static public void addColorInformation(PetriNetTab tab){
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

    public static void unfoldTab(PetriNetTab oldTab, boolean partition, boolean computeColorFixpoint, boolean useSymmetricVars) {
        ModelChecker engine;
        if(oldTab.getLens().isTimed()){
            engine = new VerifyDTAPN(new FileFinder(), new MessengerImpl());
        } else {
            engine = new VerifyPN(new FileFinder(), new MessengerImpl());
        }
        engine.setup();

        if (!engine.isCorrectVersion()) {
            new MessengerImpl().displayErrorMessage(
                "No " + engine + " specified: The verification is cancelled",
                "Verification Error");
            return;
        }
        UnfoldNet thread = new UnfoldNet(engine, new MessengerImpl(), oldTab.getGuiModels(), partition, computeColorFixpoint, useSymmetricVars);
        RunningVerificationDialog dialog = new RunningVerificationDialog(TAPAALGUI.getApp(), thread, "Unfolding");
        SmartDrawDialog.setupWorkerListener(thread);
        thread.execute(oldTab.network(), oldTab);
        dialog.setVisible(true);
    }

    public static void mapQueryToNewNames(TAPNQuery query, NameMapping mapping) {
        RenameAllPlacesVisitor placeVisitor = new RenameAllPlacesVisitor(mapping);
        RenameAllTransitionsVisitor transitionVisitor = new RenameAllTransitionsVisitor(mapping);
        query.getProperty().accept(placeVisitor, null);
        query.getProperty().accept(transitionVisitor, null);
    }

    public static String createUnfoldArgumentString(String modelFile, String queryFile, VerificationOptions options) {
        if (Platform.isWindows()) {
            return options.toString() + "\"" + modelFile + "\" \"" + queryFile + "\"";
        }

        return options.toString() + ' ' + modelFile + ' ' + queryFile;
    }
}
