package pipe.gui;

import java.awt.Container;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JOptionPane;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.Transition;
import pipe.gui.widgets.AnimationSelectmodeDialog;
import pipe.gui.widgets.EscapableDialog;
import dk.aau.cs.gui.TabContent;
import dk.aau.cs.model.tapn.NetworkMarking;
import dk.aau.cs.model.tapn.TimedToken;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.model.tapn.simulation.FiringMode;
import dk.aau.cs.model.tapn.simulation.OldestFiringMode;
import dk.aau.cs.model.tapn.simulation.RandomFiringMode;
import dk.aau.cs.model.tapn.simulation.TAPNNetworkTrace;
import dk.aau.cs.model.tapn.simulation.TAPNNetworkTraceStep;
import dk.aau.cs.model.tapn.simulation.TAPNNetworkTimeDelayStep;
import dk.aau.cs.model.tapn.simulation.TAPNNetworkTimedTransitionStep;
import dk.aau.cs.model.tapn.simulation.YoungestFiringMode;
import dk.aau.cs.util.RequireException;

public class Animator {
	private ArrayList<TAPNNetworkTraceStep> actionHistory;
	private int currentAction;
	private ArrayList<NetworkMarking> markings;
	private int currentMarkingIndex = 0;

	public FiringMode firingmode = new RandomFiringMode();
	private TabContent tab;
	private NetworkMarking initialMarking;

	private boolean isDisplayingUntimedTrace = false;

	public Animator() {
		actionHistory = new ArrayList<TAPNNetworkTraceStep>();
		currentAction = -1;
		markings = new ArrayList<NetworkMarking>();
	}

	public void setTabContent(TabContent tab) {
		this.tab = tab;
	}

	private NetworkMarking currentMarking() {
		return markings.get(currentMarkingIndex);
	}

	public void SetTrace(TAPNNetworkTrace trace) {
		if (trace.isConcreteTrace()) {
			setTimedTrace(trace);
		} else {
			setUntimedTrace(trace);
			isDisplayingUntimedTrace = true;
		}
		currentAction = -1;
		currentMarkingIndex = 0;
		CreateGui.getAnimationHistory().setSelectedIndex(0);
		CreateGui.getAnimationController().setAnimationButtonsEnabled();
	}

	private void setUntimedTrace(TAPNNetworkTrace trace) {
		tab.addAbstractAnimationPane();
		AnimationHistoryComponent untimedAnimationHistory = CreateGui.getAbstractAnimationPane();

		for(TAPNNetworkTraceStep step : trace){
			untimedAnimationHistory.addHistoryItem(step.toString());
		}

		CreateGui.getAbstractAnimationPane().setSelectedIndex(0);
		setFiringmode("Manual");

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
	}

	public NetworkMarking getInitialMarking(){
		return initialMarking;
	}
	
	/**
	 * Highlights enabled transitions
	 */
	public void highlightEnabledTransitions() {
		DataLayer current = activeGuiModel();

		Iterator<Transition> transitionIterator = current.returnTransitions();
		while (transitionIterator.hasNext()) {
			Transition tempTransition = transitionIterator.next();
			if (tempTransition.isEnabled(true) == true) {
				current.notifyObservers();
				tempTransition.repaint();
			}
		}
	}

	/**
	 * Called during animation to unhighlight previously highlighted transitions
	 */
	public void unhighlightDisabledTransitions() {
		DataLayer current = activeGuiModel();

		Iterator<Transition> transitionIterator = current.returnTransitions();
		while (transitionIterator.hasNext()) {
			Transition tempTransition = transitionIterator.next();
			if (tempTransition.isEnabled(true) == false) {
				current.notifyObservers();
				tempTransition.repaint();
			}
		}
	}

	/**
	 * Called at end of animation and resets all Transitions to false and
	 * unhighlighted
	 */
	private void disableTransitions() {
		Iterator<Transition> transitionIterator = activeGuiModel()
		.returnTransitions();
		while (transitionIterator.hasNext()) {
			Transition tempTransition = transitionIterator.next();
			tempTransition.setEnabledFalse();
			activeGuiModel().notifyObservers();
			tempTransition.repaint();
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
		disableTransitions();
		tab.network().setMarking(initialMarking);
		currentAction = -1;
	}

	/**
	 * Steps back through previously fired transitions
	 * 
	 * @author jokke refactored and added backwards firing for TAPNTransitions
	 */

	public void stepBack() {
		if (!actionHistory.isEmpty()){
			TAPNNetworkTraceStep lastStep = actionHistory.get(currentAction);
			if(isDisplayingUntimedTrace && lastStep instanceof TAPNNetworkTimedTransitionStep){
				AnimationHistoryComponent untimedAnimationHistory = tab.getUntimedAnimationHistory();
				String previousInUntimedTrace = untimedAnimationHistory.getElement(untimedAnimationHistory.getSelectedIndex());
				if(previousInUntimedTrace.equals(lastStep.toString())){
					untimedAnimationHistory.stepBackwards();
				}
			}
			
			tab.network().setMarking(markings.get(currentMarkingIndex - 1));

			activeGuiModel().repaintPlaces();
			unhighlightDisabledTransitions();
			highlightEnabledTransitions();
			currentAction--;
			currentMarkingIndex--;
		}
	}

	/**
	 * Steps forward through previously fired transitions
	 */

	public void stepForward() {
		if (currentAction < actionHistory.size() - 1) {
			TAPNNetworkTraceStep nextStep = actionHistory.get(currentAction+1);
			if(isDisplayingUntimedTrace && nextStep instanceof TAPNNetworkTimedTransitionStep){
				AnimationHistoryComponent untimedAnimationHistory = tab.getUntimedAnimationHistory();
				String nextInUntimedTrace = untimedAnimationHistory.getElement(untimedAnimationHistory.getSelectedIndex()+1);
				if(nextInUntimedTrace.equals(nextStep.toString())){
					untimedAnimationHistory.stepForward();
				}
			}
			
			tab.network().setMarking(markings.get(currentMarkingIndex + 1));
			
			activeGuiModel().repaintPlaces();
			unhighlightDisabledTransitions();
			highlightEnabledTransitions();
			currentAction++;
			currentMarkingIndex++;
			activeGuiModel().redrawVisibleTokenLists();

		}
	}

	
	// TODO: Clean up this method
	public void fireTransition(TimedTransition transition) {
		NetworkMarking next = null;
		try{
			if (getFiringmode() != null) {
				next = currentMarking().fireTransition(transition, getFiringmode());
			} else {
				List<TimedToken> tokensToConsume = showSelectSimulatorDialogue(transition);
				if(tokensToConsume == null) return; // Cancelled
				next = currentMarking().fireTransition(transition, tokensToConsume);
			}
		}catch(RequireException e){
			JOptionPane.showMessageDialog(CreateGui.getApp(), "There was an error firing the transition. Reason: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		// It is important that this comes after the above, since 
		// cancelling the token selection dialogue above should not result in changes 
		// to the untimed animation history
		if (isDisplayingUntimedTrace){
			AnimationHistoryComponent untimedAnimationHistory = tab.getUntimedAnimationHistory();
			if(untimedAnimationHistory.isStepForwardAllowed()){
				String nextFromUntimedTrace = untimedAnimationHistory.getElement(untimedAnimationHistory.getSelectedIndex()+1);
				
				if(nextFromUntimedTrace.equals(transition.model().name() + "." + transition.name())){
					untimedAnimationHistory.stepForward();
				}else{
					int fireTransition = JOptionPane.showConfirmDialog(CreateGui.getApp(),
							"Are you sure you want to fire a transition which does not follow the untimed trace?\n"
							+ "Firing this transition will discard the untimed trace and revert to standard simulation.",
							"Discrading Untimed Trace", JOptionPane.YES_NO_OPTION );

					if (fireTransition == JOptionPane.NO_OPTION){
						return;
					}else{
						CreateGui.removeAbstractAnimationPane();
						isDisplayingUntimedTrace = false;
					}
				}
			}
		}
		
		addMarking(new TAPNNetworkTimedTransitionStep(transition, null), next);
		tab.network().setMarking(currentMarking());

		activeGuiModel().repaintPlaces();
		highlightEnabledTransitions();
		unhighlightDisabledTransitions();

	}

	public void letTimePass(BigDecimal delay) {
		if (currentMarking().isDelayPossible(delay)) {
			addMarking(new TAPNNetworkTimeDelayStep(delay), currentMarking().delay(delay));
			tab.network().setMarking(currentMarking());
		}

		activeGuiModel().repaintPlaces();
		highlightEnabledTransitions();
		unhighlightDisabledTransitions();
	}

	private DataLayer activeGuiModel() {
		return tab.activeTemplate().guiModel();
	}

	public void resethistory() {
		actionHistory.clear();
		markings.clear();
		currentAction = -1;
		currentMarkingIndex = 0;
		tab.getAnimationHistory().reset();
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
		if (currentAction < actionHistory.size() - 1)
			removeStoredActions(currentAction + 1);

		CreateGui.getAnimationHistory().addHistoryItem(action.toString());
		actionHistory.add(action);
		markings.add(marking);
		currentAction++;
		currentMarkingIndex++;
	}

	private void removeLastHistoryStep() {
		actionHistory.remove(actionHistory.size() - 1);
		markings.remove(markings.size() - 1);
	}

	public void setFiringmode(String t) {
		if (t.equals("Random")) {
			firingmode = new RandomFiringMode();
		} else if (t.equals("Youngest")) {
			firingmode = new YoungestFiringMode();
		} else if (t.equals("Oldest")) {
			firingmode = new OldestFiringMode();
		} else if (t.equals("Manual")) {
			firingmode = null;
		} else {
			System.err
			.println("Illegal firing mode mode: " + t + " not found.");
		}

		CreateGui.getAnimationController().updateFiringModeComboBox();
	}

	public List<TimedToken> showSelectSimulatorDialogue(TimedTransition transition) {
		EscapableDialog guiDialog = new EscapableDialog(CreateGui.getApp(), Pipe.getProgramName(), true);

		Container contentPane = guiDialog.getContentPane();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
		AnimationSelectmodeDialog animationSelectmodeDialog = new AnimationSelectmodeDialog(transition);
		contentPane.add(animationSelectmodeDialog);
		guiDialog.setResizable(true);

		// Make window fit contents' preferred size
		guiDialog.pack();

		// Move window to the middle of the screen
		guiDialog.setLocationRelativeTo(null);
		guiDialog.setVisible(true);

		return animationSelectmodeDialog.getTokens();
	}

	public void reset(){
		resethistory();
		isDisplayingUntimedTrace = false;
	}
}
