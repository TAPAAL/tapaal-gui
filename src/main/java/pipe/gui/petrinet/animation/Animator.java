package pipe.gui.petrinet.animation;

import java.awt.Container;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;
import java.util.stream.Collectors;

import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;

import dk.aau.cs.model.tapn.simulation.*;
import net.tapaal.gui.petrinet.animation.AnimationTokenSelectDialog;
import pipe.gui.petrinet.dataLayer.DataLayer;
import net.tapaal.gui.petrinet.Template;
import pipe.gui.MessengerImpl;
import pipe.gui.TAPAALGUI;
import pipe.gui.petrinet.action.GuiAction;
import pipe.gui.petrinet.graphicElements.Transition;
import pipe.gui.petrinet.graphicElements.Place;
import pipe.gui.petrinet.graphicElements.tapn.TimedPlaceComponent;
import pipe.gui.swingcomponents.EscapableDialog;
import pipe.gui.petrinet.PetriNetTab;
import net.tapaal.gui.petrinet.animation.TransitionFiringComponent;
import net.tapaal.gui.petrinet.dialog.ColoredBindingSelectionDialog;
import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.ProductType;
import dk.aau.cs.model.CPN.Variable;
import dk.aau.cs.model.CPN.Expressions.AddExpression;
import dk.aau.cs.model.CPN.Expressions.ArcExpression;
import dk.aau.cs.model.CPN.Expressions.ColorExpression;
import dk.aau.cs.model.CPN.Expressions.DotConstantExpression;
import dk.aau.cs.model.CPN.Expressions.NumberOfExpression;
import dk.aau.cs.model.CPN.Expressions.TupleExpression;
import dk.aau.cs.model.CPN.Expressions.UserOperatorExpression;
import dk.aau.cs.model.tapn.NetworkMarking;
import dk.aau.cs.model.tapn.TimeInterval;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedInputArc;
import dk.aau.cs.model.tapn.TimedOutputArc;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedToken;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.model.tapn.TransportArc;
import dk.aau.cs.util.IntervalOperations;
import dk.aau.cs.util.RequireException;
import dk.aau.cs.util.Tuple;
import dk.aau.cs.verification.NameMapping;
import dk.aau.cs.verification.TAPNComposer;
import dk.aau.cs.verification.VerifyTAPN.ColorBindingParser;
import dk.aau.cs.verification.VerifyTAPN.TraceType;
import dk.aau.cs.verification.VerifyTAPN.VerifyCPNExporter;
import dk.aau.cs.verification.VerifyTAPN.VerifyPNInteractiveHandle;
import dk.aau.cs.verification.VerifyTAPN.VerifyTAPNExporter;

public class Animator {
    private final ArrayList<TAPNNetworkTraceStep> actionHistory = new ArrayList<TAPNNetworkTraceStep>();
    private int currentAction = -1;
    private final ArrayList<NetworkMarking> markings = new ArrayList<NetworkMarking>();
    private int currentMarkingIndex = 0;
    private TAPNNetworkTrace trace = null;

    public FiringMode firingmode = new RandomFiringMode();
    private final PetriNetTab tab;
    private NetworkMarking initialMarking;

    private boolean isDisplayingUntimedTrace = false;
    private static boolean isUrgentTransitionEnabled = false;

    private Map<String, TAPNNetworkTrace> traceMap;

    private VerifyPNInteractiveHandle interactiveEngine;
    private boolean isUsingInteractiveEngine;
    private Map<TimedTransition, List<Map<Variable, Color>>> validBindingsMap;

    public static boolean isUrgentTransitionEnabled(){
        return isUrgentTransitionEnabled;
    }

    public Animator(PetriNetTab tab) {
        super();

        this.tab = tab;
    }

    public void initializeInteractiveEngine() {
        if (!tab.getLens().isColored()) return;

        try {
            TAPNComposer composer = new TAPNComposer(new MessengerImpl(), tab.getGuiModels(), tab.getLens(), false, true);
            Tuple<TimedArcPetriNet, NameMapping> composedModel = composer.transformModel(tab.network());

            VerifyTAPNExporter exporter = new VerifyCPNExporter();
            var exportedModel = exporter.exportModel(composedModel, composer.getGuiModel());

            interactiveEngine = new VerifyPNInteractiveHandle(tab.network(), composer, composedModel.value2());
            isUsingInteractiveEngine = interactiveEngine.startInteractiveMode(exportedModel.modelFile());
            if (!isUsingInteractiveEngine) {
                JOptionPane.showMessageDialog(TAPAALGUI.getApp(), 
                    "Failed to start VerifyPN interactive mode", 
                    "Engine Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(TAPAALGUI.getApp(), 
            "Error initializing interactive engine: " + e.getMessage(), 
            "Engine Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private NetworkMarking currentMarking() {
        return markings.get(currentMarkingIndex);
    }

    public void setTrace(TAPNNetworkTrace trace, Map<String, TAPNNetworkTrace> traceMap) {
        this.traceMap = traceMap;
        setTrace(trace);
        tab.getAnimationController().updateTraceBox(traceMap);
    }

    public Map<String, TAPNNetworkTrace> getTraceMap() {
        return this.traceMap;
    }
    public void setTraceMap(Map<String, TAPNNetworkTrace> traceMap) {
        this.traceMap = traceMap;
    }

    public void changeTrace(TAPNNetworkTrace trace) {
        resetForTraceChange();
        setTrace(trace);
    }

    public void setTrace(TAPNNetworkTrace trace) {
        tab.setAnimationMode(true, tab.getLens().isColored());

        try {
            if (trace.isConcreteTrace()) {
                this.trace = trace;
                if (trace.isColoredTrace()) {
                    setColoredTrace(trace);
                } else {
                    setTimedTrace(trace);
                }
            } else {
                setUntimedTrace(trace);
                isDisplayingUntimedTrace = true;
            }
            currentAction = -1;
            currentMarkingIndex = 0;
            tab.network().setMarking(markings.get(currentMarkingIndex));
            tab.getAnimationHistorySidePanel().setSelectedIndex(0);
            updateAnimationButtonsEnabled();
            updateFireableTransitions();
        } catch (RequireException e) {
            unhighlightDisabledTransitions();
            tab.setAnimationMode(false);
            JOptionPane.showMessageDialog(TAPAALGUI.getApp(), "There was an error in the trace. Reason: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
    }

    private void setUntimedTrace(TAPNNetworkTrace trace) {
        tab.addAbstractAnimationPane();
        AnimationHistoryList untimedAnimationHistory = tab.getUntimedAnimationHistory();
        for(TAPNNetworkTraceStep step : trace){
            untimedAnimationHistory.addHistoryItem(step.toString());
        }

        tab.getUntimedAnimationHistory().setSelectedIndex(0);
        setFiringmode("Random");

        JOptionPane.showMessageDialog(TAPAALGUI.getApp(),
            "The verification process returned an untimed trace.\n\n"
                + "This means that with appropriate time delays the displayed\n"
                + "sequence of discrete transitions can become a concrete trace.\n"
                + "In case of liveness properties (EG, AF) the untimed trace\n"
                + "either ends in a deadlock, a time divergent computation without\n"
                + "any discrete transitions, or it loops back to some earlier configuration.\n"
                + "The user may experiment in the simulator with different time delays\n"
                + "in order to realize the suggested untimed trace in the model.",
            "Verification Information", JOptionPane.INFORMATION_MESSAGE);
    }

    private void setTimedTrace(TAPNNetworkTrace trace) {
        NetworkMarking previousMarking = initialMarking;
        TimedTransition previousTransition = null;
        for (TAPNNetworkTraceStep step : trace) {
            if (step instanceof TAPNNetworkTimedTransitionStep) {
                TimedTransition transition = ((TAPNNetworkTimedTransitionStep) step).getTransition();

                if (previousMarking != null && previousTransition != null) {
                    checkTokensRemoved(previousMarking, previousTransition);
                }

                previousMarking = currentMarking();
                previousTransition = transition;
            }

            addMarking(step, step.performStepFrom(currentMarking()));
        }
        
        TimedTAPNNetworkTrace timedTrace = (TimedTAPNNetworkTrace)trace;
        if (timedTrace.getTraceType() != TraceType.NOT_EG) { //If the trace was not explicitly set, maybe we have calculated it is deadlock.
            tab.getAnimationHistorySidePanel().setLastShown(timedTrace.getTraceType());
        }
    }

    private void setColoredTrace(TAPNNetworkTrace trace) {
        for (TAPNNetworkTraceStep step : trace) {
            TAPNNetworkColoredTransitionStep coloredStep = (TAPNNetworkColoredTransitionStep)step;
            addMarking(step, coloredStep.getMarking());
        }

        updateBindings(0);
    }

    public boolean isColoredTrace() {
        return trace != null && trace.isColoredTrace();
    }

    /**
     * Checks if the number of tokens removed from a place is as expected, otherwise shows an error message
     */
    private void checkTokensRemoved(NetworkMarking previousMarking, TimedTransition previousTransition) {
        if (tab.getLens().isStochastic()) return;
        for (TimedInputArc inputArc : previousTransition.getInputArcs()) {
            int tokensBefore = previousMarking.getTokensFor(inputArc.source()).size();
            
            int newTokens = 0;
            for (TimedOutputArc outputArc : previousTransition.getOutputArcs()) {
                if (outputArc.destination().equals(inputArc.source())) {
                    newTokens += outputArc.getWeight().value();
                }
            }

            int tokensAfter = currentMarking().getTokensFor(inputArc.source()).size() + newTokens;
            int tokenDelta = Math.abs(tokensBefore - tokensAfter);

            if (tokenDelta != inputArc.getWeight().value()) {
                String errorStr = "Error executing trace: expected to have " + inputArc.getWeight().value();
                errorStr += inputArc.getWeight().value() == 1 ? " token" : " tokens"; 
                errorStr += " in place " + inputArc.source().name() + ", but had " + tokenDelta;
                errorStr += tokenDelta == 1 ? " token" : " tokens";
                errorStr += " in step number " + currentMarkingIndex;
                JOptionPane.showMessageDialog(TAPAALGUI.getApp(), errorStr, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void addToTimedTrace(List<TAPNNetworkTraceStep> stepList){
        for (TAPNNetworkTraceStep step : stepList) {
            addMarking(step, step.performStepFrom(currentMarking()));
        }
    }

    public NetworkMarking getInitialMarking(){
        return initialMarking;
    }

    /**
     * Called during animation to unhighlight previously highlighted transitions
     */
    private void unhighlightDisabledTransitions() {
        disableTransitions();
    }

    private void updateValidBindingsMap() {
        if (!isUsingInteractiveEngine) return;
        validBindingsMap = interactiveEngine.sendMarking(currentMarking());
    }

    private boolean isColoredTransitionEnabled(TimedTransition transition) {
        if (tab.getLens().isColored() && isUsingInteractiveEngine) {
            if (validBindingsMap.keySet().contains(transition)) {
                return true;
            }
        }

        return false;
    }

    private void updateFireableTransitionsColored(TransitionFiringComponent transFireComponent) {
        if (tab.getLens().isColored()) {
            updateValidBindingsMap();
            for (Template template : tab.activeTemplates()) {
                for (TimedTransition transition : template.model().transitions()) {
                    if (isColoredTransitionEnabled(transition)) {
                        Transition guiTransition = template.guiModel().getTransitionByName(transition.name());
                        if (guiTransition != null) {
                            guiTransition.markTransitionEnabled(true);
                            transFireComponent.addTransition(template, guiTransition);
                        }
                    }
                }
            }
        }
    }

    public void updateFireableTransitions() {
        TransitionFiringComponent transFireComponent = tab.getTransitionFiringComponent();
        transFireComponent.startReInit();

        isUrgentTransitionEnabled = false;

        if (tab.getLens().isColored()) {
            updateFireableTransitionsColored(transFireComponent);
        } else {
            outer: for( Template template : tab.activeTemplates()){
                for (TimedTransition t : template.model().transitions()) {
                    if (t.isUrgent() && t.isEnabled()) {
                        isUrgentTransitionEnabled = true;
                        break outer;
                    }
                }
            }
    
            for (Template template : tab.activeTemplates()) {
                for (Transition t : template.guiModel().transitions()) {
                    if (t.isTransitionEnabled()) {
                        t.markTransitionEnabled(true);
                        transFireComponent.addTransition(template, t);
                    } else if (TAPAALGUI.getAppGui().isShowingDelayEnabledTransitions() &&
                        t.isDelayEnabled() && !isUrgentTransitionEnabled
                    ) {
                        t.markTransitionDelayEnabled(true);
                        transFireComponent.addTransition(template, t);
                    }
                }
            }
        }

        transFireComponent.reInitDone();
    }

    /**
     * Called at end of animation and resets all Transitions to false and
     * unhighlighted
     */
    private void disableTransitions() {
        for (Template template : tab.allTemplates()) {
            for (Transition tempTransition : template.guiModel().transitions()) {
                tempTransition.disableHightlight();
                tempTransition.repaint();
            }
        }
    }

    /**
     * Stores model at start of animation
     */
    public void storeModel() {
        initialMarking = tab.network().marking();
        resethistory();
        markings.add(initialMarking);
        storeTokenState();
        updateColoredMarking();
    }

    /**
     * Restores model at end of animation and sets all transitions to false and
     * unhighlighted
     */
    public void restoreModel() {
        if (tab != null) {
            disableTransitions();
            tab.network().setMarking(initialMarking);
            restoreTokenState();
            currentAction = -1;

            if (isUsingInteractiveEngine) {
                interactiveEngine.stopInteractiveMode();
                isUsingInteractiveEngine = false;
            }
        }
    }

    private Map<TimedPlace, Tuple<List<TimedToken>, ArcExpression>> storedTokenState = new HashMap<>();

    private void storeTokenState() {
        storedTokenState.clear();
        for (Place guiPlace : tab.currentTemplate().guiModel().getPlaces()) {
            TimedPlaceComponent placeComponent = (TimedPlaceComponent)guiPlace;
            TimedPlace place = placeComponent.underlyingPlace();
            NetworkMarking marking = tab.network().marking();
            List<TimedToken> tokens = marking.getTokensFor(place);
            ArcExpression expression = place.getTokensAsExpression();

            List<TimedToken> tokensCopy = new ArrayList<>(tokens);
            storedTokenState.put(place, new Tuple<>(tokensCopy, expression));
        }
    }

    private void restoreTokenState() {
        for (Place guiPlace : tab.currentTemplate().guiModel().getPlaces()) {
            TimedPlaceComponent placeComponent = (TimedPlaceComponent)guiPlace;
            TimedPlace place = placeComponent.underlyingPlace();
            
            Tuple<List<TimedToken>, ArcExpression> state = storedTokenState.get(place);
            if (state != null) {
                place.resetNumberOfTokensColor();
                place.updateTokens(state.value1(), state.value2());
                placeComponent.setUnderlyingPlace(place);
            }
        }

        activeGuiModel().repaintPlaces();
    }

    /**
     * Steps back through previously fired transitions
     *
     * @author jokke refactored and added backwards firing for TAPNTransitions
     */

    public void stepBack() {
        tab.getAnimationHistorySidePanel().stepBackwards();
        if (!actionHistory.isEmpty()){
            TAPNNetworkTraceStep lastStep = actionHistory.get(currentAction);
            if(isDisplayingUntimedTrace && lastStep instanceof TAPNNetworkTimedTransitionStep){
                AnimationHistoryList untimedAnimationHistory = tab.getUntimedAnimationHistory();
                String previousInUntimedTrace = untimedAnimationHistory.getElement(untimedAnimationHistory.getSelectedIndex());
                if(previousInUntimedTrace.equals(lastStep.toString())){
                    untimedAnimationHistory.stepBackwards();
                }
            }

            currentAction--;
            currentMarkingIndex--;
            updateBindings(currentAction + 1);
            tab.network().setMarking(markings.get(currentMarkingIndex));
            updateColoredMarking();
            activeGuiModel().repaintPlaces();
            unhighlightDisabledTransitions();
            updateFireableTransitions();
            updateAnimationButtonsEnabled();
            updateMouseOverInformation();
            reportBlockingPlaces();
        }
    }

    /**
     * Steps forward through previously fired transitions
     */

    public void stepForward() {
        tab.getAnimationHistorySidePanel().stepForward();
        if (currentAction == actionHistory.size()-1 && trace != null) {
            int selectedIndex = tab.getAnimationHistorySidePanel().getSelectedIndex();
            int action = currentAction;
            int markingIndex = currentMarkingIndex;

            TimedTAPNNetworkTrace timedTrace = (TimedTAPNNetworkTrace)trace;
            if (timedTrace.getTraceType() == TraceType.EG_DELAY_FOREVER) {
                addMarking(new TAPNNetworkTimeDelayStep(BigDecimal.ONE), currentMarking().delay(BigDecimal.ONE));
            }

            if (timedTrace.getLoopToIndex() != -1) {
                addToTimedTrace(timedTrace.getLoopSteps());
            }

            tab.getAnimationHistorySidePanel().setSelectedIndex(selectedIndex);
            currentAction = action;
            currentMarkingIndex = markingIndex;
        }
        
        if (currentAction < actionHistory.size() - 1) {
            TAPNNetworkTraceStep nextStep = actionHistory.get(currentAction+1);
            if(isDisplayingUntimedTrace && nextStep instanceof TAPNNetworkTimedTransitionStep){
                AnimationHistoryList untimedAnimationHistory = tab.getUntimedAnimationHistory();
                String nextInUntimedTrace = untimedAnimationHistory.getElement(untimedAnimationHistory.getSelectedIndex()+1);
                if(nextInUntimedTrace.equals(nextStep.toString())){
                    untimedAnimationHistory.stepForward();
                }
            }

            currentAction++;
            currentMarkingIndex++;
            updateBindings(currentAction + 1);
            tab.network().setMarking(markings.get(currentMarkingIndex));
            updateColoredMarking();
            activeGuiModel().repaintPlaces();
            unhighlightDisabledTransitions();
            updateFireableTransitions();
            activeGuiModel().redrawVisibleTokenLists();
        
            updateAnimationButtonsEnabled();
            updateMouseOverInformation();
            reportBlockingPlaces();
        }        
    }

    private void updateColoredMarking() {
        if (!tab.getLens().isColored()) return;

        NetworkMarking marking = tab.network().marking();
        var markingMap = marking.getMarkingMap();
        Template template = tab.currentTemplate();
        var localMarking = markingMap.get(template.model());
        Map<TimedPlace, List<TimedToken>> placesToTokensCopy = new HashMap<>();
        for (var entry : localMarking.getPlacesToTokensMap().entrySet()) {
            placesToTokensCopy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }

        for (var entry : marking.getSharedPlacesTokens().entrySet()) {
            placesToTokensCopy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
 
        for (var guiPlace : template.guiModel().getPlaces()) {
            var placeComponent = (TimedPlaceComponent)guiPlace;
            TimedPlace place = placeComponent.underlyingPlace();
            place.resetNumberOfTokensColor();
            
            if (!placesToTokensCopy.containsKey(place) || placesToTokensCopy.get(place).isEmpty()) {
                place.updateTokens(new ArrayList<>(), null);
                placeComponent.setUnderlyingPlace(place);
                continue;
            }

            List<TimedToken> tokens = placesToTokensCopy.get(place);
            Map<Color, Integer> numberOfMap = new HashMap<>();
            for (TimedToken token : tokens) {
                numberOfMap.merge(token.color(), 1, Integer::sum);
            }

            Vector<ArcExpression> numberOfExpressions = new Vector<>();
            numberOfMap.entrySet().stream()
            .sorted((e1, e2) -> e1.getKey().toString().compareTo(e2.getKey().toString()))
            .forEach(numberOfEntry -> {
                Color color = numberOfEntry.getKey();
                int number = numberOfEntry.getValue();
                Vector<ColorExpression> colorExpressions = new Vector<>();
                if (color.getColorType().equals(ColorType.COLORTYPE_DOT)) {
                    colorExpressions.add(new DotConstantExpression());
                } else if (color.getColorType().isProductColorType()) {
                    ProductType pt = (ProductType)color.getColorType();
                    Vector<Color> subColors = color.getTuple();
                    Vector<ColorExpression> subColorExpressions = new Vector<>();
                    subColorExpressions.addAll(subColors.stream()
                                                        .map(UserOperatorExpression::new)
                                                        .collect(Collectors.toList()));

                    colorExpressions.add(new TupleExpression(subColorExpressions, pt));
                } else {
                    colorExpressions.add(new UserOperatorExpression(color));
                }

                numberOfExpressions.add(new NumberOfExpression(number, colorExpressions));
            });

            ArcExpression tokenExpression = new AddExpression(numberOfExpressions);
            place.updateTokens(tokens, tokenExpression);
            placeComponent.setUnderlyingPlace(place);
        }
    }

    private void updateBindings(int stepIdx) {
        resetBindings();
        if (stepIdx < actionHistory.size() && stepIdx >= 0) {
            TAPNNetworkTraceStep step = actionHistory.get(stepIdx);
            if (step.isColoredTransitionStep()) {
                TAPNNetworkColoredTransitionStep coloredStep = (TAPNNetworkColoredTransitionStep)step;
                TimedTransition transition = coloredStep.getTransition();
                Transition guiTransition = null;
                for (Template template : tab.activeTemplates()) {
                    guiTransition = template.guiModel().getTransitionByName(transition.name());
                    if (guiTransition != null) break;
                }

                Map<Variable, Color> bindings = coloredStep.getBindings();
                guiTransition.setToolTipText(ColorBindingParser.createTooltip(bindings));
                reloadTooltip(guiTransition);
            }
        }
    }

    private void resetBindings() {
        for (Template template : tab.activeTemplates()) {
            for (Transition guiTransition : template.guiModel().transitions()) {
                guiTransition.setToolTipText(null);
                reloadTooltip(guiTransition);
            }
        }
    }

    private void reloadTooltip(Transition transition) {
        Point mousePos = MouseInfo.getPointerInfo().getLocation();
        SwingUtilities.convertPointFromScreen(mousePos, transition);
        
        boolean mouseOver = transition.contains(mousePos) && 
                            transition.isShowing() && 
                            transition.isVisible();
        if (mouseOver) {
            ToolTipManager manager = ToolTipManager.sharedInstance();
            long time = System.currentTimeMillis();
            manager.mouseMoved(new MouseEvent(transition, -1, time, 0, mousePos.x, mousePos.y, 0, false));
        }
    }

    /**
     * Make the selected transition in the animation box blink, based on the
     * list element label
     */
    public void blinkSelected(String label){
        if(label.contains(".")){
            label = label.split("\\.")[1];
        }

        Transition t = activeGuiModel().getTransitionByName(label);
        if(t != null){
            t.blink();
        }
    }

    public void dFireTransition(TimedTransition transition) {
        if (tab.getLens().isColored()) {
            fireColoredTransition(transition);
            return;
        }

        if (!TAPAALGUI.getAppGui().isShowingDelayEnabledTransitions() || isUrgentTransitionEnabled()){
            fireTransition(transition);
            return;
        }

        TimeInterval dInterval = transition.getdInterval();

        BigDecimal delayGranularity = tab.getDelayEnabledTransitionControl().getValue();
        //Make sure the granularity is small enough
        BigDecimal lowerBound = IntervalOperations.getRatBound(dInterval.lowerBound()).getBound();
        if(!dInterval.isLowerBoundNonStrict() && !dInterval.isIncluded(lowerBound.add(delayGranularity))){
            do{
                delayGranularity = delayGranularity.divide(BigDecimal.TEN);
            } while (delayGranularity.compareTo(new BigDecimal("0.00001")) >= 0 && !dInterval.isIncluded(lowerBound.add(delayGranularity)));
        }

        if(delayGranularity.compareTo(new BigDecimal("0.00001")) < 0){
            JOptionPane.showMessageDialog(TAPAALGUI.getApp(), "<html>Due to the limit of only five decimal points in the simulator</br> its not possible to fire the transition</html>");
        } else {
            BigDecimal delay = tab.getDelayEnabledTransitionControl().getDelayMode().GetDelay(transition, dInterval, delayGranularity);
            if(delay != null){
                if(delay.compareTo(BigDecimal.ZERO) != 0){ //Don't delay if the chosen delay is 0
                    if(!letTimePass(delay)){
                        return;
                    }
                }

                fireTransition(transition);
            }
        }
    }

    private void fireColoredTransition(TimedTransition transition) {
        if (!validBindingsMap.containsKey(transition)) return;
        
        if (trace != null && trace.isColoredTrace()) {
            if (isColoredTransitionEnabled(transition)) {
                if (currentAction < actionHistory.size() - 1) {
                    TAPNNetworkTraceStep nextStep = actionHistory.get(currentAction + 1);
                    if (nextStep.isColoredTransitionStep()) {
                        TAPNNetworkColoredTransitionStep coloredStep = (TAPNNetworkColoredTransitionStep)nextStep;
                        if (coloredStep.getTransition().equals(transition)) {
                            stepForward();
                            return;
                        }
                    }
                }

                int fireTransition = JOptionPane.showConfirmDialog(TAPAALGUI.getApp(),
                        "Are you sure you want to fire a transition which does not follow the colored trace?\n"
                            + "Firing this transition will discard the colored trace and revert to standard simulation.",
                        "Discarding Colored Trace", JOptionPane.YES_NO_OPTION );

                if (fireTransition == JOptionPane.NO_OPTION) {
                    return;
                }

                removeSetTrace(false);
            } else {
                return;
            }
        }

        if (isUsingInteractiveEngine) {
            Map<Variable, Color> bindings = new HashMap<>(); 
            var validBindings = validBindingsMap.get(transition);
            if (SimulationControl.getInstance().randomSimulation() && !validBindings.isEmpty()) {
                Random random = new Random();
                int randomIndex = random.nextInt(validBindings.size());
                bindings = validBindings.get(randomIndex);
            } else if (!validBindings.isEmpty()){
                bindings = ColoredBindingSelectionDialog.showDialog(transition, validBindings);
                if (bindings == null) return; // Cancelled
            }

            if (!clearStepsForward()) return;
            
            NetworkMarking newMarking;

            newMarking = interactiveEngine.sendTransition(transition, bindings);
            
            addMarking(new TAPNNetworkColoredTransitionStep(transition, bindings, newMarking), newMarking);

            updateColoredMarking();
            activeGuiModel().repaintPlaces();
            unhighlightDisabledTransitions();
            updateFireableTransitions();

            updateAnimationButtonsEnabled();
        }
    }

    // TODO: Clean up this method
    private void fireTransition(TimedTransition transition) {

        if(!clearStepsForward()){
            return;
        }

        Tuple<NetworkMarking, List<TimedToken>> next = null;
        List<TimedToken> tokensToConsume = null;
        try{
            if (getFiringmode() != null) {
                next = currentMarking().fireTransition(transition, getFiringmode());
            } else {
                tokensToConsume = getTokensToConsume(transition);
                if(tokensToConsume == null) return; // Cancelled
                next = new Tuple<NetworkMarking, List<TimedToken>> (currentMarking().fireTransition(transition, tokensToConsume), tokensToConsume);
            }
        }catch(RequireException e){
            JOptionPane.showMessageDialog(TAPAALGUI.getApp(), "There was an error firing the transition. Reason: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // It is important that this comes after the above, since
        // cancelling the token selection dialogue above should not result in changes
        // to the untimed animation history
        if (isDisplayingUntimedTrace){
            AnimationHistoryList untimedAnimationHistory = tab.getUntimedAnimationHistory();
            if(untimedAnimationHistory.isStepForwardAllowed()){
                String nextFromUntimedTrace = untimedAnimationHistory.getElement(untimedAnimationHistory.getSelectedIndex()+1);

                if(nextFromUntimedTrace.equals(transition.model().name() + "." + transition.name()) || transition.isShared() && nextFromUntimedTrace.equals(transition.name())){
                    untimedAnimationHistory.stepForward();
                }else{
                    int fireTransition = JOptionPane.showConfirmDialog(TAPAALGUI.getApp(),
                        "Are you sure you want to fire a transition which does not follow the untimed trace?\n"
                            + "Firing this transition will discard the untimed trace and revert to standard simulation.",
                        "Discarding Untimed Trace", JOptionPane.YES_NO_OPTION );

                    if (fireTransition == JOptionPane.NO_OPTION){
                        return;
                    }else{
                        removeSetTrace(false);
                    }
                }
            }
        }

        tab.network().setMarking(next.value1());

        activeGuiModel().repaintPlaces();
        unhighlightDisabledTransitions();
        updateFireableTransitions();

        addMarking(new TAPNNetworkTimedTransitionStep(transition, next.value2()), next.value1());

        updateAnimationButtonsEnabled();
        updateMouseOverInformation();
        reportBlockingPlaces();

    }

    public boolean letTimePass(BigDecimal delay) {

        if(!clearStepsForward()){
            return false;
        }

        boolean result = false;
        if (delay.compareTo(new BigDecimal(0))==0 || (currentMarking().isDelayPossible(delay) && !isUrgentTransitionEnabled)) {
            NetworkMarking delayedMarking = currentMarking().delay(delay);
            tab.network().setMarking(delayedMarking);
            addMarking(new TAPNNetworkTimeDelayStep(delay), delayedMarking);
            result = true;
        }

        activeGuiModel().repaintPlaces();
        unhighlightDisabledTransitions();
        updateFireableTransitions();

        updateAnimationButtonsEnabled();
        updateMouseOverInformation();
        reportBlockingPlaces();
        return result;
    }

    public void reportBlockingPlaces() {

        try {
            BigDecimal delay = tab.getAnimationController().getCurrentDelay();

            if (isUrgentTransitionEnabled && !delay.equals(BigDecimal.ZERO) ) {
                tab.getAnimationController().getOkButton().setEnabled(false);

                StringBuilder sb = new StringBuilder();
                sb.append("<html>Time delay is disabled due to the<br /> following enabled urgent transitions:<br /><br />");
                for (Template template : tab.activeTemplates()) {
                    for (Transition t : template.guiModel().transitions()) {
                        if (t.isTransitionEnabled() && template.model().getTransitionByName(t.getName()).isUrgent()) {
                            sb.append(template + "." + t.getName() + "<br />");
                        }
                    }
                }
                sb.append("</html>");

                tab.getAnimationController().getOkButton().setToolTipText(sb.toString());

                return;
            }

            if (delay.compareTo(new BigDecimal(0)) < 0) {
                tab.getAnimationController().getOkButton().setEnabled(false);
                tab.getAnimationController().getOkButton().setToolTipText("Time delay is possible only for nonnegative rational numbers");
            } else {
                List<TimedPlace> blockingPlaces = currentMarking().getBlockingPlaces(delay);
                if (blockingPlaces.size() == 0) {
                    tab.getAnimationController().getOkButton().setEnabled(true);
                    tab.getAnimationController().getOkButton().setToolTipText("Press to add the delay");
                } else {
                    StringBuilder sb = new StringBuilder();
                    sb.append("<html>Time delay of " + delay + " time unit(s) is disabled due to <br /> age invariants in the following places:<br /><br />");
                    for (TimedPlace t : blockingPlaces) {
                        sb.append(t.toString() + "<br />");
                    }
                    //JOptionPane.showMessageDialog(null, sb.toString());
                    sb.append("</html>");
                    tab.getAnimationController().getOkButton().setEnabled(false);
                    tab.getAnimationController().getOkButton().setToolTipText(sb.toString());
                }
            }
        } catch (ParseException | NumberFormatException e) {
            tab.getAnimationController().getOkButton().setEnabled(false);
            tab.getAnimationController().getOkButton().setToolTipText("The text in the input field is not a number");
        }
    }

    private DataLayer activeGuiModel() {
        return tab.currentTemplate().guiModel();
    }

    private void resethistory() {
        actionHistory.clear();
        markings.clear();
        currentAction = -1;
        currentMarkingIndex = 0;
        tab.getAnimationHistorySidePanel().reset();
        if(tab.getUntimedAnimationHistory() != null){
            tab.getUntimedAnimationHistory().reset();
        }
    }

    private void resetHistoryForTracechange() {
        actionHistory.clear();
        currentAction = -1;
        currentMarkingIndex = 0;
        tab.getAnimationHistorySidePanel().reset();
        if(tab.getUntimedAnimationHistory() != null){
            tab.getUntimedAnimationHistory().reset();
        }
    }

    public FiringMode getFiringmode() {
        return firingmode;
    }

    // removes stored markings and actions from index "startWith" (included)
    private void removeStoredActions(int startWith) {
        int lastIndex = actionHistory.size() - 1;
        for (int i = startWith; i <= lastIndex; i++) {
            removeLastHistoryStep();
        }
    }

    private void addMarking(TAPNNetworkTraceStep action, NetworkMarking marking) {
        if (currentAction < actionHistory.size() - 1) {
            removeStoredActions(currentAction + 1);
        }

        tab.network().setMarking(marking);
        tab.getAnimationHistorySidePanel().addHistoryItem(action.toString());
        if (action.isColoredTransitionStep()) {
            TAPNNetworkColoredTransitionStep coloredStep = (TAPNNetworkColoredTransitionStep)action;
            Map<Variable, Color> bindings = coloredStep.getBindings();
            tab.getAnimationHistorySidePanel().setTooltipForSelectedItem(ColorBindingParser.createTooltip(bindings));
        }

        actionHistory.add(action);
        markings.add(marking);
        currentAction++;
        currentMarkingIndex++;
    }

    private void removeLastHistoryStep() {
            actionHistory.remove(actionHistory.size() - 1);
        markings.remove(markings.size() - 1);
    }

    //XXX: should be enum?
    public static final String[] FIRINGMODES = { "Random", "Oldest", "Youngest", "Manual" };
    public void setFiringmode(String t) {
        switch (t) {
            case "Random":
                firingmode = new RandomFiringMode();
                break;
            case "Youngest":
                firingmode = new YoungestFiringMode();
                break;
            case "Oldest":
                firingmode = new OldestFiringMode();
                break;
            case "Manual":
                firingmode = null;
                break;
            default:
                System.err
                    .println("Illegal firing mode mode: " + t + " not found.");
                break;
        }

        tab.getAnimationController().updateFiringModeComboBox();
        tab.getAnimationController().setToolTipText("Select a method for choosing tokens during transition firing");
    }

    public boolean hasNonZeroTrance() {
        return tab.getAnimationHistorySidePanel().getListModel().size() > 1;
    }

    enum FillListStatus{
        lessThanWeight,
        weight,
        moreThanWeight
    }

    //Creates a list of tokens if there is only weight tokens in each of the places
    //Used by getTokensToConsume
    private  FillListStatus fillList(TimedTransition transition, List<TimedToken> listToFill){
        for(TimedInputArc in: transition.getInputArcs()){
            List<TimedToken> elligibleTokens = in.getElligibleTokens();
            if(elligibleTokens.size() < in.getWeight().value()){
                return FillListStatus.lessThanWeight;
            } else if(elligibleTokens.size() == in.getWeight().value()){
                listToFill.addAll(elligibleTokens);
            } else {
                return FillListStatus.moreThanWeight;
            }
        }
        for(TransportArc in: transition.getTransportArcsGoingThrough()){
            List<TimedToken> elligibleTokens = in.getElligibleTokens();
            if(elligibleTokens.size() < in.getWeight().value()){
                return FillListStatus.lessThanWeight;
            } else if(elligibleTokens.size() == in.getWeight().value()){
                listToFill.addAll(elligibleTokens);
            } else {
                return FillListStatus.moreThanWeight;
            }
        }
        return FillListStatus.weight;
    }

    private List<TimedToken> getTokensToConsume(TimedTransition transition){
        //If there are only "weight tokens in each place
        List<TimedToken> result = new ArrayList<TimedToken>();
        boolean userShouldChoose = false;
        if(transition.isShared()){
            for(TimedTransition t : transition.sharedTransition().transitions()){
                FillListStatus status = fillList(t, result);
                if(status == FillListStatus.lessThanWeight){
                    return null;
                } else if(status == FillListStatus.moreThanWeight){
                    userShouldChoose = true;
                    break;
                }
            }
        } else {
            FillListStatus status = fillList(transition, result);
            if(status == FillListStatus.lessThanWeight){
                return null;
            } else if(status == FillListStatus.moreThanWeight){
                userShouldChoose = true;
            }
        }

        if (userShouldChoose){
            return showSelectSimulatorDialogue(transition);
        } else {
            return result;
        }
    }

    private List<TimedToken> showSelectSimulatorDialogue(TimedTransition transition) {
        EscapableDialog guiDialog = new EscapableDialog(TAPAALGUI.getApp(), "Select Tokens", true);

        Container contentPane = guiDialog.getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
        AnimationTokenSelectDialog animationSelectmodeDialog = new AnimationTokenSelectDialog(transition);
        contentPane.add(animationSelectmodeDialog);
        guiDialog.setResizable(true);

        // Make window fit contents' preferred size
        guiDialog.pack();

        // Move window to the middle of the screen
        guiDialog.setLocationRelativeTo(null);
        guiDialog.setVisible(true);

        return animationSelectmodeDialog.getTokens();
    }

    public void resetForTraceChange() {
        resetHistoryForTracechange();
        removeSetTrace(false);
        markings.clear();
        markings.add(initialMarking);
        currentAction = -1;
    }

    public void reset(boolean keepInitial){
        resethistory();
        removeSetTrace(false);
        if(keepInitial && initialMarking != null){
            markings.add(initialMarking);
            tab.network().setMarking(initialMarking);
            currentAction = -1;
            updateFireableTransitions();
        }

        if(traceMap != null) {
            tab.getAnimationController().resetTraceBox(true);
        }

    }

    public void resetTraceBox() {
        if(tab.getAnimationController().getTraceBox().getModel().getSize() > 0) {
            tab.getAnimationController().resetTraceBox(true);
        }
    }

    private boolean removeSetTrace(boolean askUser){
        if(askUser && isShowingTrace()){ //Warn about deleting trace
            int answer = JOptionPane.showConfirmDialog(TAPAALGUI.getApp(),
                "You are about to remove the current trace.",
                "Removing Trace", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            if(answer != JOptionPane.OK_OPTION) return false;
        }
        if(isDisplayingUntimedTrace){
            tab.removeAbstractAnimationPane();
        }
        isDisplayingUntimedTrace = false;
        trace = null;
        return true;
    }

    public TAPNNetworkTrace getTrace(){
        return trace; 
    }

    private boolean clearStepsForward(){
        boolean answer = true;
        if(!isDisplayingUntimedTrace){
            answer = removeSetTrace(true);
        }

        if(answer){
            tab.getAnimationHistorySidePanel().clearStepsForward();
        } else if (SimulationControl.getInstance().isRunning()) {
            SimulationControl.stopSimulation();
        }
        return answer;
    }

    private boolean isShowingTrace(){
        return isDisplayingUntimedTrace || trace != null;
    }

    public ArrayList<TAPNNetworkTraceStep> getActionHistory() {
        return actionHistory;
    }


    private void setEnabledStepbackwardAction(boolean b) {
        stepbackwardAction.setEnabled(b);
    }

    private void setEnabledStepforwardAction(boolean b) {
        stepforwardAction.setEnabled(b);
    }

    public final GuiAction stepforwardAction = TAPAALGUI.getAppGui().stepforwardAction;
    public final GuiAction stepbackwardAction = TAPAALGUI.getAppGui().stepbackwardAction;

    public void updateAnimationButtonsEnabled() {
        AnimationHistoryList animationHistory = tab.getAnimationHistorySidePanel();

        setEnabledStepforwardAction(animationHistory.isStepForwardAllowed());
        setEnabledStepbackwardAction(animationHistory.isStepBackAllowed());

    }

    /**
     * Updates the mouseOver label showing token ages in animationmode
     * when a "animation" action is happening. "live updates" any mouseOver label
     */
    private void updateMouseOverInformation() {
        // update mouseOverView
        for (Place p : tab.getModel().getPlaces()) {
            if (((TimedPlaceComponent) p).isAgeOfTokensShown()) {
                ((TimedPlaceComponent) p).showAgeOfTokens(true);
            }
        }
    }
}
