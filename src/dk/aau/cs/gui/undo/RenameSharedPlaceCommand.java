package dk.aau.cs.gui.undo;

import pipe.dataLayer.TAPNQuery;
import dk.aau.cs.TCTL.visitors.RenameSharedPlaceVisitor;
import dk.aau.cs.gui.SharedPlacesAndTransitionsPanel.SharedPlacesListModel;
import dk.aau.cs.gui.TabContent;
import dk.aau.cs.model.tapn.SharedPlace;

public class RenameSharedPlaceCommand extends Command {
	private final SharedPlace place;
	private final String oldName;
	private final String newName;
	private final TabContent tab;
	private final SharedPlacesListModel listModel;

	public RenameSharedPlaceCommand(SharedPlace place, SharedPlacesListModel model, TabContent tab, String oldName, String newName) {
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
