package dk.aau.cs.gui.undo;

import dk.aau.cs.gui.TabContent;

public class ChangeAllNamesVisibilityCommand extends Command {
    private final boolean placeNames;
    private final boolean transitionNames;
    private final boolean isVisible;
    private final boolean isSelectedComponent;
	private final TabContent tabContent;

	public ChangeAllNamesVisibilityCommand(TabContent tabContent, boolean placeNames, boolean transitionNames,  boolean isVisible, boolean isSelectedComponent) {
		this.tabContent = tabContent;
		this.placeNames = placeNames;
		this.transitionNames = transitionNames;
		this.isVisible = isVisible;
		this.isSelectedComponent = isSelectedComponent;
	}

	@Override
	public void redo() {
	    if (placeNames) {
            tabContent.showNames(isVisible, true, isSelectedComponent);
        }
        if (transitionNames) {
            tabContent.showNames(isVisible, false, isSelectedComponent);
        }
	}

	@Override
	public void undo() {
        if (placeNames) {
            tabContent.showNames(!isVisible, true, isSelectedComponent);
        }
        if (transitionNames) {
            tabContent.showNames(!isVisible, false, isSelectedComponent);
        }
	}
}
