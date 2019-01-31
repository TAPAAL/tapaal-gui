package dk.aau.cs.gui.undo;

import java.util.Hashtable;
import java.util.List;

import pipe.dataLayer.TAPNQuery;
import pipe.gui.graphicElements.tapn.TimedPlaceComponent;
import dk.aau.cs.TCTL.visitors.BooleanResult;
import dk.aau.cs.TCTL.visitors.MakePlaceSharedVisitor;
import dk.aau.cs.gui.SharedPlacesAndTransitionsPanel;
import dk.aau.cs.gui.TabContent;
import dk.aau.cs.model.tapn.SharedPlace;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedInhibitorArc;
import dk.aau.cs.model.tapn.TimedInputArc;
import dk.aau.cs.model.tapn.TimedOutputArc;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedToken;
import dk.aau.cs.model.tapn.TransportArc;
import dk.aau.cs.util.Require;
import dk.aau.cs.util.RequireException;

public class MakePlaceNewSharedCommand extends Command {
	private final String newSharedName;
	private final TimedPlace place;
	private final TimedArcPetriNet tapn;
	private final TimedPlaceComponent placeComponent;
	private Hashtable<TAPNQuery, TAPNQuery> newQueryToOldQueryMapping;
	private final List<TimedToken> oldTokens;
	private final TabContent currentTab;
	private SharedPlacesAndTransitionsPanel sharedPanel;
	private SharedPlace sharedPlace;
	private boolean multiShare;
	
	public MakePlaceNewSharedCommand(TimedArcPetriNet tapn, String newSharedName, TimedPlace place, TimedPlaceComponent placeComponent, TabContent currentTab, boolean multiShare){
		Require.that(tapn != null, "tapn cannot be null");
		Require.that(newSharedName != null, "newSharedName cannot be null");
		Require.that(place != null, "timedPlace cannot be null");
		Require.that(placeComponent != null, "placeComponent cannot be null");
		Require.that(currentTab != null, "currentTab cannot be null");
		
		this.tapn = tapn;
		this.newSharedName = newSharedName;
		this.place = place;
		this.placeComponent = placeComponent;
		this.sharedPlace = null;
		this.currentTab = currentTab;
		this.sharedPanel = currentTab.getSharedPlacesAndTransitionsPanel();
		this.multiShare = multiShare;
		oldTokens = place.tokens();
		newQueryToOldQueryMapping = new Hashtable<TAPNQuery, TAPNQuery>();
	}
	
	@Override
	public void redo() {
		tapn.remove(place);

		if(sharedPlace == null){
			sharedPlace = new SharedPlace(newSharedName);
		}
		
		sharedPanel.addSharedPlace(sharedPlace, multiShare);
		updateArcs(place, sharedPlace);
		tapn.add(sharedPlace, multiShare);
		placeComponent.setUnderlyingPlace(sharedPlace);
		
		updateQueries(place, sharedPlace);
	}

	@Override
	public void undo() {	
		if(sharedPlace != null){
			sharedPanel.removeSharedPlace(sharedPlace);
		}
		updateArcs(sharedPlace, place);
		tapn.remove(sharedPlace);
		tapn.add(place, multiShare);
		place.addTokens(oldTokens);
		placeComponent.setUnderlyingPlace(place);
		
		undoQueryChanges(sharedPlace, place);
	}

	private void updateArcs(TimedPlace toReplace, TimedPlace replacement) {
		for(TimedInputArc arc : tapn.inputArcs()){
			if(arc.source().equals(toReplace)){
				arc.setSource(replacement);
			}
		}
		
		for(TimedInhibitorArc arc : tapn.inhibitorArcs()){
			if(arc.source().equals(toReplace)){
				arc.setSource(replacement);
			}
		}
		
		for(TransportArc arc : tapn.transportArcs()){
			if(arc.source().equals(toReplace)){
				arc.setSource(replacement);
			}
			
			if(arc.destination().equals(toReplace)){
				arc.setDestination(replacement);
			}
		}
		
		for(TimedOutputArc arc : tapn.outputArcs()){
			if(arc.destination().equals(toReplace)){
				arc.setDestination(replacement);
			}
		}
	}
	
	private void updateQueries(TimedPlace toReplace, TimedPlace replacement) {
		MakePlaceSharedVisitor visitor = new MakePlaceSharedVisitor((toReplace.isShared() ? "" : tapn.name()), toReplace.name(), (replacement.isShared() ? "" : tapn.name()), replacement.name());
		for(TAPNQuery query : currentTab.queries()) {
			TAPNQuery oldCopy = query.copy();
			BooleanResult isQueryAffected = new BooleanResult(false);
			query.getProperty().accept(visitor, isQueryAffected);
			
			if(isQueryAffected.result())
				newQueryToOldQueryMapping.put(query, oldCopy);
				
		}
	}
		
	private void undoQueryChanges(SharedPlace toReplace, TimedPlace replacement) {
		for(TAPNQuery query : currentTab.queries()) {
			if(newQueryToOldQueryMapping.containsKey(query))
				query.set(newQueryToOldQueryMapping.get(query));
		}
		
		newQueryToOldQueryMapping.clear();
	}
}
