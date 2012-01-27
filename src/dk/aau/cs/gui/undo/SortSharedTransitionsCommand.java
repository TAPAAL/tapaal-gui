package dk.aau.cs.gui.undo;

import dk.aau.cs.gui.SharedPlacesAndTransitionsPanel.SharedPlacesListModel;
import dk.aau.cs.gui.SharedPlacesAndTransitionsPanel.SharedTransitionsListModel;
import dk.aau.cs.model.tapn.SharedPlace;
import dk.aau.cs.model.tapn.SharedTransition;

public class SortSharedTransitionsCommand extends Command {
	
	SharedTransitionsListModel listModel;
	SharedTransition[] oldOrder;
	
	public SortSharedTransitionsCommand(SharedTransitionsListModel listModel) {
		this.listModel = listModel;
	}
	
	@Override
	public void undo() {
		listModel.undoSort(oldOrder);
	}

	@Override
	public void redo() {
		oldOrder = listModel.sort();
	}
}
