package net.tapaal.gui.petrinet.undo;

import java.util.ArrayList;
import java.util.List;

import pipe.gui.petrinet.dataLayer.DataLayer;
import net.tapaal.gui.petrinet.verification.TAPNQuery;
import pipe.gui.TAPAALGUI;
import pipe.gui.petrinet.graphicElements.tapn.TimedPlaceComponent;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedToken;
import pipe.gui.petrinet.undo.TAPNElementCommand;

public class DeleteTimedPlaceCommand extends TAPNElementCommand {
	private final TimedPlaceComponent timedPlaceComponent;
	private final TimedPlace timedPlace;
	private final List<TimedToken> tokens;
	private final ArrayList<TAPNQuery> queriesInclusion = new ArrayList<TAPNQuery>();

	public DeleteTimedPlaceCommand(TimedPlaceComponent timedPlaceComponent, TimedArcPetriNet tapn, DataLayer guiModel) {
		super(tapn, guiModel);
		this.timedPlaceComponent = timedPlaceComponent;
		tokens = timedPlaceComponent.underlyingPlace().tokens();
		timedPlace = timedPlaceComponent.underlyingPlace();
		
		// queries this place is an inclusion place in 
		Iterable<TAPNQuery> queries = TAPAALGUI.getCurrentTab().queries();
		
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
			for(TimedToken token : tokens) {
				tapn.addToken(token);
			}
		}
		
		for(TAPNQuery q : queriesInclusion){
			q.inclusionPlaces().inclusionPlaces().add(timedPlace);
		}
	}

}
