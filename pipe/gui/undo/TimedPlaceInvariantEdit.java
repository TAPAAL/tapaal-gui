package pipe.gui.undo;

import dk.aau.cs.gui.undo.Command;
import pipe.dataLayer.TimedPlace;

public class TimedPlaceInvariantEdit extends Command {

	private String oldvalue;
	private String newvalue;
	private TimedPlace place;
	
	public TimedPlaceInvariantEdit(TimedPlace place, String oldvalue, String newvalue) {
	
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
