package dk.aau.cs.approximation;

import java.util.HashMap;
import java.util.Map.Entry;

import dk.aau.cs.TCTL.TCTLAtomicPropositionNode;
import dk.aau.cs.TCTL.TCTLEFNode;
import dk.aau.cs.model.tapn.*;
import dk.aau.cs.model.tapn.simulation.*;
import dk.aau.cs.util.Tuple;
import dk.aau.cs.verification.NameMapping;
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
	
	public void makeTraceTAPN(Tuple<TimedArcPetriNet, NameMapping> transformedModel, VerificationResult<TAPNNetworkTrace> result) {
		TimedArcPetriNet net = transformedModel.value1();

		LocalTimedPlace currentPlace = new LocalTimedPlace("PTRACE0");
		TimedTransition currentTransition = new TimedTransition("TTRACE0");
		TimedToken currentToken = new TimedToken(currentPlace); 
		net.add(currentPlace);
		net.add(currentTransition);
		currentPlace.addToken(currentToken);
		int integerName = 0;
		TAPNNetworkTrace trace = result.getTrace();
		HashMap<String,String> reversedNameMap = reverseNameMapping(transformedModel.value2().getMappedToOrg());

		for(TAPNNetworkTraceStep step : trace) {
			if (step instanceof TAPNNetworkTimedTransitionStep) {
				TimedTransition firedTransition = net.getTransitionByName(reversedNameMap.get(((TAPNNetworkTimedTransitionStep) step).getTransition().name()));
				net.add(new TimedInputArc(currentPlace, currentTransition, TimeInterval.ZERO_INF));
				for (TimedTransition transition : net.transitions()) {
					if(firedTransition != transition && currentTransition != transition){
						net.add(new TimedInhibitorArc(currentPlace, transition, TimeInterval.ZERO_INF));
					}
				}
				if (currentPlace == null)
					System.out.println("Current place is null");
				if (currentTransition == null)
					System.out.println("Current transition is null");
				integerName += 1;
				currentPlace = new LocalTimedPlace("PTRACE" + Integer.toString(integerName));
				
				net.add(currentPlace);
				net.add(new TimedOutputArc(currentTransition, currentPlace));
				currentTransition = new TimedTransition("TTRACE" + Integer.toString(integerName));
				net.add(currentTransition);
			}
		}
		
		for (TimedTransition transition : net.transitions()) {
			if(!transition.name().startsWith("TTRACE")){
				net.add(new TimedInhibitorArc(currentPlace, transition, TimeInterval.ZERO_INF));
			}
		}
		
		net.remove(currentTransition); //Removing last orphan transition
	}
	
	public static HashMap<String,String> reverseNameMapping(HashMap<String,Tuple<String,String>> map) {
		HashMap<String,String> newMap = new HashMap<String,String>();
		for ( Entry<String, Tuple<String,String>> entry : map.entrySet() ) {
		    newMap.put(entry.getValue().value2(), entry.getKey());
		}
		return newMap;
	}
}
