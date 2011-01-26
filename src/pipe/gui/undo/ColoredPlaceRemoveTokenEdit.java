package pipe.gui.undo;

import pipe.dataLayer.colors.ColoredTimedPlace;
import pipe.dataLayer.colors.ColoredToken;

public class ColoredPlaceRemoveTokenEdit extends UndoableEdit {

	private ColoredTimedPlace place;
	private ColoredToken removedToken;
	
	public ColoredPlaceRemoveTokenEdit(ColoredTimedPlace place, ColoredToken removedToken){
		this.place = place;
		this.removedToken = removedToken;
	}
	
	@Override
	public void redo() {
		place.removeColoredToken(removedToken);
	}

	@Override
	public void undo() {
		place.addColoredToken(removedToken);
	}

}
