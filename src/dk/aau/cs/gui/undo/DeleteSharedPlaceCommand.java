package dk.aau.cs.gui.undo;

import dk.aau.cs.gui.SharedPlacesAndTransitionsPanel.SharedPlacesListModel;
import dk.aau.cs.model.tapn.SharedPlace;

public class DeleteSharedPlaceCommand extends Command {
	private SharedPlace place;
	private SharedPlacesListModel listModel;
	
	public DeleteSharedPlaceCommand(SharedPlace place, SharedPlacesListModel listModel){
		this.place = place;
		this.listModel = listModel;
	}
	
	@Override
	public void redo() {
		listModel.removeElement(place);
	}

	@Override
	public void undo() {
		listModel.addElement(place);
	}
}
