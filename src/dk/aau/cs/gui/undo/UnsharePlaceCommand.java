package dk.aau.cs.gui.undo;

import dk.aau.cs.model.tapn.SharedPlace;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.util.Require;

public class UnsharePlaceCommand extends Command {
	private final SharedPlace sharedPlace;
	private final TimedPlace timedPlace;
	
	public UnsharePlaceCommand(SharedPlace sharedPlace, TimedPlace timedPlace){
		Require.notImplemented();
		this.sharedPlace = sharedPlace;
		this.timedPlace = timedPlace;
	}
	
	@Override
	public void redo() {
		Require.notImplemented();
//		timedPlace.unshare();
	}

	@Override
	public void undo() {
		Require.notImplemented();
//		sharedPlace.makeShared(timedPlace);
	}
}
