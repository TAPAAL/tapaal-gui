package pipe.gui.undo;

import pipe.dataLayer.colors.ColoredOutputArc;
import pipe.dataLayer.colors.IntOrConstant;

public class ColoredOutputArcOutputValueEdit extends UndoableEdit {

	private IntOrConstant oldValue;
	private IntOrConstant newValue;
	private ColoredOutputArc arc;

	public ColoredOutputArcOutputValueEdit(ColoredOutputArc arc, IntOrConstant oldValue, IntOrConstant newValue){
		this.arc = arc;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}
	
	public void redo() {
		arc.setOutputValue(newValue);
	}

	public void undo() {
		arc.setOutputValue(oldValue);
	}

}
