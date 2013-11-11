package dk.aau.cs.approximation;

import dk.aau.cs.model.tapn.Bound;
import dk.aau.cs.model.tapn.IntBound;
import dk.aau.cs.model.tapn.TimeInterval;
import dk.aau.cs.model.tapn.TimeInvariant;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedInputArc;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TransportArc;
import pipe.dataLayer.TAPNQuery;

public class OverApproximation implements ITAPNApproximation {
	@Override
	public void modifyTAPN(TimedArcPetriNet net, TAPNQuery query) {	
		
		
		for (TimedInputArc arc : net.inputArcs()) {
			 //Fix input arcs
			TimeInterval oldInterval = arc.interval();
			
			TimeInterval newInterval = modifyIntervals(oldInterval, query.approximationDenominator());
			
			arc.setTimeInterval(newInterval);
		}
		 
		for (TransportArc arc : net.transportArcs()) {
			//fix transport arcs
			TimeInterval oldInterval = arc.interval();
			
			TimeInterval newInterval = modifyIntervals(oldInterval, query.approximationDenominator());
			
			arc.setTimeInterval(newInterval);
		}
		 
		// Fix invariants in places
		for (TimedPlace place : net.places()) {
			if ( ! (place.invariant().upperBound() instanceof Bound.InfBound) && place.invariant().upperBound().value() > 0) {					
				TimeInvariant oldInvariant = place.invariant();
				place.setInvariant(new TimeInvariant(oldInvariant.isUpperNonstrict(), new IntBound((int) Math.ceil(oldInvariant.upperBound().value() / (double)query.approximationDenominator()))));
			}
		}
	}
	
	//Returns a copy of an approximated interval
	private TimeInterval modifyIntervals(TimeInterval oldInterval, int denominator){
		Bound newUpperBound;
		// Do not calculate upper bound for infinite
		if ( ! (oldInterval.upperBound() instanceof Bound.InfBound)) {
			 // Calculate the new upper bound value. If the value is fx. 22 the new value needs to be 3  
			int oldUpperBoundValue = oldInterval.upperBound().value();
			newUpperBound = new IntBound((int) Math.ceil((double)oldUpperBoundValue /  denominator));
		}		
		else
			newUpperBound = Bound.Infinity;
		 
		// Calculate the new lower bound
		IntBound newLowerBound = new IntBound((int) Math.floor(oldInterval.lowerBound().value() / denominator));

		return new TimeInterval(
			 oldInterval.IsLowerBoundNonStrict(),
			 newLowerBound,
			 newUpperBound,
			 oldInterval.IsUpperBoundNonStrict()
			 );
	}

}
