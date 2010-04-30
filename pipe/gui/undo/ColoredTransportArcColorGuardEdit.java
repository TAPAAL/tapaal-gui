package pipe.gui.undo;

import pipe.dataLayer.colors.ColorSet;
import pipe.dataLayer.colors.ColoredTransportArc;

public class ColoredTransportArcColorGuardEdit extends ColorSetEdit {

	private ColoredTransportArc arc;
	public ColoredTransportArcColorGuardEdit(ColoredTransportArc arc, ColorSet oldColorSet,
			ColorSet newColorSet) {
		super(oldColorSet, newColorSet);
		this.arc = arc;
	}

	
	@Override
	public void redo() {
		arc.setColorGuard(getNewColorSet());
	}

	
	@Override
	public void undo() {
		arc.setColorGuard(getOldColorSet());

	}

}