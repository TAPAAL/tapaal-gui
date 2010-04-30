package dk.aau.cs.petrinet.colors;

import java.util.List;

import dk.aau.cs.petrinet.Arc;
import dk.aau.cs.petrinet.Location;
import dk.aau.cs.petrinet.TAPN;
import dk.aau.cs.petrinet.TAPNArc;
import dk.aau.cs.petrinet.TAPNPlace;
import dk.aau.cs.petrinet.TAPNTransition;
import dk.aau.cs.petrinet.Transition;

public class ColoredTAPN extends TAPN implements ColoredTimedArcPetriNet {

	private int lowerBound = Integer.MAX_VALUE;
	private int upperBound = Integer.MIN_VALUE;
	private boolean boundsCalculated = false;
	
	public int getLowerBoundForColor() {
		if(!boundsCalculated){
			calculateBounds();
		}
		return lowerBound;
	}

	public int getUpperBoundForColor() {
		if(!boundsCalculated){
			calculateBounds();
		}
		return upperBound;
	}

	private void calculateBounds() {
		for(TAPNPlace place : getPlaces()){
			ColoredPlace ctp = (ColoredPlace)place;
			
			for(ColoredToken token : ctp.getColoredTokens()){
				int value = token.getColor();
				
				updateBoundsIfNecessary(value);
			}
		}
		
		for(Arc arc : getArcs()){
			if(arc instanceof ColoredOutputArc){
				ColoredOutputArc outputArc = (ColoredOutputArc)arc;
				int value = outputArc.getOutputValue();
				updateBoundsIfNecessary(value);
			}else if(arc instanceof ColoredTransportArc){
				ColoredTransportArc outputArc = (ColoredTransportArc)arc;
				if(outputArc.getPreservation().equals(Preservation.Age)){
					int value = outputArc.getOutputValue();
					updateBoundsIfNecessary(value);
				}
			}
		}
		
		boundsCalculated = true;
	}

	private void updateBoundsIfNecessary(int value) {
		if(value < lowerBound){
			lowerBound = value;
		}
		
		if(value > upperBound){
			upperBound = value;
		}
	}

	
	public void convertToConservative() throws Exception {
		ColoredPlace capacity = new ColoredPlace("P_capacity", new ColoredTimeInvariant(), new ColorSet());
		addPlace(capacity);

		for (TAPNTransition t : getTransitions()) {
			int difference = t.getPostset().size() - t.getPreset().size();

			if (difference < 0){
				// Add outgoing arcs from transitions to capacity
				for (int i = 0; i > difference; i--) {
					ColoredOutputArc tmp = new ColoredOutputArc(t, capacity);
					add(tmp);
				}
			} else if (difference > 0){
				// Add ingoing arcs from transitions to capacity
				for (int i = 0; i < difference; i++) {
					ColoredInputArc tmp = new ColoredInputArc(capacity, t);
					add(tmp);
				}
			} 
		}
	}
}
