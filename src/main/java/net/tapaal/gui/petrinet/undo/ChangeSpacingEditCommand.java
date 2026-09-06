package net.tapaal.gui.petrinet.undo;

import java.awt.Point;
import java.util.Map;

import pipe.gui.petrinet.graphicElements.PetriNetObject;
import pipe.gui.petrinet.PetriNetTab;

public class ChangeSpacingEditCommand implements Command {

	private final Map<PetriNetObject, Point> before;
	private final Map<PetriNetObject, Point> after;
	private final PetriNetTab tab;

    public ChangeSpacingEditCommand(Map<PetriNetObject, Point> before, Map<PetriNetObject, Point> after, PetriNetTab tabContent) {
        super();
		this.before = before;
		this.after = after;
        this.tab = tabContent;
    }

    @Override
	public void redo() {
		tab.restoreGuiObjectLocations(after);
	}

	@Override
	public void undo() {
		tab.restoreGuiObjectLocations(before);
	}

}
