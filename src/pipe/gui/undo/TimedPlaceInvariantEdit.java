package pipe.gui.undo;

import pipe.gui.graphicElements.tapn.TimedPlaceComponent;
import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.tapn.TimeInvariant;

public class TimedPlaceInvariantEdit extends Command {

	private final TimeInvariant oldvalue;
	private final TimeInvariant newvalue;
	private final TimedPlaceComponent place;

	public TimedPlaceInvariantEdit(TimedPlaceComponent place,
			TimeInvariant oldvalue, TimeInvariant newvalue) {

		this.oldvalue = oldvalue;
		this.newvalue = newvalue;
		this.place = place;

	}

	@Override
	public void redo() {
		place.setInvariant(newvalue);

	}

	@Override
	public void undo() {
		place.setInvariant(oldvalue);
	}

}
