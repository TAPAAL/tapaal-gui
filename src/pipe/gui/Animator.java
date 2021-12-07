package pipe.gui;

import java.awt.Container;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import pipe.dataLayer.DataLayer;
import pipe.dataLayer.Template;
import pipe.gui.action.GuiAction;
import pipe.gui.graphicElements.Transition;
import pipe.gui.graphicElements.tapn.TimedPlaceComponent;
import pipe.gui.widgets.AnimationTokenSelectDialog;
import pipe.gui.widgets.EscapableDialog;
import dk.aau.cs.gui.TabContent;
import dk.aau.cs.gui.components.TransitionFiringComponent;
import dk.aau.cs.model.tapn.NetworkMarking;
import dk.aau.cs.model.tapn.TimeInterval;
import dk.aau.cs.model.tapn.TimedInputArc;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedToken;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.model.tapn.TransportArc;
import dk.aau.cs.model.tapn.simulation.FiringMode;
import dk.aau.cs.model.tapn.simulation.OldestFiringMode;
import dk.aau.cs.model.tapn.simulation.RandomFiringMode;
import dk.aau.cs.model.tapn.simulation.TAPNNetworkTimeDelayStep;
import dk.aau.cs.model.tapn.simulation.TAPNNetworkTimedTransitionStep;
import dk.aau.cs.model.tapn.simulation.TAPNNetworkTrace;
import dk.aau.cs.model.tapn.simulation.TAPNNetworkTraceStep;
import dk.aau.cs.model.tapn.simulation.TimedTAPNNetworkTrace;
import dk.aau.cs.model.tapn.simulation.YoungestFiringMode;
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
    private final TabContent tab;
    private NetworkMarking initialMarking;

    private boolean isDisplayingUntimedTrace = false;
    private static boolean isUrgentTransitionEnabled = false;

    public static boolean isUrgentTransitionEnabled(){
        return isUrgentTransitionEnabled;
    }

    public Animator(TabContent tab) {
        super();

        this.tab = tab;
    }

    private NetworkMarking currentMarking() {
        return markings.get(currentMarkingIndex);
    }

    public void setTrace(TAPNNetworkTrace trace) {
        CreateGui.getCurrentTab().setAnimationMode(true);

        try {
            if (trace.isConcreteTrace()) {
                this.trace = trace;
                setTimedTrace(trace);
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
            CreateGui.getCurrentTab().toggleAnimationMode();
            JOptionPane.showMessageDialog(CreateGui.getApp(), "There was an error in the trace. Reason: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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

        JOptionPane.showMessageDialog(CreateGui.getApp(),
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
        for (TAPNNetworkTraceStep step : trace) {
            addMarking(step, step.performStepFrom(currentMarking()));
        }
        if (getTrace().getTraceType() != TraceType.NOT_EG) { //If the trace was not explicitly set, maybe we have calculated it is deadlock.
            tab.getAnimationHistorySidePanel().setLastShown(getTrace().getTraceType());
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

        for( Template template : tab.activeTemplates()){
            for (Transition t : template.guiModel().transitions()) {
                if (t.isTransitionEnabled()) {
                    t.markTransitionEnabled(true);
                    transFireComponent.addTransition(template, t);
                } else if (CreateGui.getApp().isShowingDelayEnabledTransitions() &&
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
        for(Template template : tab.allTemplates())
        {
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
        if(!CreateGui.getApp().isShowingDelayEnabledTransitions() || isUrgentTransitionEnabled()){
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
            JOptionPane.showMessageDialog(CreateGui.getApp(), "<html>Due to the limit of only five decimal points in the simulator</br> its not possible to fire the transition</html>");
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
            JOptionPane.showMessageDialog(CreateGui.getApp(), "There was an error firing the transition. Reason: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
                    int fireTransition = JOptionPane.showConfirmDialog(CreateGui.getApp(),
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
                            sb.append(template.toString() + "." + t.getName() + "<br />");
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
        EscapableDialog guiDialog = new EscapableDialog(CreateGui.getApp(), "Select Tokens", true);

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

    public void reset(boolean keepInitial){
        resethistory();
        removeSetTrace(false);
        if(keepInitial && initialMarking != null){
            markings.add(initialMarking);
            tab.network().setMarking(initialMarking);
            currentAction = -1;
            updateFireableTransitions();
        }
    }

    private boolean removeSetTrace(boolean askUser){
        if(askUser && isShowingTrace()){ //Warn about deleting trace
            int answer = JOptionPane.showConfirmDialog(CreateGui.getApp(),
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

    public final GuiAction stepforwardAction = CreateGui.getAppGui().stepforwardAction;
    public final GuiAction stepbackwardAction = CreateGui.getAppGui().stepbackwardAction;

    public void updateAnimationButtonsEnabled() {
        AnimationHistoryList animationHistory = CreateGui.getCurrentTab().getAnimationHistorySidePanel();

        setEnabledStepforwardAction(animationHistory.isStepForwardAllowed());
        setEnabledStepbackwardAction(animationHistory.isStepBackAllowed());

    }

    /**
     * Updates the mouseOver label showing token ages in animationmode
     * when a "animation" action is happening. "live updates" any mouseOver label
     */
    private void updateMouseOverInformation() {
        // update mouseOverView
        for (pipe.gui.graphicElements.Place p : CreateGui.getCurrentTab().getModel().getPlaces()) {
            if (((TimedPlaceComponent) p).isAgeOfTokensShown()) {
                ((TimedPlaceComponent) p).showAgeOfTokens(true);
            }
        }
    }

    public TabContent getTab(){
        return tab;
    }

}
