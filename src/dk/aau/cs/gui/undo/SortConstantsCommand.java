package dk.aau.cs.gui.undo;

import net.tapaal.gui.editor.ConstantsPane;
import pipe.gui.TabContent;
import dk.aau.cs.model.tapn.Constant;

public class SortConstantsCommand extends Command{
	
	final TabContent tabContent;
	final ConstantsPane constantsPane;
	Constant[] oldOrder;
	
	public SortConstantsCommand(TabContent tabContent, ConstantsPane constantsPane) {
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
