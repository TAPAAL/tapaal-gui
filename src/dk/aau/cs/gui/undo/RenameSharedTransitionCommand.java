package dk.aau.cs.gui.undo;

import dk.aau.cs.gui.SharedPlacesAndTransitionsPanel.SharedTransitionsListModel;
import dk.aau.cs.model.tapn.SharedTransition;

public class RenameSharedTransitionCommand extends Command {
	private final SharedTransition transition;
	private final String oldName;
	private final String newName;
	private SharedTransitionsListModel listModel;

	public RenameSharedTransitionCommand(SharedTransition transition, String oldName, String name, SharedTransitionsListModel listModel) {
		this.transition = transition;
		this.oldName = oldName;
		this.newName = name;
		this.listModel = listModel;
	}

	@Override
	public void redo() {
		transition.setName(newName);
		listModel.updatedName();
	}

	@Override
	public void undo() {
		transition.setName(oldName);
		listModel.updatedName();
	}

}
