package dk.aau.cs.gui.undo;

import pipe.gui.CreateGui;
import dk.aau.cs.model.tapn.TimedTransition;

public class ToggleTransitionUrgent extends Command {
	private final TimedTransition transition;
	private final boolean oldValue;
	
	public ToggleTransitionUrgent(TimedTransition transition){
		this.transition = transition;
		oldValue = transition.isUrgent();
	}
	
	@Override
	public void redo() {
		transition.setUrgent(!oldValue);
		CreateGui.getCurrentTab().repaint();
	}

	@Override
	public void undo() {
		transition.setUrgent(oldValue);
		CreateGui.getCurrentTab().repaint();
	}

}
