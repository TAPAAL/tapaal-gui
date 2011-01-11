package pipe.gui;

import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;


/** Class to represent the history of the net animation
 *
 * @author Pere Bonet changed and added a number of new functions
 * to fix the unexpected behaviour observed during animation playback.
 * Previously, under certain circumstances, it is possible to step back to state(s)
 * before the initial state and step forward to state(s) after the last recorded
 * transitions in the animation history. 
 * These actions are no longer allowed after the fix.
 * */
public class AnimationHistory
extends JList {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4284885450021683552L;
	/** Holds all the transitions in the sequence */


	public AnimationHistory() throws 
	javax.swing.text.BadLocationException {
		super();
		setModel(new DefaultListModel());
		getListModel().addElement("Initial Marking");
		setSelectedIndex(0);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		for(MouseListener listener : getMouseListeners()){ removeMouseListener(listener); }
		for(MouseMotionListener listener : getMouseMotionListeners()){ removeMouseMotionListener(listener); }
		for(KeyListener listener : getKeyListeners()){ removeKeyListener(listener); }
	}


	public void addHistoryItem(String transitionName) {
		getListModel().addElement(transitionName);
		setSelectedIndex(getListModel().size()-1);
	}

	public void addHistoryItemDontChange(String transitionName) {
		getListModel().addElement(transitionName);
	}


	public void clearStepsForward() {
		DefaultListModel listModel = getListModel();
		int lastIndex = listModel.size()-1;
		
		if(listModel.size() > 1 && getSelectedIndex() < lastIndex){
			listModel.removeRange(getSelectedIndex()+1, lastIndex);
		}
	}


	public void stepForward() {
		if (isStepForwardAllowed()) {
			int nextIndex = getSelectedIndex()+1;
			setSelectedIndex(nextIndex);
		}
	}


	public void stepBackwards() {
		if (isStepBackAllowed()){
			int indexToMoveTo = getSelectedIndex()-1;
			setSelectedIndex(indexToMoveTo);
		}
	}


	public boolean isStepForwardAllowed(){
		return getSelectedIndex() < getListModel().size()-1;
	}


	public boolean isStepBackAllowed(){
		return getSelectedIndex() > 0;
	}

	public int getCurrentItem(){
		return getSelectedIndex();
	}

	public String getElement(int i){
		return (String)getListModel().get(i);   
	}


	DefaultListModel getListModel(){
		return (DefaultListModel)getModel();
	}
	
	@Override
	public void setSelectedIndex(int index) {
		super.setSelectedIndex(index);
		ensureIndexIsVisible(index);
	}
}
