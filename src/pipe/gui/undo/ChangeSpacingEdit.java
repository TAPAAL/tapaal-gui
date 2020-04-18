package pipe.gui.undo;

import dk.aau.cs.gui.TabContent;
import dk.aau.cs.gui.undo.Command;

public class ChangeSpacingEdit extends Command {

	private double factor;
	private TabContent tab;

    public ChangeSpacingEdit(double factor, TabContent tabContent) {
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
