package pipe.gui.undo;

import pipe.dataLayer.colors.ColoredTimeInvariant;
import pipe.dataLayer.colors.ColoredTimedPlace;

public class ColoredTimedPlaceTimeInvariantEdit extends UndoableEdit {

	private ColoredTimedPlace place;
	private ColoredTimeInvariant oldInvariant;
	private ColoredTimeInvariant newInvariant;
	
	public ColoredTimedPlaceTimeInvariantEdit(ColoredTimedPlace place, ColoredTimeInvariant oldInvariant, ColoredTimeInvariant newInvariant){
		this.place = place;
		this.oldInvariant = oldInvariant;
		this.newInvariant = newInvariant;
	}
	
	public void redo() {
		place.setTimeInvariant(newInvariant);
	}

	public void undo() {
		place.setTimeInvariant(oldInvariant);

	}

}
