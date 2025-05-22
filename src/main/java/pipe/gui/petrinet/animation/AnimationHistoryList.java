package pipe.gui.petrinet.animation;

import java.awt.Component;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.ToolTipManager;

import net.tapaal.gui.petrinet.Template;
import pipe.gui.TAPAALGUI;
import pipe.gui.petrinet.graphicElements.Transition;

import dk.aau.cs.verification.VerifyTAPN.TraceType;

public class AnimationHistoryList extends JList<String> {

	private TraceType lastShown = TraceType.NOT_EG;
    private final Map<Integer, String> itemTooltips = new HashMap<>();

	public AnimationHistoryList() {
		super();
		setModel(new DefaultListModel<>());
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
        ToolTipManager.sharedInstance().registerComponent(this);
        for (MouseListener listener : getMouseListeners()) {
			if (!(listener.getClass().getName().contains("ToolTipManager"))) {
                removeMouseListener(listener);
            }
		}

		for (MouseMotionListener listener : getMouseMotionListeners()) {
			if (!(listener.getClass().getName().contains("ToolTipManager"))) {
                removeMouseMotionListener(listener);
            }
		}

		for (KeyListener listener : getKeyListeners()) {
			removeKeyListener(listener);
		}

        setCellRenderer(new TooltipListCellRenderer());
	}

	public void addHistoryItem(String transitionName) {
		if(lastShown == TraceType.NOT_EG){
			getListModel().addElement(transitionName);
			setSelectedIndex(getListModel().size() - 1);
		} else {
			getListModel().add(getListModel().size()-1, transitionName);
			setSelectedIndex(getListModel().size() - 2);
		}
		
		updateAccordingToDeadlock();
	}

	public void clearStepsForward() {
		DefaultListModel<String> listModel = getListModel();
		int lastIndex = listModel.size() - 1;

		if (listModel.size() > 1 && getSelectedIndex() < lastIndex) {
			listModel.removeRange(getSelectedIndex() + 1, lastIndex);
		}
		lastShown = TraceType.NOT_EG;
		updateAccordingToDeadlock();
	}

	public void stepForward() {
		if (isStepForwardAllowed()) {
			int nextIndex = getSelectedIndex() + 1;
			setSelectedIndex(nextIndex);
		}
		layoutAdjustment();
	}

	public void stepBackwards() {
		if (isStepBackAllowed()) {
			int indexToMoveTo = getSelectedIndex() - 1;
			setSelectedIndex(indexToMoveTo);
		}
	}

	public boolean isStepForwardAllowed() {
	    layoutAdjustment();
		if(lastShown != TraceType.EG_DEADLOCK){
			return getSelectedIndex() < getListModel().size() - 1;
		} else {
			return getSelectedIndex() < getListModel().size() - 2;
		}
	}

	public boolean isStepBackAllowed() {
		return getSelectedIndex() > 0;
	}

	public String getCurrentItem() {
		return getElement(getSelectedIndex());
	}

	public String getElement(int i) {
		return getListModel().get(i);
	}

	public DefaultListModel<String> getListModel() {
		return (DefaultListModel<String>) getModel();
	}

	@Override
	public void setSelectedIndex(int index) {
		super.setSelectedIndex(index);
		ensureIndexIsVisible(index);
	}

    public void setTooltipForIndex(int index, String tooltip) {
        if (index >= 0 && index < getListModel().size()) {
            itemTooltips.put(index, tooltip);
        }
    }

    public void setTooltipForSelectedItem(String tooltip) {
        setTooltipForIndex(getSelectedIndex(), tooltip);
    }

	public void reset() {
		getListModel().clear();
		getListModel().addElement("Initial Marking");
		setSelectedIndex(0);
		lastShown = TraceType.NOT_EG;
		updateAccordingToDeadlock();
        itemTooltips.clear();
	}
	
	static final private String deadlockString = "<html><i><font color=red>" + "Deadlock" + "</i></font></html>";
	static final private String delayForeverString = "<html><i><font color=red>" + "Delay forever" + "</i></font></html>";
	static final private String gotoString = "<html><i><font color=red>" + "Goto *" + "</i></font></html>";
	
	public void setLastShown(TraceType tracetype){
		if(lastShown != TraceType.NOT_EG){
            getListModel().remove(getListModel().size()-1);
		}
		lastShown = tracetype;
		
		switch (tracetype) {
		case EG_DEADLOCK:
			getListModel().addElement(deadlockString);
			break;
		case EG_DELAY_FOREVER:
			getListModel().addElement(delayForeverString);
                        break;
		case EG_LOOP:
			getListModel().addElement(gotoString);
		case NOT_EG:
			break;
		}
		layoutAdjustment();
	}

    private void layoutAdjustment() {
        // if the trace ends with "deadlock", "delay for ever" or "goto *" makes sure we don't have to scrool to see it
        int selectedIndex = getSelectedIndex();
        if (selectedIndex == getListModel().getSize() - 2) {
            setSelectedIndex(selectedIndex + 1);
            setSelectedIndex(selectedIndex);
        }
    }
	
	private void updateAccordingToDeadlock() {
		
		if(lastShown == TraceType.EG_DELAY_FOREVER){
			return;
		}
		for (Template t : TAPAALGUI.getCurrentTab().activeTemplates()){
			for(Transition trans : t.guiModel().getTransitions()){
				if(trans.isTransitionEnabled() || trans.isDelayEnabled()){
					return;
				}
			}
		}
		
		setLastShown(TraceType.EG_DEADLOCK);
	}

    private class TooltipListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (itemTooltips.containsKey(index)) {
                setToolTipText(itemTooltips.get(index));
            } else {
                setToolTipText(null);
            }
            
            return c;
        }
    }
}
