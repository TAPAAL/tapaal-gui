package pipe.gui.undo;

import pipe.dataLayer.colors.ColoredTimedPlace;
import pipe.dataLayer.colors.ColoredToken;

public class ColoredPlaceAddTokenEdit extends UndoableEdit {

	private ColoredTimedPlace place;
	private ColoredToken addedToken;
	
	public ColoredPlaceAddTokenEdit(ColoredTimedPlace place, ColoredToken addedToken){
		this.place = place;
		this.addedToken = addedToken;
	}
	
	@Override
	public void redo() {
		place.addColoredToken(addedToken);
	}

	@Override
	public void undo() {
		place.removeColoredToken(addedToken);
	}

}
