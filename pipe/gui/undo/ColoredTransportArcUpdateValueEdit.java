package pipe.gui.undo;

import pipe.dataLayer.colors.ColoredTransportArc;
import pipe.dataLayer.colors.IntOrConstant;

public class ColoredTransportArcUpdateValueEdit extends UndoableEdit {

	private IntOrConstant oldValue;
	private IntOrConstant newValue;
	private ColoredTransportArc arc;
	
	public ColoredTransportArcUpdateValueEdit(ColoredTransportArc arc, IntOrConstant oldValue, IntOrConstant newValue){
		this.arc = arc;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}
	
	@Override
	public void redo() {
		arc.setOutputValue(newValue);
		((ColoredTransportArc)arc.getconnectedTo()).setOutputValue(newValue);
	}

	@Override
	public void undo() {
		arc.setOutputValue(oldValue);
		((ColoredTransportArc)arc.getconnectedTo()).setOutputValue(oldValue);
	}

}
