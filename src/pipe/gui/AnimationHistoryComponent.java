package pipe.gui;

import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;

import dk.aau.cs.model.tapn.simulation.TAPNNetworkTrace;
import dk.aau.cs.model.tapn.simulation.TimedTAPNNetworkTrace;

public class AnimationHistoryComponent extends JList {
	private static final long serialVersionUID = -4284885450021683552L;

	public AnimationHistoryComponent() {
		super();
		setModel(new DefaultListModel());
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		reset();
		
		for (MouseListener listener : getMouseListeners()) {
			removeMouseListener(listener);
		}
		
		for (MouseMotionListener listener : getMouseMotionListeners()) {
			removeMouseMotionListener(listener);
		}
		for (KeyListener listener : getKeyListeners()) {
			removeKeyListener(listener);
		}
	}

	public void addHistoryItem(String transitionName) {
		getListModel().addElement(transitionName);
		setSelectedIndex(getListModel().size() - 1);
	}

	public void addHistoryItemDontChange(String transitionName) {
		getListModel().addElement(transitionName);
	}

	public void clearStepsForward() {
		CreateGui.getAnimator().nullSetTrace();
		DefaultListModel listModel = getListModel();
		int lastIndex = listModel.size() - 1;

		if (listModel.size() > 1 && getSelectedIndex() < lastIndex) {
			listModel.removeRange(getSelectedIndex() + 1, lastIndex);
		}
	}

	public void stepForward() {
		if (isStepForwardAllowed()) {
			int nextIndex = getSelectedIndex() + 1;
			setSelectedIndex(nextIndex);
		}
	}

	public void stepBackwards() {
		if (isStepBackAllowed()) {
			int indexToMoveTo = getSelectedIndex() - 1;
			setSelectedIndex(indexToMoveTo);
		}
	}

	public boolean isStepForwardAllowed() {
		return getSelectedIndex() < getListModel().size() - 1;
	}

	public boolean isStepBackAllowed() {
		return getSelectedIndex() > 0;
	}

	public String getCurrentItem() {
		return getElement(getSelectedIndex());
	}

	public String getElement(int i) {
		return (String) getListModel().get(i);
	}

	DefaultListModel getListModel() {
		return (DefaultListModel) getModel();
	}

	@Override
	public void setSelectedIndex(int index) {
		super.setSelectedIndex(index);
		ensureIndexIsVisible(index);
	}

	public void reset() {
		getListModel().clear();
		getListModel().addElement("Initial Marking");
		setSelectedIndex(0);
	}
}
