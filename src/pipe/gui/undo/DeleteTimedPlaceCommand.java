package pipe.gui.undo;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.TimedPlaceComponent;
import pipe.gui.DrawingSurfaceImpl;
import dk.aau.cs.model.tapn.TimedArcPetriNet;

public class DeleteTimedPlaceCommand extends TAPNElementCommand {
	private final TimedPlaceComponent timedPlace;

	public DeleteTimedPlaceCommand(TimedPlaceComponent timedPlace, TimedArcPetriNet tapn, DataLayer guiModel,
			DrawingSurfaceImpl view) {
		super(tapn, guiModel, view);
		this.timedPlace = timedPlace;
	}

	@Override
	public void redo() {
		timedPlace.delete();
		view.repaint();		
	}

	@Override
	public void undo() {
		timedPlace.undelete(view);
		tapn.add(timedPlace.underlyingPlace());
		view.repaint();
	}
	

}
