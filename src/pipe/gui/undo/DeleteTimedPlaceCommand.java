package pipe.gui.undo;

import java.util.ArrayList;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.TAPNQuery;
import pipe.gui.CreateGui;
import pipe.gui.graphicElements.tapn.TimedPlaceComponent;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedToken;

public class DeleteTimedPlaceCommand extends TAPNElementCommand {
	private final TimedPlaceComponent timedPlaceComponent;
	private final TimedPlace timedPlace;
	private final int numberOfTokens;
	private final ArrayList<TAPNQuery> queriesInclusion = new ArrayList<TAPNQuery>();

	public DeleteTimedPlaceCommand(TimedPlaceComponent timedPlaceComponent, TimedArcPetriNet tapn, DataLayer guiModel) {
		super(tapn, guiModel);
		this.timedPlaceComponent = timedPlaceComponent;
		numberOfTokens = timedPlaceComponent.underlyingPlace().numberOfTokens();
		timedPlace = timedPlaceComponent.underlyingPlace();
		
		// queries this place is an inclusion place in 
		Iterable<TAPNQuery> queries = CreateGui.getCurrentTab().queries();
		
		for (TAPNQuery q : queries) {
			if(q.inclusionPlaces().inclusionPlaces().contains(timedPlace)){
				queriesInclusion.add(q);
			}
		}
	}

	@Override
	public void redo() {
		// remove the place from the list of inclusion places
		for (TAPNQuery q : queriesInclusion) {
			q.inclusionPlaces().removePlace(timedPlace);
		}

		tapn.remove(timedPlace);
		guiModel.removePetriNetObject(timedPlaceComponent);
	}

	@Override
	public void undo() {
        timedPlaceComponent.deselect();
		guiModel.addPetriNetObject(timedPlaceComponent);
		tapn.add(timedPlace);
		
		if(!timedPlace.isShared()){
			for(int i = 0; i < numberOfTokens; i++) {
				tapn.addToken(new TimedToken(timedPlace));
			}
		}
		
		for(TAPNQuery q : queriesInclusion){
			q.inclusionPlaces().inclusionPlaces().add(timedPlace);
		}
	}

}
