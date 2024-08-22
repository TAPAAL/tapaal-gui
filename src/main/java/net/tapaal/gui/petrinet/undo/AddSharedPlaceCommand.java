package net.tapaal.gui.petrinet.undo;

import net.tapaal.gui.petrinet.editor.SharedPlacesAndTransitionsPanel.SharedPlacesListModel;
import dk.aau.cs.model.tapn.SharedPlace;

public class AddSharedPlaceCommand implements Command {
	private final SharedPlace place;
	private final SharedPlacesListModel listModel;
	
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
