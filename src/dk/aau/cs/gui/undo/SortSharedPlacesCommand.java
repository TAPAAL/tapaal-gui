package dk.aau.cs.gui.undo;

import net.tapaal.gui.editor.SharedPlacesAndTransitionsPanel.SharedPlacesListModel;
import dk.aau.cs.model.tapn.SharedPlace;

public class SortSharedPlacesCommand extends Command {

	final SharedPlacesListModel listModel;
	SharedPlace[] oldOrder;
	
	public SortSharedPlacesCommand(SharedPlacesListModel listModel) {
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
