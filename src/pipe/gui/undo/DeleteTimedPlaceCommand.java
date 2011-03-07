package pipe.gui.undo;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.TimedPlaceComponent;
import pipe.gui.DrawingSurfaceImpl;
import dk.aau.cs.model.tapn.SharedPlace;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedToken;
import dk.aau.cs.util.Require;

public class DeleteTimedPlaceCommand extends TAPNElementCommand {
	private TimedPlaceComponent timedPlace;
	private SharedPlace sharedPlace;
	private int numberOfTokens;

	public DeleteTimedPlaceCommand(TimedPlaceComponent timedPlace, TimedArcPetriNet tapn, DataLayer guiModel, DrawingSurfaceImpl view) {
		super(tapn, guiModel, view);
		Require.notImplemented();
//		this.timedPlace = timedPlace;
//		this.numberOfTokens = timedPlace.underlyingPlace().numberOfTokens();
//		this.sharedPlace = timedPlace.underlyingPlace().sharedPlace();
	}

	@Override
	public void redo() {
		Require.notImplemented();
//		timedPlace.delete();
//		view.repaint();		
	}

	@Override
	public void undo() {
		Require.notImplemented();
//		timedPlace.undelete(view);
//		if(sharedPlace != null) sharedPlace.makeShared(timedPlace.underlyingPlace());
//		tapn.add(timedPlace.underlyingPlace());
//		for(int i = 0; i < numberOfTokens; i++)
//			tapn.addToken(new TimedToken(timedPlace.underlyingPlace()));
//		view.repaint();
	}
	

}
