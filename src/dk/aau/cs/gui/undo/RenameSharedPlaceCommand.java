package dk.aau.cs.gui.undo;

import dk.aau.cs.model.tapn.SharedPlace;

public class RenameSharedPlaceCommand extends Command {
	private final SharedPlace place;
	private final String oldName;
	private final String newName;

	public RenameSharedPlaceCommand(SharedPlace place, String oldName, String newName) {
		this.place = place;
		this.oldName = oldName;
		this.newName = newName;
	}

	@Override
	public void redo() {
		place.setName(newName);
	}

	@Override
	public void undo() {
		place.setName(oldName);
	}

}
