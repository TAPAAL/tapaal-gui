package pipe.gui.undo;

import dk.aau.cs.gui.undo.Command;
import pipe.dataLayer.colors.ColorSet;

public abstract class ColorSetEdit extends Command {

	private ColorSet oldColorSet;
	private ColorSet newColorSet;

	public ColorSetEdit(ColorSet oldColorSet, ColorSet newColorSet){
		this.oldColorSet = oldColorSet;
		this.newColorSet = newColorSet;
	}

	public void setOldColorSet(ColorSet oldColorSet) {
		this.oldColorSet = oldColorSet;
	}

	public ColorSet getOldColorSet() {
		return oldColorSet;
	}

	public void setNewColorSet(ColorSet newColorSet) {
		this.newColorSet = newColorSet;
	}

	public ColorSet getNewColorSet() {
		return newColorSet;
	}
}
