package net.tapaal.gui.petrinet.undo;

import pipe.gui.petrinet.graphicElements.tapn.TimedPlaceComponent;
import dk.aau.cs.model.tapn.TimeInvariant;

public class TimedPlaceInvariantEditCommand extends Command {

	private final TimeInvariant oldvalue;
	private final TimeInvariant newvalue;
	private final TimedPlaceComponent place;

	public TimedPlaceInvariantEditCommand(TimedPlaceComponent place,
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
