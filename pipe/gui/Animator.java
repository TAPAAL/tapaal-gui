package pipe.gui;

import java.awt.Container;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.TimedTransitionComponent;
import pipe.dataLayer.Transition;
import pipe.gui.widgets.AnimationSelectmodeDialog;
import pipe.gui.widgets.EscapableDialog;
import dk.aau.cs.gui.TabContent;
import dk.aau.cs.model.tapn.NetworkMarking;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.model.tapn.simulation.FiringMode;
import dk.aau.cs.model.tapn.simulation.OldestFiringMode;
import dk.aau.cs.model.tapn.simulation.RandomFiringMode;
import dk.aau.cs.model.tapn.simulation.TapaalTrace;
import dk.aau.cs.model.tapn.simulation.TapaalTraceStep;
import dk.aau.cs.model.tapn.simulation.TimeDelayStep;
import dk.aau.cs.model.tapn.simulation.TimedTransitionStep;
import dk.aau.cs.model.tapn.simulation.YoungestFiringMode;


/**
 * This class is used to process clicks by the user to manually step 
 * through enabled transitions in the net. 
 * 
 * @author unspecified 	wrote this code
 * @author David Patterson fixed a bug with double-firing transitions
 *         in the doRandomFiring method. I also renamed the fireTransition
 *         method to recordFiredTransition to better describe what it does.
 *
 * @author Pere Bonet modified the recordFiredTransition method to
 * fix the unexcepted behaviour observed during animation playback.
 * The method is renamed back to fireTransition. 
 * 
 * @author Edwin Chung fixed the bug where users can still step forward to 
 * previous firing sequence even though it has been reset. The issue where an 
 * unexpected behaviour will occur when the firing sequence has been altered 
 * has been resolved. The problem where animation will freeze halfway while 
 * stepping back a firing sequence has also been fixed (Feb 2007) 
 *
 * @author Dave Patterson The code now outputs an error message in the status 
 * bar if there is no transition to be found when picking a random transition 
 * to fire. This is related to the problem described in bug 1699546.
 * 
 * @author Joakim Byg Edited the code so that it can animate time passing in 
 * TAPN. This include refactoring lastFiredTransition so that it also 
 * contains timePasses. The rest of the code is altered so that it takes 
 * this into account (count is renamed to currentAction). (Feb 2009)
 */

public class Animator {
	private ArrayList<TapaalTraceStep> actionHistory;
	private int currentAction;
	private ArrayList<NetworkMarking> markings;
	private int currentMarkingIndex = 0;

	public FiringMode firingmode = new RandomFiringMode();
	private TabContent tab;
	private NetworkMarking initialMarking;

	public Animator(){
		actionHistory = new ArrayList<TapaalTraceStep>();
		currentAction = -1;
		markings = new ArrayList<NetworkMarking>();
	}

	public void setTabContent(TabContent tab){
		this.tab = tab;
	}

	private NetworkMarking currentMarking(){
		return markings.get(currentMarkingIndex);
	}

	public void SetTrace(TapaalTrace trace){
		if(trace.isConcreteTrace()){
			setTimedTrace(trace);
		}else{
			setUntimedTrace(trace);
		}
		currentAction = -1;
		currentMarkingIndex = 0;
		CreateGui.getAnimationHistory().setSelectedIndex(0);
		CreateGui.getAnimationController().setAnimationButtonsEnabled();
	}

	private void setUntimedTrace(TapaalTrace trace) {
		//		CreateGui.addAbstractAnimationPane();
		//		AnimationHistoryComponent untimedAnimationHistory = CreateGui.getAbstractAnimationPane();
		//		for(FiringAction action : trace){
		//			String transitionName = action instanceof ColoredDiscreteFiringAction ? ((ColoredDiscreteFiringAction)action).getTransition().getName() : ((DiscreetFiringAction)action).getTransition().getName();
		//			untimedAnimationHistory.addHistoryItemDontChange(transitionName);
		//		}
		//		setFiringmode("Manual");
		//
				JOptionPane.showMessageDialog(CreateGui.getApp(),
						"The verification process returned an untimed trace.\n\n"+
						"This means that with appropriate time delays the displayed\n"+
						"sequence of discrete transitions can become a concrete trace.\n"+
						"In case of liveness properties (EG, AF) the untimed trace\n"+
						"either ends in a deadlock, or time divergent computation without\n" +
						"any discrete transitions, or it loops back to some earlier configuration.\n"+
						"The user may experiment in the simulator with different time delays\n"+
						"in order to realize the suggested untimed trace in the model.",
						"Verification Information",
						JOptionPane.INFORMATION_MESSAGE);
	}

	private void setTimedTrace(TapaalTrace trace) {
		for(TapaalTraceStep step : trace){
			addMarking(step, step.performStepFrom(currentMarking()));
		}
	}


	/**
	 * Highlights enabled transitions
	 */
	public void highlightEnabledTransitions(){
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
	public void unhighlightDisabledTransitions(){
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
	private void disableTransitions(){
		Iterator<Transition> transitionIterator = activeGuiModel().returnTransitions();
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
	public void storeModel(){
		initialMarking = tab.network().marking();
		resethistory();
		markings.add(initialMarking);
	}


	/**
	 * Restores model at end of animation and sets all transitions to false and 
	 * unhighlighted
	 */
	public void restoreModel(){
		disableTransitions();
		tab.network().setMarking(initialMarking);
		currentAction = -1;
	}





	/**
	 * Steps back through previously fired transitions
	 * @author jokke refactored and added backwards firing for TAPNTransitions
	 */
	public void stepBack(){

		if ( ! actionHistory.isEmpty() ){
			//
			//			if (actionHistory.get(currentAction) instanceof DiscreetFiringAction){
			//				TimedTransitionComponent transition = (TimedTransitionComponent)((DiscreetFiringAction)actionHistory.get(currentAction)).getTransition(); // XXX - unsafe cast
			//				HashMap<TimedPlaceComponent, ArrayList<BigDecimal>> markingToGoBackTo = markingHistory.get(currentAction);
			//
			//				if (markingToGoBackTo == null){
			//					System.err.println("No marking to go back to, ERROR!");
			//				}
			//
			//				HashMap<TimedPlaceComponent, ArrayList<BigDecimal>> presetMarking = new HashMap<TimedPlaceComponent, ArrayList<BigDecimal>>();
			//				for (Arc a : transition.getPreset() ){
			//					TimedPlaceComponent place = (TimedPlaceComponent)a.getSource();
			//					presetMarking.put(place, (ArrayList<BigDecimal>)markingToGoBackTo.get(place).clone());
			//				}
			//
			//				HashMap<TimedPlaceComponent, ArrayList<BigDecimal>> postsetMarking = new HashMap<TimedPlaceComponent, ArrayList<BigDecimal>>();
			//				for (Arc a : transition.getPostset() ){
			//					TimedPlaceComponent place = (TimedPlaceComponent)a.getTarget();
			//					postsetMarking.put(place, (ArrayList<BigDecimal>)markingToGoBackTo.get(place).clone());
			//				}
			//
			//				CreateGui.currentPNMLData().fireTimedTransitionBackwards(presetMarking, postsetMarking, transition);
			//
			//				//If untimed simulation
			//				if (CreateGui.getAbstractAnimationPane() != null){
			//
			//					AnimationHistory untimedAnimationHistory = CreateGui.getAbstractAnimationPane();
			//					int current = untimedAnimationHistory.getCurrentItem();
			//					if ((untimedAnimationHistory.getElement(current-1)).trim().equals(transition.getName())){ //Possible null pointer exception
			//						//It is fired
			//						untimedAnimationHistory.stepBackwards();	 
			//					}
			//
			//				}
			//
			//			}else if(actionHistory.get(currentAction) instanceof ColoredDiscreteFiringAction){
			//				CreateGui.currentPNMLData().fireColoredTransitionBackwards((ColoredDiscreteFiringAction)actionHistory.get(currentAction));
			//			}else if ( actionHistory.get(currentAction) instanceof TimeDelayFiringAction){
			//				BigDecimal timeDelay = ((TimeDelayFiringAction)actionHistory.get(currentAction)).getDealy();
			//				try {
			//					tab.network().setMarking(markings.get(currentMarkingIndex-1));//CreateGui.getModel().letTimePass(timeDelay.negate());
			//				} catch (RequireException e) {
			//					e.printStackTrace();
			//				}
			//			}else{
			//				System.err.println("problem in Animator");
			//			}

			tab.network().setMarking(markings.get(currentMarkingIndex-1));

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
	public void stepForward(){
		if ( currentAction < actionHistory.size()-1 ) {
			tab.network().setMarking(markings.get(currentMarkingIndex+1));
			//			if ( actionHistory.get(currentAction+1) instanceof DiscreetFiringAction){
			//				TimedTransitionComponent nextTransition = (TimedTransitionComponent)((DiscreetFiringAction)actionHistory.get(currentAction+1)).getTransition(); // XXX - unsafe cast
			//
			//				// Before we firer the transition we need to setup Select firing mode, and 
			//				// firer the transition with the same tokens as in the DiscreetFiringAction
			//				// IT IS A HACK TO USE FIRING MODEL SELECT TO THIS !!! -- kyrke
			//
			//				//Setup firingmode
			//
			//				//If this marking is not saved in marking history (e.g. its a uppaal trace)
			//
			////				if (markingHistory.get(currentAction+1)==null){
			////					Logger.log("Marking is null, we will fix it");
			////
			////					HashMap<TimedPlaceComponent, ArrayList<BigDecimal>> currentmakring = CreateGui.currentPNMLData().getCurrentMarking();
			////					markingHistory.set(currentAction+1, currentmakring);
			////				}
			////
			////				HashMap<Place, ArrayList<BigDecimal>> consumedTokens = ((DiscreetFiringAction)actionHistory.get(currentAction+1)).getConsumedTokensList();
			////
			////				CreateGui.currentPNMLData().fireTransition(nextTransition, consumedTokens);
			////
			//////				CreateGui.currentPNMLData().setEnabledTransitions();
			//////				unhighlightDisabledTransitions();
			//////				highlightEnabledTransitions();
			//////				currentAction++;
			////
			////				//If untimed simulation
			////				if (CreateGui.getAbstractAnimationPane() != null){
			////
			////					AnimationHistoryComponent untimedAnimationHistory = CreateGui.getAbstractAnimationPane();
			////					int current = untimedAnimationHistory.getCurrentItem();
			////					if ((untimedAnimationHistory.getElement(current)).trim().equals(nextTransition.getName())){ //Possible null pointer exception
			////						//It is fired
			////						untimedAnimationHistory.stepForward();
			////					}
			////
			////				}
			//				tab.network().setMarking(markings.get(currentMarkingIndex+1));
			//
			//			}else if(actionHistory.get(currentAction+1) instanceof ColoredDiscreteFiringAction){
			//				ColoredDiscreteFiringAction action = (ColoredDiscreteFiringAction)actionHistory.get(currentAction+1);
			//				CreateGui.currentPNMLData().fireTransition(action);
			//			}else if (actionHistory.get(currentAction+1) instanceof TimeDelayFiringAction){
			//				BigDecimal timeDelay = ((TimeDelayFiringAction)actionHistory.get(currentAction+1)).getDealy();
			//
			//				//If this marking is not saved in marking history (e.g. its a uppaal trace)
			//
			//				if (markings.get(currentMarkingIndex+1) == null){
			//					Logger.log("Marking is null, we will fix it");
			//					markings.set(currentMarkingIndex+1, currentMarking().delay(timeDelay));
			//				}
			//
			//				try {
			//					tab.network().setMarking(markings.get(currentMarkingIndex+1));
			//				} catch (RequireException e) {
			//					// XXX - kyrke, An error can con come here as this is valid states stored in animator 
			//					e.printStackTrace();
			//				}
			//				
			//			}

			activeGuiModel().repaintPlaces();
			unhighlightDisabledTransitions();
			highlightEnabledTransitions();
			currentAction++;
			currentMarkingIndex++;
			activeGuiModel().redrawVisibleTokenLists();
		}
	}

	/** This method keeps track of a fired transition in the AnimationHistory 
	 * object, enables transitions after the recent firing, and properly displays 
	 * the transitions.
	 * 
	 * @author David Patterson renamed this method and changed the 
	 * AnimationHandler to make it fire the transition before calling this method.
	 * This prevents double-firing a transition.
	 * 
	 * @author Pere Bonet modified this method so that it now stores transitions 
	 * that has just been fired in an array so that it can be accessed during 
	 * backwards and stepping to fix the unexcepted behaviour observed during 
	 * animation playback.
	 * The method is renamed back to fireTransition.
	 * 
	 * 
	 */
	public void fireTransition(Transition transition){
		//		HashMap<TimedPlaceComponent, ArrayList<BigDecimal>> currentmakring = CreateGui.currentPNMLData().getCurrentMarking();
		//
		//		//If untimed simulation
		//		if (CreateGui.getAbstractAnimationPane() != null){
		//
		//			AnimationHistory untimedAnimationHistory = CreateGui.getAbstractAnimationPane();
		//			int current = untimedAnimationHistory.getCurrentItem();
		//			if ((untimedAnimationHistory.getElement(current)).trim().equals(transition.getName())){ //Possible null pointer exception
		//				//It is fired
		//				untimedAnimationHistory.stepForward();	 
		//			}else{
		//				int fireTransition = JOptionPane.showConfirmDialog( CreateGui.getApp().getRootPane(),
		//						"Are you sure you want to fire a transition which does not follow the untimed trace?\n" +
		//						"Firing this transition will discard the untimed trace and revert to standard simulation.",
		//						"Discrading Untimed Trace",
		//						JOptionPane.YES_NO_OPTION );
		//				if (fireTransition > 0){
		//					return;
		//				}else{
		//					CreateGui.removeAbstractAnimationPane();
		//					CreateGui.updateLeftPanel();
		//				}
		//			}
		//		}
		TimedTransition timedTransition = ((TimedTransitionComponent)transition).underlyingTransition();
		NetworkMarking next = null;
		if(getFiringmode() != null){
			next = currentMarking().fireTransition(timedTransition, firingmode);
		}else{
			throw new RuntimeException("Not implemented");
		}

		addMarking(new TimedTransitionStep(timedTransition, null), next);
		tab.network().setMarking(currentMarking());

		activeGuiModel().repaintPlaces();
		highlightEnabledTransitions();
		unhighlightDisabledTransitions();

	}

	public void letTimePass(BigDecimal timeToPass) {
		if (canTimePass(timeToPass)){						
			addMarking(new TimeDelayStep(timeToPass), currentMarking().delay(timeToPass));
			tab.network().setMarking(currentMarking());
		}

		activeGuiModel().repaintPlaces();
		highlightEnabledTransitions();
		unhighlightDisabledTransitions();
	}

	private DataLayer activeGuiModel() {
		return tab.activeTemplate().guiModel();
	}

	private boolean canTimePass(BigDecimal delay){
		return currentMarking().isDelayPossible(delay);
	}

	public void resethistory(){
		actionHistory.clear();
		markings.clear();
		currentAction = -1;
		currentMarkingIndex = 0;
	}

	public FiringMode getFiringmode() {
		return firingmode;
	}


	//removes stored markings and actions from index "startWith" (included)
	private void removeStoredActions(int startWith) {
		int lastIndex = actionHistory.size()-1;
		for (int i=startWith; i<=lastIndex; i++){
			removeLastHistoryStep();
		}
	}

	private void addMarking(TapaalTraceStep action, NetworkMarking marking){
		if ( currentAction < actionHistory.size()-1 ) removeStoredActions(currentAction+1);

		CreateGui.getAnimationHistory().addHistoryItem(action.toString());	
		actionHistory.add(action);
		markings.add(marking);
		currentAction++;
		currentMarkingIndex++;
	}


	private void removeLastHistoryStep(){
		actionHistory.remove(actionHistory.size()-1);
		//markingHistory.remove(markingHistory.size()-1);
		markings.remove(markings.size()-1);
	}



	public void setFiringmode(String t){

		if (t.equals("Random")){
			firingmode = new RandomFiringMode();
		} else if (t.equals("Youngest")){
			firingmode = new YoungestFiringMode();
		} else if (t.equals("Oldest")){
			firingmode = new OldestFiringMode();
		} else if (t.equals("Manual")){
			firingmode = null;
		} else {
			System.err.println("Illegal firing mode mode: " + t + " not found.");
		}

		CreateGui.getAnimationController().updateFiringModeComboBox();
	}

	public boolean showSelectSimulatorDialogue(Transition t){
		EscapableDialog guiDialog = 
			new EscapableDialog(CreateGui.getApp(), Pipe.getProgramName(), true);

		Container contentPane = guiDialog.getContentPane();


		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));      


		AnimationSelectmodeDialog animationSelectmodeDialog = new AnimationSelectmodeDialog(t); 
		contentPane.add(animationSelectmodeDialog);

		guiDialog.setResizable(true);     

		// Make window fit contents' preferred size
		guiDialog.pack();

		// Move window to the middle of the screen
		guiDialog.setLocationRelativeTo(null);
		guiDialog.setVisible(true);

		if(!animationSelectmodeDialog.cancelled()){
			ArrayList<Integer> intlist = new ArrayList<Integer>();
			for (JComboBox jb : animationSelectmodeDialog.presetPanels){
				intlist.add(jb.getSelectedIndex());
			}
			//((SelectFiringmode)firingmode).setTokensToFire(intlist);
		}

		return !animationSelectmodeDialog.cancelled();
	}
}
