package pipe.gui.petrinet.animation;

import java.awt.Container;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JOptionPane;

import dk.aau.cs.model.tapn.simulation.*;
import net.tapaal.gui.petrinet.animation.AnimationTokenSelectDialog;
import pipe.gui.petrinet.dataLayer.DataLayer;
import net.tapaal.gui.petrinet.Template;
import pipe.gui.TAPAALGUI;
import pipe.gui.petrinet.action.GuiAction;
import pipe.gui.petrinet.graphicElements.Transition;
import pipe.gui.petrinet.graphicElements.Place;
import pipe.gui.petrinet.graphicElements.tapn.TimedPlaceComponent;
import pipe.gui.swingcomponents.EscapableDialog;
import pipe.gui.petrinet.PetriNetTab;
import net.tapaal.gui.petrinet.animation.TransitionFiringComponent;
import dk.aau.cs.model.tapn.NetworkMarking;
import dk.aau.cs.model.tapn.TimeInterval;
import dk.aau.cs.model.tapn.TimedInputArc;
import dk.aau.cs.model.tapn.TimedOutputArc;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedToken;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.model.tapn.TransportArc;
import dk.aau.cs.util.IntervalOperations;
import dk.aau.cs.util.RequireException;
import dk.aau.cs.util.Tuple;
import dk.aau.cs.verification.VerifyTAPN.TraceType;

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

    public static boolean isUrgentTransitionEnabled(){
        return isUrgentTransitionEnabled;
    }

    public Animator(PetriNetTab tab) {
        super();

        this.tab = tab;
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
        tab.setAnimationMode(true);

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
        if (getTrace().getTraceType() != TraceType.NOT_EG) { //If the trace was not explicitly set, maybe we have calculated it is deadlock.
            tab.getAnimationHistorySidePanel().setLastShown(getTrace().getTraceType());
        }
    }

    private void setColoredTrace(TAPNNetworkTrace trace) {
        NetworkMarking previousMarking = initialMarking;
        TimedTransition previousTransition = null;
        for (TAPNNetworkTraceStep step : trace) {
            TimedTransition transition = ((TAPNNetworkColoredTransitionStep)step).getTransition();
            if (previousMarking != null && previousTransition != null) {
                checkTokensRemoved(previousMarking, previousTransition);
            }

            previousMarking = currentMarking();
            previousTransition = transition;

            addMarking(step, ((TAPNNetworkColoredTransitionStep)step).getMarking());
        }
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

    public void updateFireableTransitions(){
        TransitionFiringComponent transFireComponent = tab.getTransitionFiringComponent();
        transFireComponent.startReInit();
        isUrgentTransitionEnabled = false;

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
    }

    /**
     * Restores model at end of animation and sets all transitions to false and
     * unhighlighted
     */
    public void restoreModel() {
        if (tab != null) {
            disableTransitions();
            tab.network().setMarking(initialMarking);
            currentAction = -1;
        }
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
            tab.network().setMarking(markings.get(currentMarkingIndex - 1));

            activeGuiModel().repaintPlaces();
            unhighlightDisabledTransitions();
            updateFireableTransitions();
            currentAction--;
            currentMarkingIndex--;

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
        if(currentAction == actionHistory.size()-1 && trace != null){
            int selectedIndex = tab.getAnimationHistorySidePanel().getSelectedIndex();
            int action = currentAction;
            int markingIndex = currentMarkingIndex;

            if(getTrace().getTraceType() == TraceType.EG_DELAY_FOREVER){
                addMarking(new TAPNNetworkTimeDelayStep(BigDecimal.ONE), currentMarking().delay(BigDecimal.ONE));
            }
            if(getTrace().getLoopToIndex() != -1){
                addToTimedTrace(getTrace().getLoopSteps());
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
            tab.network().setMarking(markings.get(currentMarkingIndex + 1));

            activeGuiModel().repaintPlaces();
            unhighlightDisabledTransitions();
            updateFireableTransitions();
            currentAction++;
            currentMarkingIndex++;
            activeGuiModel().redrawVisibleTokenLists();

            updateAnimationButtonsEnabled();
            updateMouseOverInformation();
            reportBlockingPlaces();
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

    public void dFireTransition(TimedTransition transition){
        if(!TAPAALGUI.getAppGui().isShowingDelayEnabledTransitions() || isUrgentTransitionEnabled()){
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
                        "Discrading Untimed Trace", JOptionPane.YES_NO_OPTION );

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

    public TimedTAPNNetworkTrace getTrace(){
        return (TimedTAPNNetworkTrace)trace;
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
