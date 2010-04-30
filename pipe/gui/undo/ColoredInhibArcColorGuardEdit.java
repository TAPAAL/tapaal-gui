package pipe.gui.undo;

import pipe.dataLayer.colors.ColorSet;
import pipe.dataLayer.colors.ColoredInhibitorArc;

public class ColoredInhibArcColorGuardEdit extends ColorSetEdit {

	private ColoredInhibitorArc arc;
	public ColoredInhibArcColorGuardEdit(ColoredInhibitorArc arc, ColorSet oldColorSet,
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