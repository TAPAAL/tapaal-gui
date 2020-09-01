package dk.aau.cs.approximation;

import java.util.ArrayList;

import dk.aau.cs.model.tapn.Bound;
import dk.aau.cs.model.tapn.IntBound;
import dk.aau.cs.model.tapn.TimeInterval;
import dk.aau.cs.model.tapn.TimeInvariant;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedInhibitorArc;
import dk.aau.cs.model.tapn.TimedInputArc;
import dk.aau.cs.model.tapn.TimedOutputArc;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.model.tapn.TransportArc;
import pipe.dataLayer.DataLayer;
import pipe.gui.graphicElements.Arc;
import pipe.gui.graphicElements.Place;
import pipe.gui.graphicElements.Transition;

public class UnderApproximation implements ITAPNApproximation {
	@Override
	public void modifyTAPN(TimedArcPetriNet net, int approximationDenominator) {
		modifyTAPN(net, approximationDenominator, null);
	}
	
	public void modifyTAPN(TimedArcPetriNet net, int approximationDenominator, DataLayer guiModel) {	
		// Fix input arcs
		ArrayList<TimedTransition> transitionsToDelete = new ArrayList<TimedTransition>();
		
		for (TimedInputArc arc : net.inputArcs()) {
			 //Fix input arcs
			TimeInterval oldInterval = arc.interval();
			
			TimeInterval newInterval = modifyIntervals(oldInterval, approximationDenominator);
			
			//New interval can be null if lower bound surpassed upper bound in rounding
			if (newInterval != null){
				arc.setTimeInterval(newInterval);
			}
			else{
				//If an arcs interval became invalid we need to delete it, as well as the destination transition and any other arcs that transition has
				//otherwise we could enable behavior which is not allowed for under approximation
				if(!transitionsToDelete.contains(arc.destination())){
					transitionsToDelete.add(arc.destination());
				}
			}
		}

		for (TransportArc arc : net.transportArcs()) {
			//fix transport arcs
			TimeInterval oldInterval = arc.interval();
			
			TimeInterval newInterval = modifyIntervals(oldInterval, approximationDenominator);
			
			//New interval can be null if lower bound surpassed upper bound in rounding
			if (newInterval != null)
				arc.setTimeInterval(newInterval);
			else{
				if(!transitionsToDelete.contains(arc.transition())){
					transitionsToDelete.add(arc.transition());
				}
			}
		}

		for(TimedTransition transitionToDelete : transitionsToDelete){
			if(guiModel != null){
				deleteTransitionFromGuiModel(transitionToDelete, guiModel);
			}
			else{
				deleteArcsFromTransition(transitionToDelete);
			}
			transitionToDelete.delete();
		}	
		
		// Fix invariants in places
		for (TimedPlace place1 : net.places()) {
			if ( ! (place1.invariant().upperBound() instanceof Bound.InfBound) && place1.invariant().upperBound().value() > 0) {					
				TimeInvariant oldInvariant = place1.invariant();
				
				int newInvariantBound = (int) Math.floor(oldInvariant.upperBound().value() / (double)approximationDenominator);
				if(newInvariantBound != 0){
					place1.setInvariant(new TimeInvariant(oldInvariant.isUpperNonstrict(), new IntBound(newInvariantBound)));
				}
				else{
					place1.setInvariant(new TimeInvariant(true, new IntBound(0)));
				}
			}
		}
	}
	
	private void deleteTransitionFromGuiModel(TimedTransition transitionToDelete, DataLayer guiModel) {
		// We assert here that deletions in the guimodel will also delete underlying model elements
		Transition guiTransition = guiModel.getTransitionByName(transitionToDelete.name());
		
		for (Arc arc1 : guiModel.getArcs()){
			if (arc1.getTarget() instanceof Place && arc1.getSource() == guiTransition) //If arc1 is an output arc
			{
				guiModel.removePetriNetObject(arc1);
			}
			else if (arc1.getTarget() instanceof Transition && arc1.getTarget() == guiTransition){ //Else if arc1 is an input arc
				guiModel.removePetriNetObject(arc1);
			}
		}
		guiModel.removePetriNetObject(guiTransition);
	}
	
	private void deleteArcsFromTransition(TimedTransition transitionToDelete) {
		ArrayList<TimedInputArc> inputArcs = new ArrayList<TimedInputArc>();
		//Add all did not work properly, so we do it manually.
		for(TimedInputArc arc : transitionToDelete.getInputArcs()){
			inputArcs.add(arc);
		}
		for(TimedInputArc arc : inputArcs){
			arc.delete();
		}
		
		ArrayList<TimedOutputArc> outputArcs = new ArrayList<TimedOutputArc>();
		for(TimedOutputArc arc : transitionToDelete.getOutputArcs()){
			outputArcs.add(arc);
		}
		for(TimedOutputArc arc : outputArcs){
			arc.delete();
		}
		
		ArrayList<TransportArc> transportArcs = new ArrayList<TransportArc>();
		for(TransportArc arc : transitionToDelete.getTransportArcsGoingThrough()){
			transportArcs.add(arc);
		}
		for(TransportArc arc : transportArcs){
			arc.delete();
		}
		
		ArrayList<TimedInhibitorArc> inhibitorArcs = new ArrayList<TimedInhibitorArc>();
		for(TimedInhibitorArc arc : transitionToDelete.getInhibitorArcs()){
			inhibitorArcs.add(arc);
		}
		for(TimedInhibitorArc arc : inhibitorArcs){
			arc.delete();
		}
	}
	
	//Returns a copy of an approximated interval
	private TimeInterval modifyIntervals(TimeInterval oldInterval, int denominator){
		Bound newUpperBound;
		// Do not calculate upper bound for infinite
		if ( ! (oldInterval.upperBound() instanceof Bound.InfBound)) {
			
			 // Calculate the new upper bound value.
			int oldUpperBoundValue = oldInterval.upperBound().value();
			newUpperBound = new IntBound((int) Math.floor((double)oldUpperBoundValue /  denominator));
		}
		else
			newUpperBound = Bound.Infinity;
		 
		// Calculate the new lower bound
		IntBound newLowerBound = new IntBound((int) Math.ceil((double)oldInterval.lowerBound().value() / denominator));
		 
		// if the lower bound has become greater than the upper bound by rounding
				if ( ! (oldInterval.upperBound() instanceof Bound.InfBound) && newLowerBound.value() > newUpperBound.value())
				{
					newLowerBound = new IntBound((int) Math.floor((double)oldInterval.lowerBound().value() / denominator));
					newUpperBound = new IntBound((int) Math.floor((double)oldInterval.upperBound().value() / denominator));
				}
				
				boolean isLowerBoundNonStrict = oldInterval.isLowerBoundNonStrict();
				boolean isUpperBoundNonStrict = oldInterval.isUpperBoundNonStrict();
				
				// if the interval becomes too small we make it a bit bigger to secure, that we do not have to delete the arc
				if ( (newUpperBound.value() == newLowerBound.value()) && !(oldInterval.isLowerBoundNonStrict() && oldInterval.isUpperBoundNonStrict()))
				{
					isUpperBoundNonStrict = true;
					isLowerBoundNonStrict = true;
				}

				return new TimeInterval(
					 isLowerBoundNonStrict,
					 newLowerBound,
					 newUpperBound,
					 isUpperBoundNonStrict
					 );
	}

}
