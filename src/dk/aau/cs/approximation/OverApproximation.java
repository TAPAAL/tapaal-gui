package dk.aau.cs.approximation;

import dk.aau.cs.model.tapn.Bound;
import dk.aau.cs.model.tapn.IntBound;
import dk.aau.cs.model.tapn.TimeInterval;
import dk.aau.cs.model.tapn.TimeInvariant;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedInputArc;
import dk.aau.cs.model.tapn.TimedPlace;
import pipe.dataLayer.TAPNQuery;

public class OverApproximation implements ITAPNApproximation {
	@Override
	public TimedArcPetriNet modifyTAPN(TimedArcPetriNet net, TAPNQuery query) {	
		// Fix input arcs
		for (TimedInputArc arc : net.inputArcs()) {
			 TimeInterval oldInterval = arc.interval();
			 // Make sure the interval does not have an upper bound of Inf
			 if ( ! (oldInterval.upperBound() instanceof Bound.InfBound)) {
				 // Calculate the new lower bound
				 IntBound newLowerBound = new IntBound((int) Math.floor(oldInterval.lowerBound().value() / query.approximationDenominator()));
				 
				 // Calculate the new upper bound value. If the value is fx. 22 the new value needs to be 3  
				 int newUpperBoundValue = oldInterval.upperBound().value();
				 if ((oldInterval.upperBound().value() % 10) < 5)
					 newUpperBoundValue = oldInterval.upperBound().value() + 5;
				 
				 IntBound newUpperBound = new IntBound((int) Math.ceil(newUpperBoundValue /  (double)query.approximationDenominator()));
	
				 TimeInterval interval = new TimeInterval(
						 oldInterval.IsLowerBoundNonStrict(),
						 newLowerBound,
						 newUpperBound,
						 oldInterval.IsUpperBoundNonStrict()
						 );
				 arc.setTimeInterval(interval);
			 }
		}
		 
		// Fix invariants in places
		for (TimedPlace place : net.places()) {
			if ( ! (place.invariant().upperBound() instanceof Bound.InfBound) && place.invariant().upperBound().value() > 0) {					
				TimeInvariant oldInvariant = place.invariant().copy();
				place.setInvariant(new TimeInvariant(oldInvariant.isUpperNonstrict(), new IntBound((int) Math.floor(oldInvariant.upperBound().value() / (double)query.approximationDenominator()))));
			}
		}
		return net;
	}

}
