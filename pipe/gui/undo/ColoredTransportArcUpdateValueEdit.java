package pipe.gui.undo;

import pipe.dataLayer.colors.ColoredTransportArc;
import pipe.dataLayer.colors.OutputValue;

public class ColoredTransportArcUpdateValueEdit extends UndoableEdit {

	private OutputValue oldValue;
	private OutputValue newValue;
	private ColoredTransportArc arc;
	
	public ColoredTransportArcUpdateValueEdit(ColoredTransportArc arc, OutputValue oldValue, OutputValue newValue){
		this.arc = arc;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}
	
	public void redo() {
		arc.setOutputValue(newValue);
		((ColoredTransportArc)arc.getconnectedTo()).setOutputValue(newValue);
	}

	public void undo() {
		arc.setOutputValue(oldValue);
		((ColoredTransportArc)arc.getconnectedTo()).setOutputValue(oldValue);
	}

}
