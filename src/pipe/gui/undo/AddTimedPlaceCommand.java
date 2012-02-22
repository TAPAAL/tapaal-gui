package pipe.gui.undo;

import pipe.dataLayer.DataLayer;
import pipe.gui.DrawingSurfaceImpl;
import pipe.gui.graphicElements.tapn.TimedPlaceComponent;
import dk.aau.cs.model.tapn.TimedArcPetriNet;

public class AddTimedPlaceCommand extends TAPNElementCommand {
	final TimedPlaceComponent timedPlace;

	public AddTimedPlaceCommand(TimedPlaceComponent timedPlace,
			TimedArcPetriNet tapn, DataLayer guiModel, DrawingSurfaceImpl view) {
		super(tapn, guiModel, view);
		this.timedPlace = timedPlace;
	}

	@Override
	public void undo() {
		tapn.remove(timedPlace.underlyingPlace());
		timedPlace.delete();
		view.repaint();
	}

	@Override
	public void redo() {
		timedPlace.undelete(view);
		tapn.add(timedPlace.underlyingPlace());
		view.repaint();
	}
}
