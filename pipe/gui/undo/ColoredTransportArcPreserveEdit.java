package pipe.gui.undo;

import dk.aau.cs.gui.undo.Command;
import pipe.dataLayer.colors.ColoredTransportArc;
import pipe.dataLayer.colors.Preserve;

public class ColoredTransportArcPreserveEdit extends Command {

	private Preserve oldPreserve;
	private Preserve newPreserve;
	private ColoredTransportArc arc;
	
	public ColoredTransportArcPreserveEdit(ColoredTransportArc arc, Preserve oldPreserve, Preserve newPreserve){
		this.arc = arc;
		this.oldPreserve = oldPreserve;
		this.newPreserve = newPreserve;
	}
	
	@Override
	public void redo() {
		arc.setPreservation(newPreserve);
	}

	
	@Override
	public void undo() {
		arc.setPreservation(oldPreserve);
	}

}
