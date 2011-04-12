package dk.aau.cs.gui.undo;

import dk.aau.cs.gui.SharedPlacesAndTransitionsPanel.SharedPlacesListModel;
import dk.aau.cs.model.tapn.SharedPlace;

public class AddSharedPlaceCommand extends Command {
	private SharedPlace place;
	private SharedPlacesListModel listModel;
	
	public AddSharedPlaceCommand(SharedPlacesListModel listModel, SharedPlace place){
		this.listModel = listModel;
		this.place = place;
	}
	
	@Override
	public void redo() {
		listModel.addElement(place);
	}

	@Override
	public void undo() {
		listModel.removeElement(place);
	}
}
