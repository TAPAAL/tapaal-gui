package pipe.gui.undo;

import pipe.dataLayer.colors.ColorSet;
import pipe.dataLayer.colors.ColoredTimedPlace;

public class PlaceColorInvariantEdit extends UndoableEdit {

	private ColorSet oldColorInvariant;
	private ColorSet newColorInvariant;
	private ColoredTimedPlace place;
	
	public PlaceColorInvariantEdit(ColoredTimedPlace place, ColorSet oldInv, ColorSet newInv){
		this.place = place;
		this.oldColorInvariant = oldInv;
		this.newColorInvariant = newInv;
	}
	
	public void redo() {
		place.setColorInvariant(newColorInvariant);
	}

	
	public void undo() {
		place.setColorInvariant(oldColorInvariant);
	}

}
