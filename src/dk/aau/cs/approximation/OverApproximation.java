package dk.aau.cs.approximation;

import java.util.ArrayList;
import java.util.HashMap;
import dk.aau.cs.TCTL.TCTLAFNode;
import dk.aau.cs.TCTL.TCTLAGNode;
import dk.aau.cs.TCTL.TCTLAbstractProperty;
import dk.aau.cs.TCTL.TCTLAndListNode;
import dk.aau.cs.TCTL.TCTLAtomicPropositionNode;
import dk.aau.cs.TCTL.TCTLConstNode;
import dk.aau.cs.TCTL.TCTLEFNode;
import dk.aau.cs.TCTL.TCTLEGNode;
import dk.aau.cs.TCTL.TCTLNotNode;
import dk.aau.cs.TCTL.TCTLOrListNode;
import dk.aau.cs.TCTL.TCTLPlaceNode;
import dk.aau.cs.model.tapn.*;
import dk.aau.cs.model.tapn.simulation.*;
import dk.aau.cs.util.Tuple;
import dk.aau.cs.verification.NameMapping;
import dk.aau.cs.verification.VerificationResult;

public class OverApproximation implements ITAPNApproximation {
	@Override
	public void modifyTAPN(TimedArcPetriNet net, int approximationDenominator) {
		// Fix input arcs
		for (TimedInputArc arc : net.inputArcs()) {
			TimeInterval oldInterval = arc.interval();
			TimeInterval newInterval = modifyIntervals(oldInterval, approximationDenominator);
			
			arc.setTimeInterval(newInterval);
		}
		 
		// Fix transport arcs
		for (TransportArc arc : net.transportArcs()) {
			TimeInterval oldInterval = arc.interval();
			TimeInterval newInterval = modifyIntervals(oldInterval, approximationDenominator);
			
			arc.setTimeInterval(newInterval);
		}
		 
		// Fix invariants in places
		for (TimedPlace place : net.places()) {
			if ( ! (place.invariant().upperBound() instanceof Bound.InfBound) && place.invariant().upperBound().value() > 0) {					
				TimeInvariant oldInvariant = place.invariant();
				place.setInvariant(new TimeInvariant(oldInvariant.isUpperNonstrict(), new IntBound((int) Math.ceil(oldInvariant.upperBound().value() / (double)approximationDenominator))));
			}
		}
	}
	
	//Returns a copy of an approximated interval
	private TimeInterval modifyIntervals(TimeInterval oldInterval, int denominator){
		Bound newUpperBound;
		// Do not calculate upper bound for infinite
		if ( ! (oldInterval.upperBound() instanceof Bound.InfBound)) {
			 // Calculate the new upper bound value. E.g. if the value is 22, the new value needs to be 3 given that r = 10
			int oldUpperBoundValue = oldInterval.upperBound().value();
			newUpperBound = new IntBound((int) Math.ceil((double)oldUpperBoundValue /  denominator));
		} else {
			newUpperBound = Bound.Infinity;
		}
		 
		// Calculate the new lower bound
		IntBound newLowerBound = new IntBound((int) Math.floor(oldInterval.lowerBound().value() / denominator));

		return new TimeInterval(
			 oldInterval.isLowerBoundNonStrict(),
			 newLowerBound,
			 newUpperBound,
			 oldInterval.isUpperBoundNonStrict()
			 );
	}
	
	public void makeTraceTAPN(Tuple<TimedArcPetriNet, NameMapping> transformedModel, VerificationResult<TAPNNetworkTrace> result, dk.aau.cs.model.tapn.TAPNQuery query) {
		TimedArcPetriNet net = transformedModel.value1();
                
		LocalTimedPlace currentPlace = new LocalTimedPlace("PTRACE0");
		TimedToken currentToken = new TimedToken(currentPlace);
		net.add(currentPlace);
		currentPlace.addToken(currentToken);
		
		// Block place, which secures the net makes at most one transition not in the trace.
		LocalTimedPlace blockPlace = new LocalTimedPlace("PBLOCK", TimeInvariant.LESS_THAN_INFINITY);
		TimedToken blockToken = new TimedToken(blockPlace);
		net.add(blockPlace);
		blockPlace.addToken(blockToken);
		
		// Copy the original transitions
		ArrayList<TimedTransition> originalTransitions = new ArrayList<TimedTransition>();
		for (TimedTransition transition : net.transitions()) {
			originalTransitions.add(transition);
		}
		
		// Copy the original input arcs
		ArrayList<TimedInputArc> originalInput = new ArrayList<TimedInputArc>();
		for (TimedInputArc inputarc : net.inputArcs()) {
			originalInput.add(inputarc);
		}
		
		// Copy the original output arcs
		ArrayList<TimedOutputArc> originalOutput = new ArrayList<TimedOutputArc>();
		for (TimedOutputArc outputarc : net.outputArcs()) {
			originalOutput.add(outputarc);
		}
		
		// Copy the original inhibitor arcs
		ArrayList<TimedInhibitorArc> originalInhibitor = new ArrayList<TimedInhibitorArc>();
		for (TimedInhibitorArc inhibitor : net.inhibitorArcs()) {
			originalInhibitor.add(inhibitor);
		}
		
		// Copy the original transport arcs
		ArrayList<TransportArc> originalTransport = new ArrayList<TransportArc>();
		for (TransportArc transport : net.transportArcs()) {
			originalTransport.add(transport);
		}

		int placeInteger = 0;
		int transitionInteger = 0;
		TimedOutputArc next = null;
		boolean traceHasTransitionStep = false;
		
		TAPNNetworkTrace trace = result.getTrace();
		HashMap<Tuple<String, String>, String> nameMap = transformedModel.value2().getOrgToMapped();
		LocalTimedPlace loopStep = null;
		boolean delayIsLoopStep = false;
		
		for(TAPNNetworkTraceStep step : trace) {
			if (step instanceof TAPNNetworkTimeDelayStep){
				// Skip if delay step, but check if this step is a delayStep
				if(step.isLoopStep())
				{
					delayIsLoopStep = true;
				}				
			}
			if (step instanceof TAPNNetworkTimedTransitionStep) {
				//TimedTransition firedTransition = net.getTransitionByName(reversedNameMap.get(((TAPNNetworkTimedTransitionStep) step).getTransition().name()));
				TAPNNetworkTimedTransitionStep tmpStep = (TAPNNetworkTimedTransitionStep)step;
				//If the transition in step is shared, use "" as model for lookup in namemap
				Tuple<String, String> key = new Tuple<String, String>(
						(((TAPNNetworkTimedTransitionStep) step).getTransition().sharedTransition() == null ? tmpStep.getTransition().model().name() : ""), 
						tmpStep.getTransition().name()); 
				TimedTransition firedTransition = net.getTransitionByName(nameMap.get(key));
				TimedTransition copyTransition = new TimedTransition(firedTransition.name() + "_traceNet_" + Integer.toString(++transitionInteger), firedTransition.isUrgent());
				
				net.add(copyTransition);
				net.add(new TimedInputArc(currentPlace, copyTransition, TimeInterval.ZERO_INF));
				
				net.add(new TimedInputArc(blockPlace, copyTransition, TimeInterval.ZERO_INF));
				net.add(new TimedOutputArc(copyTransition, blockPlace));
				
				// EG queries, where there is a loopStep, we need to store it for later use.
				if(step.isLoopStep() || delayIsLoopStep)
				{
					loopStep = currentPlace;
					delayIsLoopStep = false;
				}
				
				currentPlace = new LocalTimedPlace("PTRACE" + Integer.toString(++placeInteger));
				net.add(currentPlace);
				next = new TimedOutputArc(copyTransition, currentPlace);
				net.add(next);
				
				
				for (TimedInputArc arc : originalInput) {
					if (arc.destination() == firedTransition) {
						net.add(new TimedInputArc(arc.source(), copyTransition, arc.interval(), arc.getWeight()));
					}
				}
				for (TimedOutputArc arc : originalOutput) {
					if (arc.source() == firedTransition) {
						net.add(new TimedOutputArc(copyTransition, arc.destination(), arc.getWeight()));
					}
				}
				for (TimedInhibitorArc arc : originalInhibitor) {
					if (arc.destination() == firedTransition) {
						net.add(new TimedInhibitorArc(arc.source(), copyTransition, arc.interval(), arc.getWeight()));
					}
				}
				for (TransportArc arc : originalTransport) {
					if (arc.transition() == firedTransition) {
						net.add(new TransportArc(arc.source(), copyTransition, arc.destination(), arc.interval(), arc.getWeight()));
					}
				}
				
				traceHasTransitionStep = true;
			}
		}
		
		// If the trace is a EG trace with a loop, we need to incorporate the loop in traceTAPN
		if(loopStep != null){
			net.add(new TimedOutputArc(next.source(), loopStep));
		}
		
		if(traceHasTransitionStep){
			net.remove(next);
			net.remove(currentPlace);
		}
		
		modifyQuery(query, blockPlace);
		
		// An input arc from pBlock to all original transitions makes sure, that we can do deadlock checks.
		for (TimedTransition transition : originalTransitions) {
			net.add(new TimedInputArc(blockPlace, transition, TimeInterval.ZERO_INF));	
		}           
	}


	private void modifyQuery(dk.aau.cs.model.tapn.TAPNQuery query, LocalTimedPlace blockPlace) {
		TCTLAbstractProperty topNode = query.getProperty();

		TCTLAtomicPropositionNode pBlock = new TCTLAtomicPropositionNode(new TCTLPlaceNode(blockPlace.name()), "=", new TCTLConstNode(1));
		
		// We need to modify the query to also have pBlock = 1. 
		if(topNode instanceof TCTLEFNode)
		{
			if(((TCTLEFNode) topNode).getProperty() instanceof TCTLAndListNode){
				((TCTLAndListNode) ((TCTLEFNode) topNode).getProperty()).addConjunct(pBlock);
			}
			else{
				TCTLAndListNode andList = new TCTLAndListNode((((TCTLEFNode) topNode).getProperty()), pBlock);
				((TCTLEFNode) topNode).setProperty(andList);
			}
				
		}
		else if(topNode instanceof TCTLAGNode)
		{
			TCTLNotNode notNode = new TCTLNotNode(pBlock);
			
			if(((TCTLAGNode) topNode).getProperty() instanceof TCTLOrListNode){
				((TCTLOrListNode) ((TCTLAGNode) topNode).getProperty()).addDisjunct(notNode);
			}
			else{
				TCTLOrListNode orList = new TCTLOrListNode((((TCTLAGNode) topNode).getProperty()), notNode);
				((TCTLAGNode) topNode).setProperty(orList);
			}
	
		}
		else if(topNode instanceof TCTLEGNode)
		{
			if(((TCTLEGNode) topNode).getProperty() instanceof TCTLAndListNode){
				((TCTLAndListNode) ((TCTLEGNode) topNode).getProperty()).addConjunct(pBlock);
			}
			else{
				TCTLAndListNode andList = new TCTLAndListNode((((TCTLEGNode) topNode).getProperty()), pBlock);
				((TCTLEGNode) topNode).setProperty(andList);
			}
		}
		else if(topNode instanceof TCTLAFNode)
		{
			TCTLNotNode notNode = new TCTLNotNode(pBlock);
			
			if(((TCTLAFNode) topNode).getProperty() instanceof TCTLOrListNode){
				((TCTLOrListNode) ((TCTLAFNode) topNode).getProperty()).addDisjunct(notNode);
			}
			else{
				TCTLOrListNode orList = new TCTLOrListNode((((TCTLAFNode) topNode).getProperty()), notNode);
				((TCTLAFNode) topNode).setProperty(orList);
			}
		}
	}
}
