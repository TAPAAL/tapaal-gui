package dk.aau.cs.gui.undo;

import dk.aau.cs.TCTL.visitors.RenamePlaceTCTLVisitor;
import dk.aau.cs.gui.TabContent;
import dk.aau.cs.model.tapn.LocalTimedPlace;
import dk.aau.cs.model.tapn.TimedTransition;
import pipe.dataLayer.TAPNQuery;

import java.time.LocalTime;
import java.util.Dictionary;

public class ChangeAllNamesVisibilityCommand extends Command {
    private final boolean placeNames;
    private final boolean transitionNames;
    private final boolean isVisible;
    private final boolean isActiveComponent;
	private final TabContent tabContent;

	public ChangeAllNamesVisibilityCommand(TabContent tabContent, boolean placeNames, boolean transitionNames,  boolean isVisible, boolean isActiveComponent) {
		this.tabContent = tabContent;
		this.placeNames = placeNames;
		this.transitionNames = transitionNames;
		this.isVisible = isVisible;
		this.isActiveComponent = isActiveComponent;
	}

	@Override
	public void redo() {
	    if (placeNames) {
            tabContent.showNames(isVisible, true, isActiveComponent);
        }
        if (transitionNames) {
            tabContent.showNames(isVisible, false, isActiveComponent);
        }
	}

	@Override
	public void undo() {
        if (placeNames) {
            tabContent.showNames(!isVisible, true, isActiveComponent);
        }
        if (transitionNames) {
            tabContent.showNames(!isVisible, false, isActiveComponent);
        }
	}
}
