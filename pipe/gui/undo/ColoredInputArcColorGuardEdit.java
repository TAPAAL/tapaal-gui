package pipe.gui.undo;

import pipe.dataLayer.colors.ColorSet;
import pipe.dataLayer.colors.ColoredInputArc;

public class ColoredInputArcColorGuardEdit extends ColorSetEdit {

	private ColoredInputArc arc;
	public ColoredInputArcColorGuardEdit(ColoredInputArc arc, ColorSet oldColorSet,
			ColorSet newColorSet) {
		super(oldColorSet, newColorSet);
		this.arc = arc;
	}

	
	public void redo() {
		arc.setColorGuard(getNewColorSet());
	}

	
	public void undo() {
		arc.setColorGuard(getOldColorSet());

	}

}
