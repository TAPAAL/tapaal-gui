package pipe.gui.undo;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.TimedPlaceComponent;
import pipe.gui.DrawingSurfaceImpl;
import dk.aau.cs.model.tapn.TimedArcPetriNet;

public class AddTimedPlaceCommand extends AddTAPNElementCommand {
	final TimedPlaceComponent timedPlace;
	public AddTimedPlaceCommand(TimedPlaceComponent timedPlace, TimedArcPetriNet tapn, DataLayer guiModel, DrawingSurfaceImpl view){
		super(tapn, guiModel, view);
		this.timedPlace = timedPlace;
	}
	
	@Override
	public void undo() {
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
