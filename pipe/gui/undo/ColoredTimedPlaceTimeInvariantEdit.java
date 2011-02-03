package pipe.gui.undo;

import pipe.dataLayer.colors.ColoredTimeInvariant;
import pipe.dataLayer.colors.ColoredTimedPlace;
import dk.aau.cs.gui.undo.Command;

public class ColoredTimedPlaceTimeInvariantEdit extends Command {

	private ColoredTimedPlace place;
	private ColoredTimeInvariant oldInvariant;
	private ColoredTimeInvariant newInvariant;

	public ColoredTimedPlaceTimeInvariantEdit(ColoredTimedPlace place,
			ColoredTimeInvariant oldInvariant, ColoredTimeInvariant newInvariant) {
		this.place = place;
		this.oldInvariant = oldInvariant;
		this.newInvariant = newInvariant;
	}

	@Override
	public void redo() {
		place.setTimeInvariant(newInvariant);
	}

	@Override
	public void undo() {
		place.setTimeInvariant(oldInvariant);

	}

}
