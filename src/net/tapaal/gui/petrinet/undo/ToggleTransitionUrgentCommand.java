package net.tapaal.gui.petrinet.undo;

import pipe.gui.petrinet.PetriNetTab;
import dk.aau.cs.model.tapn.TimedTransition;

public class ToggleTransitionUrgentCommand extends Command {
	private final TimedTransition transition;
	private final boolean oldValue;
	private final PetriNetTab tab;
	
	public ToggleTransitionUrgentCommand(TimedTransition transition, PetriNetTab tab){
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
