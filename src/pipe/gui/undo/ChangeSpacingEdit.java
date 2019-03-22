package pipe.gui.undo;

import pipe.gui.CreateGui;
import dk.aau.cs.gui.undo.Command;

public class ChangeSpacingEdit extends Command {

	private double factor;

	public ChangeSpacingEdit(double factor) {
		this.factor = factor;
	}

	@Override
	public void redo() {
		CreateGui.getApp().changeSpacing(factor);
	}

	@Override
	public void undo() {
		CreateGui.getApp().changeSpacing(1/factor);
	}

}
