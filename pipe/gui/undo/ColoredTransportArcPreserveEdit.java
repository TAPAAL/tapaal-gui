package pipe.gui.undo;

import pipe.dataLayer.colors.ColoredTransportArc;
import pipe.dataLayer.colors.Preserve;

public class ColoredTransportArcPreserveEdit extends UndoableEdit {

	private Preserve oldPreserve;
	private Preserve newPreserve;
	private ColoredTransportArc arc;
	
	public ColoredTransportArcPreserveEdit(ColoredTransportArc arc, Preserve oldPreserve, Preserve newPreserve){
		this.arc = arc;
		this.oldPreserve = oldPreserve;
		this.newPreserve = newPreserve;
	}
	
	public void redo() {
		arc.setPreservation(newPreserve);
	}

	
	public void undo() {
		arc.setPreservation(oldPreserve);
	}

}
