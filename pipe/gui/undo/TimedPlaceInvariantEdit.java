package pipe.gui.undo;

import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.tapn.TimeInvariant;
import pipe.dataLayer.TimedPlaceComponent;

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
		place.setInvariant(newvalue.toString()); 

	}

	@Override
	public void undo() {
		place.setInvariant(oldvalue.toString());
	}

}
