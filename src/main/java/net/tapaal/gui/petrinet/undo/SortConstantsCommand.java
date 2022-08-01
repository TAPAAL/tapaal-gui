package net.tapaal.gui.petrinet.undo;

import net.tapaal.gui.petrinet.editor.ConstantsPane;
import pipe.gui.petrinet.PetriNetTab;
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
