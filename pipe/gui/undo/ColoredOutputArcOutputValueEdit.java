package pipe.gui.undo;

import pipe.dataLayer.colors.ColoredOutputArc;
import pipe.dataLayer.colors.OutputValue;

public class ColoredOutputArcOutputValueEdit extends UndoableEdit {

	private OutputValue oldValue;
	private OutputValue newValue;
	private ColoredOutputArc arc;

	public ColoredOutputArcOutputValueEdit(ColoredOutputArc arc, OutputValue oldValue, OutputValue newValue){
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
