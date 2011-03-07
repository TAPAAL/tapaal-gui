package dk.aau.cs.gui.undo;

import dk.aau.cs.model.tapn.TimeInvariant;
import dk.aau.cs.model.tapn.TimedPlaceInterface;

public class ChangedInvariantCommand extends Command {
	private final TimedPlaceInterface place;
	private final TimeInvariant oldInvariant;
	private final TimeInvariant newInvariant;

	public ChangedInvariantCommand(TimedPlaceInterface place, TimeInvariant oldInvariant, TimeInvariant newInvariant){
		this.place = place;
		this.oldInvariant = oldInvariant;
		this.newInvariant = newInvariant;
		
	}
	
	@Override
	public void redo() {
		place.setInvariant(newInvariant);
	}

	@Override
	public void undo() {
		place.setInvariant(oldInvariant);
	}

}
