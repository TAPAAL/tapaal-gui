package pipe.gui.undo;

import java.util.List;

import pipe.dataLayer.colors.ColoredTimedPlace;
import pipe.dataLayer.colors.ColoredToken;

public class ColoredPlaceTokensChangedEdit extends UndoableEdit {

	private ColoredTimedPlace place;
	private List<ColoredToken> oldTokens;
	private List<ColoredToken> newTokens;
	
	public ColoredPlaceTokensChangedEdit(ColoredTimedPlace place, List<ColoredToken> oldTokens, List<ColoredToken> newTokens){
		this.place = place;
		this.oldTokens = oldTokens;
		this.newTokens = newTokens;
	}
	@Override
	public void redo() {
		place.setColoredTokens(newTokens);
	}

	@Override
	public void undo() {
		place.setColoredTokens(oldTokens);
	}

}
