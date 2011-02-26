package dk.aau.cs.gui.undo;

import dk.aau.cs.model.tapn.SharedPlace;
import dk.aau.cs.model.tapn.TimedPlace;

public class UnsharePlaceCommand extends Command {
	private final SharedPlace sharedPlace;
	private final TimedPlace timedPlace;
	
	public UnsharePlaceCommand(SharedPlace sharedPlace, TimedPlace timedPlace){
		this.sharedPlace = sharedPlace;
		this.timedPlace = timedPlace;
	}
	
	@Override
	public void redo() {
		timedPlace.unshare();
	}

	@Override
	public void undo() {
		sharedPlace.makeShared(timedPlace);
	}
}
