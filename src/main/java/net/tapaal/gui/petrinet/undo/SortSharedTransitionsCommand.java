package net.tapaal.gui.petrinet.undo;

import net.tapaal.gui.petrinet.editor.SharedPlacesAndTransitionsPanel.SharedTransitionsListModel;
import dk.aau.cs.model.tapn.SharedTransition;

public class SortSharedTransitionsCommand implements Command {
	
	final SharedTransitionsListModel listModel;
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
