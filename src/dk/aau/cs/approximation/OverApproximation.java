package dk.aau.cs.approximation;

import dk.aau.cs.TCTL.TCTLAtomicPropositionNode;
import dk.aau.cs.TCTL.TCTLEFNode;
import dk.aau.cs.model.tapn.*;
import dk.aau.cs.model.tapn.simulation.*;
import dk.aau.cs.verification.VerificationResult;
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
	
	public void makeTraceTAPN(TimedArcPetriNet net, VerificationResult<TAPNNetworkTrace> result, dk.aau.cs.model.tapn.TAPNQuery query) {
		
		LocalTimedPlace currentPlace = new LocalTimedPlace("PTRACE0");
		TimedToken currentToken = new TimedToken(currentPlace); 
		net.add(currentPlace);
		currentPlace.addToken(currentToken);
		
		int integerName = 0;
		TAPNNetworkTrace trace = result.getTrace();
				
		for(TAPNNetworkTraceStep step : trace) {
			if (step instanceof TAPNNetworkTimedTransitionStep) {
				TimedTransition currentTransition = net.getTransitionByName(((TAPNNetworkTimedTransitionStep) step).getTransition().name());
				net.add(new TimedInputArc(currentPlace, currentTransition, TimeInterval.ZERO_INF));
				for (TimedTransition transition : net.transitions()) {
					if(currentTransition != transition){
						net.add(new TimedInhibitorArc(currentPlace, transition, TimeInterval.ZERO_INF));
					}
				}
				
				integerName += 1;
				currentPlace = new LocalTimedPlace("PTRACE" + Integer.toString(integerName));
				
				net.add(currentPlace);
				net.add(new TimedOutputArc(currentTransition, currentPlace));
			}
		}
		
		query = new dk.aau.cs.model.tapn.TAPNQuery(new TCTLEFNode(new TCTLAtomicPropositionNode(currentPlace.name(), "=", 1)), result.getQueryResult().getQuery().getExtraTokens());
		
	}

}
