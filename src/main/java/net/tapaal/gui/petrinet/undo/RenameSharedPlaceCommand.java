package net.tapaal.gui.petrinet.undo;

import net.tapaal.gui.petrinet.verification.TAPNQuery;
import dk.aau.cs.TCTL.visitors.RenameSharedPlaceVisitor;
import net.tapaal.gui.petrinet.editor.SharedPlacesAndTransitionsPanel.SharedPlacesListModel;
import pipe.gui.petrinet.PetriNetTab;
import dk.aau.cs.model.tapn.SharedPlace;

public class RenameSharedPlaceCommand implements Command {
	private final SharedPlace place;
	private final String oldName;
	private final String newName;
	private final PetriNetTab tab;
	private final SharedPlacesListModel listModel;

	public RenameSharedPlaceCommand(SharedPlace place, SharedPlacesListModel model, PetriNetTab tab, String oldName, String newName) {
		this.place = place;
		listModel = model;
		this.tab = tab;
		this.oldName = oldName;
		this.newName = newName;
	}

	@Override
	public void redo() {
		place.setName(newName);
		updateQueries(oldName, newName);
		listModel.updatedName();
	}

	private void updateQueries(String nameToFind, String nameToReplaceWith) {
		for(TAPNQuery query : tab.queries()){
			query.getProperty().accept(new RenameSharedPlaceVisitor(nameToFind, nameToReplaceWith), null);
		}
	}

	@Override
	public void undo() {
		place.setName(oldName);
		updateQueries(newName, oldName);
		listModel.updatedName();
	}

}
