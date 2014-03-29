package dk.aau.cs.approximation;

import java.util.ArrayList;

import dk.aau.cs.model.tapn.Bound;
import dk.aau.cs.model.tapn.IntBound;
import dk.aau.cs.model.tapn.TAPNElement;
import dk.aau.cs.model.tapn.TimeInterval;
import dk.aau.cs.model.tapn.TimeInvariant;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedInhibitorArc;
import dk.aau.cs.model.tapn.TimedInputArc;
import dk.aau.cs.model.tapn.TimedOutputArc;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.model.tapn.TransportArc;
import dk.aau.cs.verification.VerificationOptions;
import pipe.dataLayer.DataLayer;
import pipe.dataLayer.TAPNQuery;
import pipe.gui.graphicElements.Arc;
import pipe.gui.graphicElements.Place;
import pipe.gui.graphicElements.Transition;
import pipe.gui.graphicElements.tapn.TimedInhibitorArcComponent;
import pipe.gui.graphicElements.tapn.TimedInputArcComponent;
import pipe.gui.graphicElements.tapn.TimedTransportArcComponent;

public class UnderApproximation implements ITAPNApproximation {
	@Override
	public void modifyTAPN(TimedArcPetriNet net, int approximationDenominator) {
		modifyTAPN(net, approximationDenominator, null);
	}
	
	public void modifyTAPN(TimedArcPetriNet net, int approximationDenominator, DataLayer guiModel) {	
		// Fix input arcs
		ArrayList<TAPNElement> arcsToDelete = new ArrayList<TAPNElement>();
		for (TimedInputArc arc : net.inputArcs()) {
			 //Fix input arcs
			TimeInterval oldInterval = arc.interval();
			
			TimeInterval newInterval = modifyIntervals(oldInterval, approximationDenominator);
			
			//New interval can be null if lower bound surpassed upper bound in rounding
			if (newInterval != null)
				arc.setTimeInterval(newInterval);
			else
				arcsToDelete.add(arc);
		}

		for (TransportArc arc : net.transportArcs()) {
			//fix transport arcs
			TimeInterval oldInterval = arc.interval();
			
			TimeInterval newInterval = modifyIntervals(oldInterval, approximationDenominator);
			
			//New interval can be null if lower bound surpassed upper bound in rounding
			if (newInterval != null)
				arc.setTimeInterval(newInterval);
			else
				arcsToDelete.add(arc);
		}
		
		TimedTransition transition = null;
		TimedPlace place = null;
		Transition guiTransition = null;
		
		for(TAPNElement arc : arcsToDelete){
			if(arc instanceof TimedInputArc){
				transition = ((TimedInputArc)arc).destination();
				place = ((TimedInputArc)arc).source();
			}
			else if(arc instanceof TransportArc){
				transition = ((TransportArc)arc).transition();
				place = ((TransportArc)arc).source();
			}
			
			if (guiModel != null) {	// If we are in the process of saving XML and want to update guiModel
				guiTransition = guiModel.getTransitionByName(transition.name());
	
				//We need to find the arc in the guiModel and delete it
				for (Arc arc1 : guiModel.getArcs()){	
					if(arc1 instanceof TimedInputArcComponent) //If input arc or transport arc
					{ 
						if(arc1 instanceof TimedTransportArcComponent){
							if (net.getTransitionByName(((TimedTransportArcComponent)arc1).getTransition().getName()) == transition){
								arc1.delete();
							}
						}
						else { //Else we must have an input arc
						// TEST TO ENSURE THAT WE ARE ACTUALLY WORKING WITH THE CORRECT GUIMODEL (it verifies)
						//	((TimedInputArcComponent) arc1).setGuardAndWeight(new TimeInterval(true, new IntBound(11), Bound.Infinity, false), arc1.getWeight()); 
							if(net.getTransitionByName(((Transition)arc1.getTarget()).getName()) == transition &&
							   net.getPlaceByName(((Place)arc1.getSource()).getName()) == place){	//If we have the matching arc
						       arc1.delete();
							}
						}
					}
				}
			}
			else {
				arc.delete();
			}
				
			// checks if the transition are now orphan and the output and inhibitorarcs should be deleted
			if(transition.getInputArcs().isEmpty() && transition.getTransportArcsGoingThrough().isEmpty()){
				ArrayList<TAPNElement> toDelete = new ArrayList<TAPNElement>();
				toDelete.addAll(transition.getOutputArcs());
				toDelete.addAll(transition.getInhibitorArcs());
				for(TAPNElement orphanTransitionArc : toDelete){	//Delete the arcs that were connected to 
				//	orphanTransitionArc.delete();
					if (orphanTransitionArc instanceof TimedOutputArc){	//We have only been deleting input arcs and know there are now no input ares or transport arcs
						
						if (guiModel != null){
							//We need to find the arc in the gui and delete it
							for (Arc arc1 : guiModel.getArcs()){
								if (arc1.getTarget() instanceof Place) //If arc1 is an output arc
								{ 
									if(net.getTransitionByName(((Transition)arc1.getSource()).getName()) == ((TimedOutputArc)orphanTransitionArc).source() &&
									   net.getPlaceByName(((Place)arc1.getTarget()).getName()) == ((TimedOutputArc)orphanTransitionArc).destination()){
										arc1.delete();
									}
								}
							}
						}
						else {
							orphanTransitionArc.delete();
						}
					}
				}
				if (guiModel != null) {
					guiTransition.delete(); //Asserting that this also deletes the underlying model transition
				} else {
					transition.delete(); 
				}
				//Now that the transition has been deleted, we also need to delete any inhibitor arcs that were inhibiting it
				ArrayList<TAPNElement> inhibArcsToDelete = new ArrayList<TAPNElement>();
				for (TimedInhibitorArc inhibArc : net.inhibitorArcs()) {
					if (inhibArc.destination() == transition){
						inhibArcsToDelete.add(inhibArc);
					}
				}
				for(TAPNElement inhibArc : inhibArcsToDelete){
					if (guiModel != null) {	// If we are in the process of saving XML and want to update guiModel
						//We need to find the arc in the guiModel and delete it
						for (Arc arc1 : guiModel.getArcs()){	
							if(arc1 instanceof TimedInhibitorArcComponent)
							{ 
								if(((TimedInhibitorArcComponent) arc1).underlyingTimedInhibitorArc() == inhibArc){
									arc1.delete();
								}
							}
						}
					}
					else {
						inhibArc.delete();
					}
				}
			}
		}	
		
		
		// Fix invariants in places
		for (TimedPlace place1 : net.places()) {
			if ( ! (place1.invariant().upperBound() instanceof Bound.InfBound) && place1.invariant().upperBound().value() > 0) {					
				TimeInvariant oldInvariant = place1.invariant();
				place1.setInvariant(new TimeInvariant(oldInvariant.isUpperNonstrict(), new IntBound((int) Math.floor(oldInvariant.upperBound().value() / (double)approximationDenominator))));
			}
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
			return null;
		}
		
		// if the interval becomes too small
		if ( (newUpperBound.value() == newLowerBound.value()) && 
				((oldInterval.IsLowerBoundNonStrict() && !oldInterval.IsUpperBoundNonStrict()) ||
						(!oldInterval.IsLowerBoundNonStrict() && oldInterval.IsUpperBoundNonStrict())))
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
