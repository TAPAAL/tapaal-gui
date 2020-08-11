package dk.aau.cs.gui.undo;

import dk.aau.cs.gui.TabContent;
import dk.aau.cs.model.tapn.TimedTransition;

public class ToggleTransitionUrgent extends Command {
	private final TimedTransition transition;
	private final boolean oldValue;
	private final TabContent tab;
	
	public ToggleTransitionUrgent(TimedTransition transition, TabContent tab){
		this.transition = transition;
		oldValue = transition.isUrgent();
		this.tab = tab;
	}
	
	@Override
	public void redo() {
		transition.setUrgent(!oldValue);
		tab.repaint();
	}

	@Override
	public void undo() {
		transition.setUrgent(oldValue);
		tab.repaint();
	}

}
