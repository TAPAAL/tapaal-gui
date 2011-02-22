package dk.aau.cs.gui.undo;

import dk.aau.cs.model.tapn.TimedTransition;

public class RenameTimedTransition extends Command {

	private final TimedTransition transition;
	private final String oldName;
	private final String newName;

	public RenameTimedTransition(TimedTransition transition, String oldName, String newName) {
		this.transition = transition;
		this.oldName = oldName;
		this.newName = newName;
	}

	@Override
	public void redo() {
		transition.setName(newName);
	}

	@Override
	public void undo() {
		transition.setName(oldName);
	}

}
