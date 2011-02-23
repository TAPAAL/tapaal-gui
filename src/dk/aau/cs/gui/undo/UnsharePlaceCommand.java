package dk.aau.cs.gui.undo;

import pipe.dataLayer.TimedPlaceComponent;
import dk.aau.cs.model.tapn.SharedPlace;
import dk.aau.cs.model.tapn.TimedPlace;

public class UnsharePlaceCommand extends Command {
	private final SharedPlace sharedPlace;
	private final TimedPlace timedPlace;
	private final TimedPlaceComponent placeComponent;
	
	public UnsharePlaceCommand(SharedPlace sharedPlace, TimedPlace timedPlace, TimedPlaceComponent placeComponent){
		this.sharedPlace = sharedPlace;
		this.timedPlace = timedPlace;
		this.placeComponent = placeComponent;
	}
	
	@Override
	public void redo() {
		timedPlace.unshare();
		placeComponent.repaint();
	}

	@Override
	public void undo() {
		sharedPlace.makeShared(timedPlace);
		placeComponent.repaint();
	}
}
