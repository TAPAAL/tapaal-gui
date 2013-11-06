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

public class UnderApproximation implements ITAPNApproximation {
	@Override
	public void modifyTAPN(TimedArcPetriNet net, TAPNQuery query) {	
		// Fix input arcs
		for (TimedInputArc arc : net.inputArcs()) {
			 //Fix input arcs
			TimeInterval oldInterval = arc.interval();
			
			TimeInterval newInterval = modifyIntervals(oldInterval, query.approximationDenominator());
			
			//New interval can be null if lower bound surpassed upper bound in rounding
			if (newInterval != null)
				arc.setTimeInterval(newInterval);
			arc.delete();
		}
		 
		for (TransportArc arc : net.transportArcs()) {
			//fix transport arcs
			TimeInterval oldInterval = arc.interval();
			
			TimeInterval newInterval = modifyIntervals(oldInterval, query.approximationDenominator());
			
			//New interval can be null if lower bound surpassed upper bound in rounding
			if (newInterval != null)
				arc.setTimeInterval(newInterval);
			arc.delete();
		}
		
		// Fix invariants in places
		for (TimedPlace place : net.places()) {
			if ( ! (place.invariant().upperBound() instanceof Bound.InfBound) && place.invariant().upperBound().value() > 0) {					
				TimeInvariant oldInvariant = place.invariant().copy();
				place.setInvariant(new TimeInvariant(oldInvariant.isUpperNonstrict(), new IntBound((int) Math.floor(oldInvariant.upperBound().value() / (double)query.approximationDenominator()))));
			}
		}
	}
	
	
	//Returns a copy of an approximated interval
	private TimeInterval modifyIntervals(TimeInterval oldInterval, int denominator){
		int newUpperBoundValue = oldInterval.upperBound().value();
		IntBound newUpperBound;
		// Do not calculate upper bound for infinite
		if ( ! (oldInterval.upperBound() instanceof Bound.InfBound)) {
			
			 // Calculate the new upper bound value. If the value is fx. 22 the new value needs to be 3  
			newUpperBoundValue = oldInterval.upperBound().value();
			newUpperBound = new IntBound((int) Math.floor((double)newUpperBoundValue /  denominator));
		}
		else
			newUpperBound = new IntBound(oldInterval.upperBound().value());
		 
		// Calculate the new lower bound
		IntBound newLowerBound = new IntBound((int) Math.ceil(oldInterval.lowerBound().value() / denominator));
		 
		// if the lower bound has become greater than the upper bound by rounding
		if ( ! (oldInterval.upperBound() instanceof Bound.InfBound) && newLowerBound.value() > newUpperBound.value())
		{
			return null;
		}

		return new TimeInterval(
			 oldInterval.IsLowerBoundNonStrict(),
			 newLowerBound,
			 newUpperBound,
			 oldInterval.IsUpperBoundNonStrict()
			 );
	}

}
