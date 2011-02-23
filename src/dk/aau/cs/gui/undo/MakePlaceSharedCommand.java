package dk.aau.cs.gui.undo;

import pipe.dataLayer.TimedPlaceComponent;
import dk.aau.cs.model.tapn.SharedPlace;
import dk.aau.cs.model.tapn.TimedPlace;

public class MakePlaceSharedCommand extends Command {
	private final SharedPlace sharedPlace;
	private final TimedPlace timedPlace;
	private final TimedPlaceComponent placeComponent;
	private final String oldName;
	
	public MakePlaceSharedCommand(SharedPlace sharedPlace, TimedPlace timedPlace, TimedPlaceComponent placeComponent){
		this.sharedPlace = sharedPlace;
		this.timedPlace = timedPlace;
		this.placeComponent = placeComponent;
		this.oldName = timedPlace.name();
	}
	
	@Override
	public void redo() {
		sharedPlace.makeShared(timedPlace);
		placeComponent.repaint();
	}

	@Override
	public void undo() {
		timedPlace.unshare();
		timedPlace.setName(oldName);
		placeComponent.repaint();
	}
}
