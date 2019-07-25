package pipe.gui.undo;

import pipe.dataLayer.DataLayer;
import pipe.gui.graphicElements.tapn.TimedPlaceComponent;
import dk.aau.cs.model.tapn.TimedArcPetriNet;

public class AddTimedPlaceCommand extends TAPNElementCommand {
	final TimedPlaceComponent timedPlace;

	public AddTimedPlaceCommand(TimedPlaceComponent timedPlace,
			TimedArcPetriNet tapn, DataLayer guiModel) {
		super(tapn, guiModel);
		this.timedPlace = timedPlace;
	}

	@Override
	public void undo() {
		tapn.remove(timedPlace.underlyingPlace());
		guiModel.removePetriNetObject(timedPlace);
	}

	@Override
	public void redo() {
		guiModel.addPetriNetObject(timedPlace);
		tapn.add(timedPlace.underlyingPlace());
	}
}
