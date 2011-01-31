package pipe.gui;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.Timer;

import pipe.dataLayer.ColoredDiscreteFiringAction;
import pipe.dataLayer.DataLayer;
import pipe.dataLayer.DiscreetFiringAction;
import pipe.dataLayer.FiringAction;
import pipe.dataLayer.TAPNTrace;
import pipe.dataLayer.TimeDelayFiringAction;
import pipe.dataLayer.TimedPlaceComponent;
import pipe.dataLayer.TimedTransitionComponent;
import pipe.dataLayer.Transition;
import pipe.exception.InvariantViolatedAnimationException;
import pipe.gui.widgets.AnimationSelectmodeDialog;
import pipe.gui.widgets.EscapableDialog;
import dk.aau.cs.gui.TabContent;
import dk.aau.cs.model.tapn.NetworkMarking;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.model.tapn.simulation.FiringMode;
import dk.aau.cs.model.tapn.simulation.OldestFiringMode;
import dk.aau.cs.model.tapn.simulation.RandomFiringMode;
import dk.aau.cs.model.tapn.simulation.YoungestFiringMode;
import dk.aau.cs.util.RequireException;


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

	Timer timer;
	int numberSequences;   
	private ArrayList<FiringAction> actionHistory;
	private int currentAction;
	private ArrayList<HashMap<TimedPlaceComponent, ArrayList<BigDecimal>>> markingHistory;
	private ArrayList<NetworkMarking> markings;
	private int currentMarkingIndex = 0;

	public FiringMode firingmode = new RandomFiringMode();
	private TabContent tab;
	private NetworkMarking initialMarking;
	
	public Animator(){
		actionHistory = new ArrayList<FiringAction>();

		timer = new Timer(0, new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if ((getNumberSequences() < 1) ||
						!CreateGui.getView().isInAnimationMode()) {
					timer.stop();
					CreateGui.getApp().setRandomAnimationMode(false);
					return;
				}
				doRandomFiring();
				setNumberSequences(getNumberSequences() - 1);
			}
		});
		currentAction = -1;
		markingHistory = new ArrayList<HashMap<TimedPlaceComponent,ArrayList<BigDecimal>>>();
		markings = new ArrayList<NetworkMarking>();
	}
	
	public void setTabContent(TabContent tab){
		this.tab = tab;
	}

	private NetworkMarking currentMarking(){
		return markings.get(currentMarkingIndex);
	}
	
	public void SetTrace(TAPNTrace trace){
		if(trace.isConcreteTrace()){
			setTimedTrace(trace);
		}else{
			setUntimedTrace(trace);
		}
	}

	private void setUntimedTrace(TAPNTrace trace) {
		CreateGui.addAbstractAnimationPane();
		AnimationHistoryComponent untimedAnimationHistory = CreateGui.getAbstractAnimationPane();
		for(FiringAction action : trace){
			String transitionName = action instanceof ColoredDiscreteFiringAction ? ((ColoredDiscreteFiringAction)action).getTransition().getName() : ((DiscreetFiringAction)action).getTransition().getName();
			untimedAnimationHistory.addHistoryItemDontChange(transitionName);
		}
		setFiringmode("Manual");

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

	private void setTimedTrace(TAPNTrace trace) {
		for(FiringAction action : trace){
			if(action instanceof TimeDelayFiringAction)
				manipulatehistory(((TimeDelayFiringAction)action).getDealy());
			else if(action instanceof DiscreetFiringAction){
				manipulatehistory((DiscreetFiringAction)action);
			}else if(action instanceof ColoredDiscreteFiringAction){
				manipulatehistory((ColoredDiscreteFiringAction)action);
			}
		}
	}


	/**
	 * Highlights enabled transitions
	 */
	public void highlightEnabledTransitions(){
		/* rewritten by wjk 03/10/2007 */
		DataLayer current = activeGuiModel();//CreateGui.currentPNMLData();

		//current.setEnabledTransitions();      

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
		DataLayer current = activeGuiModel();//CreateGui.currentPNMLData();

		//current.setEnabledTransitions();      

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
			//CreateGui.currentPNMLData().returnTransitions();
		while (transitionIterator.hasNext()) {
			Transition tempTransition = transitionIterator.next();
			tempTransition.setEnabledFalse();
			CreateGui.currentPNMLData().notifyObservers();
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
		//CreateGui.setupModelForSimulation();
		//CreateGui.currentPNMLData().storeState();
	}


	/**
	 * Restores model at end of animation and sets all transitions to false and 
	 * unhighlighted
	 */
	public void restoreModel(){
		//CreateGui.restoreModelForEditing();
		//CreateGui.currentPNMLData().restoreState();
		disableTransitions();
		tab.network().setMarking(initialMarking);
		currentAction = -1;
	}


	public void startRandomFiring(){      
		if (getNumberSequences() > 0) {
			// stop animation
			setNumberSequences(0);
		} else {
			try {
				String s = JOptionPane.showInputDialog(
						"Enter number of firings to perform", "1");
				this.numberSequences=Integer.parseInt(s);
				s = JOptionPane.showInputDialog(
						"Enter time delay between firing /ms", "50");
				timer.setDelay(Integer.parseInt(s));
				timer.start();
			} catch (NumberFormatException e) {
				CreateGui.getApp().setRandomAnimationMode(false);
			}
		}
	}


	public void stopRandomFiring() {
		numberSequences = 0;
	}


	/**
	 * This method randomly fires one of the enabled transitions. It then records 
	 * the information about this by calling the recordFiredTransition method.
	 * 
	 * @author Dave Patterson Apr 29, 2007
	 * I changed the code to keep the random transition found by the DataLayer.
	 * If it is not null, I call the fireTransition method, otherwise I put 
	 * out an error message in the status bar. 
	 */
	public void doRandomFiring() {
		DataLayer data = CreateGui.currentPNMLData();
		Transition t = data.fireRandomTransition(); //revisar
		//CreateGui.getAnimationHistory().clearStepsForward(); //ok - igual
		//removeStoredTransitions(); //ok - igual
		if (t != null) {
			fireTransition(t); //revisar
			//unhighlightDisabledTransitions();
			//highlightEnabledTransitions();
		} else {
			CreateGui.getApp().getStatusBar().changeText( 
			"ERROR: No transition to fire." );
		}
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
			CreateGui.currentPNMLData().setEnabledTransitions();
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
			activeGuiModel().setEnabledTransitions();
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
		if(firingmode != null){
			next = currentMarking().fireTransition(timedTransition, firingmode);
		}else{
			throw new RuntimeException("Not implemented");
		}
		
		CreateGui.getAnimationHistory().addHistoryItem(timedTransition.model().getName() + "." + timedTransition.name());
		if ( currentAction < actionHistory.size()-1 ) removeStoredActions(currentAction+1);
		
		addMarking(new DiscreetFiringAction(transition), next);
		
//		FiringAction fired;
//		fired = CreateGui.currentPNMLData().fireTransition(transition);
//
//
//		if ( currentAction < actionHistory.size()-1 ){
//			removeStoredActions(currentAction+1);
//			addToHistory( fired,  currentmakring);
//		}else{
//			addToHistory( fired, currentmakring );  
//		}
		tab.network().setMarking(currentMarking());
		activeGuiModel().repaintPlaces();
		activeGuiModel().setEnabledTransitions();
		highlightEnabledTransitions();
		unhighlightDisabledTransitions();

	}

	public void letTimePass(BigDecimal timeToPass) throws InvariantViolatedAnimationException {
		if (canTimePass(timeToPass)){//guiModel.canTimePass(timeToPass)){
			if ( currentAction < actionHistory.size()-1 ){
				removeStoredActions(currentAction+1);
			}
			
			//addToHistory( new TimeDelayFiringAction(timeToPass), guiModel.getCurrentMarking() );  
			addMarking(new TimeDelayFiringAction(timeToPass), currentMarking().delay(timeToPass));

			try {

				//Catch exception and dont add history, throw exception again 
				// to alow handling further on. 
				//Stripping too long decimals

				//This is a trick to get locals (, vs .) right in the text box.
				DecimalFormat df = new DecimalFormat();
				df.setMaximumFractionDigits(Pipe.AGE_DECIMAL_PRECISION);
				df.setMinimumFractionDigits(Pipe.AGE_DECIMAL_PRECISION);

				BigDecimal strippedTimeDelay = new BigDecimal(timeToPass.toString(), new MathContext(Pipe.AGE_PRECISION));
				
				//guiModel.letTimePass(strippedTimeDelay);
				tab.network().setMarking(currentMarking());
				CreateGui.getAnimationHistory().addHistoryItem("Time delay: "+ df.format(strippedTimeDelay));


			} catch (RequireException e) {
				throw e;
			} 



			

			activeGuiModel().repaintPlaces();
			activeGuiModel().setEnabledTransitions();
			highlightEnabledTransitions();
			unhighlightDisabledTransitions();
		}


	}

	private DataLayer activeGuiModel() {
		return tab.activeTemplate().guiModel();
	}
	
	private boolean canTimePass(BigDecimal delay){
		return currentMarking().isDelayPossible(delay);
	}

	public void resethistory(){
		markingHistory.clear();
		actionHistory.clear();
		markings.clear();
		currentAction = -1;
		currentMarkingIndex = 0;
	}

	public void manipulatehistory(ColoredDiscreteFiringAction dfa){


		markingHistory.add(null);

		actionHistory.add(dfa); // newAction = the transition to fire
		CreateGui.getAnimationHistory().addHistoryItemDontChange(dfa.getTransition().getName()); 

		CreateGui.getAnimationController().setAnimationButtonsEnabled();
	}

	public void manipulatehistory(DiscreetFiringAction dfa){


		markingHistory.add(null);

		actionHistory.add(dfa); // newAction = the transition to fire
		CreateGui.getAnimationHistory().addHistoryItemDontChange(dfa.getTransition().getName()); 

		CreateGui.getAnimationController().setAnimationButtonsEnabled();
	}
	public void manipulatehistory(BigDecimal delay){
		markingHistory.add(null); 
		actionHistory.add(new TimeDelayFiringAction(delay)); 

		CreateGui.getAnimationHistory().addHistoryItemDontChange("Time delay: "+ delay);

		CreateGui.getAnimationController().setAnimationButtonsEnabled();
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

	public synchronized int getNumberSequences() {
		return numberSequences;
	}

	public synchronized void setNumberSequences(int numberSequences) {
		this.numberSequences = numberSequences;
	}

	private void addMarking(FiringAction action, NetworkMarking marking){
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
