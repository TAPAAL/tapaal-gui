package pipe.gui.undo;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.TimedPlaceComponent;
import pipe.gui.DrawingSurfaceImpl;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedToken;

public class DeleteTimedPlaceCommand extends TAPNElementCommand {
	private TimedPlaceComponent timedPlaceComponent;
	private TimedPlace timedPlace;
	private int numberOfTokens;

	public DeleteTimedPlaceCommand(TimedPlaceComponent timedPlaceComponent, TimedArcPetriNet tapn, DataLayer guiModel, DrawingSurfaceImpl view) {
		super(tapn, guiModel, view);
		this.timedPlaceComponent = timedPlaceComponent;
		this.numberOfTokens = timedPlaceComponent.underlyingPlace().numberOfTokens();
		this.timedPlace = timedPlaceComponent.underlyingPlace();
	}

	@Override
	public void redo() {
		tapn.remove(timedPlace);
		timedPlaceComponent.delete();
		view.repaint();		
	}

	@Override
	public void undo() {
		timedPlaceComponent.undelete(view);
		tapn.add(timedPlace);
		if(!timedPlace.isShared()){
		for(int i = 0; i < numberOfTokens; i++)
			tapn.addToken(new TimedToken(timedPlace));
		}
		view.repaint();
	}
	

}
