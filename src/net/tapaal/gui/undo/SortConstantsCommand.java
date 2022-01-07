package net.tapaal.gui.undo;

import net.tapaal.gui.editor.ConstantsPane;
import pipe.gui.PetriNetTab;
import dk.aau.cs.model.tapn.Constant;

public class SortConstantsCommand extends Command{
	
	final PetriNetTab tabContent;
	final ConstantsPane constantsPane;
	Constant[] oldOrder;
	
	public SortConstantsCommand(PetriNetTab tabContent, ConstantsPane constantsPane) {
		this.tabContent = tabContent;
		this.constantsPane = constantsPane;
	}
	
	@Override
	public void undo() {
		tabContent.undoSort(oldOrder);
		constantsPane.showConstants();
	}

	@Override
	public void redo() {
		oldOrder = tabContent.sortConstants();
		constantsPane.showConstants();
	}

}
