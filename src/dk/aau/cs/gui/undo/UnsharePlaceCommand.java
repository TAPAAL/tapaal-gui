package dk.aau.cs.gui.undo;

import pipe.gui.graphicElements.tapn.TimedPlaceComponent;
import dk.aau.cs.model.tapn.SharedPlace;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedInhibitorArc;
import dk.aau.cs.model.tapn.TimedInputArc;
import dk.aau.cs.model.tapn.TimedOutputArc;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TransportArc;
import dk.aau.cs.util.Require;

public class UnsharePlaceCommand extends Command {
	private final SharedPlace sharedPlace;
	private final TimedPlace place;
	private final TimedArcPetriNet tapn;
	private final TimedPlaceComponent placeComponent;
		
	public UnsharePlaceCommand(TimedArcPetriNet tapn, SharedPlace sharedPlace, TimedPlace place, TimedPlaceComponent placeComponent){
		Require.that(tapn != null, "tapn cannot be null");
		Require.that(sharedPlace != null, "sharedPlace cannot be null");
		Require.that(place != null, "timedPlace cannot be null");
		Require.that(placeComponent != null, "placeComponent cannot be null");
		
		this.tapn = tapn;
		this.sharedPlace = sharedPlace;
		this.place = place;
		this.placeComponent = placeComponent;
	}
	
	@Override
	public void redo() {
		updateArcs(sharedPlace, place);
		
		tapn.remove(sharedPlace);
		tapn.add(place);
		placeComponent.setUnderlyingPlace(place);
	}

	@Override
	public void undo() {
		updateArcs(place, sharedPlace);
		
		tapn.remove(place);
		tapn.add(sharedPlace);
		placeComponent.setUnderlyingPlace(sharedPlace);	
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
}
