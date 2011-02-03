package pipe.gui.undo;

import pipe.dataLayer.colors.ColoredOutputArc;
import pipe.dataLayer.colors.IntOrConstant;
import dk.aau.cs.gui.undo.Command;

public class ColoredOutputArcOutputValueEdit extends Command {

	private IntOrConstant oldValue;
	private IntOrConstant newValue;
	private ColoredOutputArc arc;

	public ColoredOutputArcOutputValueEdit(ColoredOutputArc arc,
			IntOrConstant oldValue, IntOrConstant newValue) {
		this.arc = arc;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	@Override
	public void redo() {
		arc.setOutputValue(newValue);
	}

	@Override
	public void undo() {
		arc.setOutputValue(oldValue);
	}

}
