package net.tapaal.gui.petrinet.undo;

import pipe.gui.petrinet.PetriNetTab;

public class ChangeSpacingEditCommand implements Command {

	private final double factor;
	private final PetriNetTab tab;

    public ChangeSpacingEditCommand(double factor, PetriNetTab tabContent) {
        super();
        this.factor = factor;
        this.tab = tabContent;
    }

    @Override
	public void redo() {
		tab.changeSpacing(factor);
	}

	@Override
	public void undo() {
		tab.changeSpacing(1/factor);
	}

}
