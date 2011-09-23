package dk.aau.cs.gui.undo;

import dk.aau.cs.model.tapn.SharedTransition;

public class RenameSharedTransitionCommand extends Command {
	private final SharedTransition transition;
	private final String oldName;
	private final String newName;

	public RenameSharedTransitionCommand(SharedTransition transition, String oldName, String name) {
		this.transition = transition;
		this.oldName = oldName;
		newName = name;
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
