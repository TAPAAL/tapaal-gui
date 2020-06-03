package dk.aau.cs.gui.undo;

import java.util.Hashtable;
import java.util.List;

import pipe.dataLayer.TAPNQuery;
import pipe.gui.graphicElements.tapn.TimedPlaceComponent;
import dk.aau.cs.TCTL.visitors.BooleanResult;
import dk.aau.cs.TCTL.visitors.MakePlaceSharedVisitor;
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

public class MakePlaceSharedCommand extends Command {
	private final SharedPlace sharedPlace;
	private final TimedPlace place;
	private final TimedArcPetriNet tapn;
	private final TimedPlaceComponent placeComponent;
	private final boolean multishare;
	
	private final Hashtable<TAPNQuery, TAPNQuery> newQueryToOldQueryMapping;
	private final List<TimedToken> oldTokens;
	private final TabContent currentTab;

	public MakePlaceSharedCommand(TimedArcPetriNet tapn, SharedPlace sharedPlace, TimedPlace place, TimedPlaceComponent placeComponent, TabContent currentTab, boolean multishare){
		Require.that(tapn != null, "tapn cannot be null");
		Require.that(sharedPlace != null, "sharedPlace cannot be null");
		Require.that(place != null, "timedPlace cannot be null");
		Require.that(placeComponent != null, "placeComponent cannot be null");
		Require.that(currentTab != null, "currentTab cannot be null");

		this.tapn = tapn;
		this.sharedPlace = sharedPlace;
		this.place = place;
		this.placeComponent = placeComponent;
		this.multishare = multishare;
		oldTokens = place.tokens();
		this.currentTab = currentTab;
		newQueryToOldQueryMapping = new Hashtable<TAPNQuery, TAPNQuery>();
	}
	public MakePlaceSharedCommand(TimedArcPetriNet tapn, SharedPlace sharedPlace, TimedPlace place, TimedPlaceComponent placeComponent, TabContent currentTab){
		this(tapn, sharedPlace, place, placeComponent, currentTab, false);
	}
	
	@Override
	public void redo() {
		updateArcs(place, sharedPlace);
		
		tapn.remove(place);
		tapn.add(sharedPlace, multishare);
		placeComponent.setUnderlyingPlace(sharedPlace);
		
		updateQueries(place, sharedPlace);
	}

	@Override
	public void undo() {
		updateArcs(sharedPlace, place);
		tapn.remove(sharedPlace);
		tapn.add(place, multishare);
		place.addTokens(oldTokens);
		placeComponent.setUnderlyingPlace(place);
		
		undoQueryChanges(sharedPlace, place);
	}

	private void updateArcs(TimedPlace toReplace, TimedPlace replacement) {
		for(TimedInputArc arc : tapn.inputArcs()){
			if(arc.source().equals(toReplace)){
				arc.setSource(replacement);
				replacement.addInputArc(arc);
			}
		}
		
		for(TimedInhibitorArc arc : tapn.inhibitorArcs()){
			if(arc.source().equals(toReplace)){
				arc.setSource(replacement);
                replacement.addInhibitorArc(arc);
            }
		}
		
		for(TransportArc arc : tapn.transportArcs()){
			if(arc.source().equals(toReplace)){
				arc.setSource(replacement);
                replacement.addTransportArc(arc);
            }
			
			if(arc.destination().equals(toReplace)){
				arc.setDestination(replacement);
                replacement.addTransportArc(arc);
            }
		}
		
		for(TimedOutputArc arc : tapn.outputArcs()){
			if(arc.destination().equals(toReplace)){
				arc.setDestination(replacement);
                replacement.addOutputArc(arc);
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
