package pipe.gui.undo;

import pipe.dataLayer.colors.ColorSet;
import pipe.dataLayer.colors.ColoredTimedPlace;

public class PlaceColorInvariantEdit extends ColorSetEdit {

	private ColoredTimedPlace place;

	public PlaceColorInvariantEdit(ColoredTimedPlace place, ColorSet oldInv,
			ColorSet newInv) {
		super(oldInv, newInv);
		this.place = place;
	}

	@Override
	public void redo() {
		place.setColorInvariant(getNewColorSet());
	}

	@Override
	public void undo() {
		place.setColorInvariant(getOldColorSet());
	}

}
