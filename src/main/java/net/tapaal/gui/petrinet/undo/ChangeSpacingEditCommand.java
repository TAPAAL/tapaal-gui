package net.tapaal.gui.petrinet.undo;

import pipe.gui.petrinet.PetriNetTab;

public class ChangeSpacingEditCommand implements Command {

	private final PetriNetObjectPositionSnapshot before;
	private final PetriNetObjectPositionSnapshot after;
	private final PetriNetTab tab;

    public ChangeSpacingEditCommand(PetriNetObjectPositionSnapshot before, PetriNetObjectPositionSnapshot after, PetriNetTab tabContent) {
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
