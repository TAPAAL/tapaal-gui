package dk.aau.cs.petrinet.colors;

import dk.aau.cs.petrinet.Arc;
import dk.aau.cs.petrinet.TAPN;
import dk.aau.cs.petrinet.TAPNPlace;

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
			ColoredOutputArc outputArc = (ColoredOutputArc)arc;
			
			int value = outputArc.getOutputValue();
			
			updateBoundsIfNecessary(value);
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

}
