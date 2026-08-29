package net.tapaal.gui.petrinet.undo;

import net.tapaal.gui.petrinet.editor.SharedPlacesAndTransitionsPanel.SharedPlacesListModel;
import dk.aau.cs.model.tapn.SharedPlace;

public class DeleteSharedPlaceCommand implements Command {
	private final SharedPlace place;
	private final SharedPlacesListModel listModel;
	
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
