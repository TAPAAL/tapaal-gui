package pipe.gui.undo;

import pipe.dataLayer.TimedPlaceComponent;
import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.tapn.TimeInvariant;

public class TimedPlaceInvariantEdit extends Command {

	private TimeInvariant oldvalue;
	private TimeInvariant newvalue;
	private TimedPlaceComponent place;
	
	public TimedPlaceInvariantEdit(TimedPlaceComponent place, TimeInvariant oldvalue, TimeInvariant newvalue) {
	
		this.oldvalue = oldvalue;
		this.newvalue = newvalue;
		this.place = place;
	
	}
	
	@Override
	public void redo() {
		place.setInvariantFromString(newvalue.toString()); 

	}

	@Override
	public void undo() {
		place.setInvariantFromString(oldvalue.toString());
	}

}
