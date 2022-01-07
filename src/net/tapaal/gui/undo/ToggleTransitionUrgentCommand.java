package net.tapaal.gui.undo;

import pipe.gui.TabContent;
import dk.aau.cs.model.tapn.TimedTransition;

public class ToggleTransitionUrgentCommand extends Command {
	private final TimedTransition transition;
	private final boolean oldValue;
	private final TabContent tab;
	
	public ToggleTransitionUrgentCommand(TimedTransition transition, TabContent tab){
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
