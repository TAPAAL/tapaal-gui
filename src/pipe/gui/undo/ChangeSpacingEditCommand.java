package pipe.gui.undo;

import pipe.gui.TabContent;
import net.tapaal.gui.undo.Command;

public class ChangeSpacingEditCommand extends Command {

	private final double factor;
	private final TabContent tab;

    public ChangeSpacingEditCommand(double factor, TabContent tabContent) {
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
