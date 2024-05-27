package dk.aau.cs.approximation;

import dk.aau.cs.model.CPN.ColoredTimeInterval;
import dk.aau.cs.model.CPN.ColoredTimeInvariant;
import dk.aau.cs.model.tapn.*;
import pipe.gui.petrinet.dataLayer.DataLayer;
import pipe.gui.petrinet.graphicElements.Arc;
import pipe.gui.petrinet.graphicElements.Place;
import pipe.gui.petrinet.graphicElements.Transition;

import java.util.ArrayList;
import java.util.List;

public class UnderApproximation implements ITAPNApproximation {
	@Override
	public void modifyTAPN(TimedArcPetriNet net, int approximationDenominator) {
		modifyTAPN(net, approximationDenominator, null);
	}
	
	public void modifyTAPN(TimedArcPetriNet net, int approximationDenominator, DataLayer guiModel) {	
		// Fix input arcs
		List<TimedTransition> transitionsToDelete = new ArrayList<TimedTransition>();
		
		for (TimedInputArc arc : net.inputArcs()) {
			List<ColoredTimeInterval> newIntervals = new ArrayList<ColoredTimeInterval>();
			for (ColoredTimeInterval interval : arc.getColorTimeIntervals()) {
				ColoredTimeInterval newInterval = (ColoredTimeInterval)modifyIntervals(interval, approximationDenominator);

				if (newInterval != null) {
					newIntervals.add(newInterval);
				} else if (!transitionsToDelete.contains(arc.destination())) {
					transitionsToDelete.add(arc.destination());
				}
			}
			arc.setColorTimeIntervals(newIntervals);

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
			List<ColoredTimeInterval> newIntervals = new ArrayList<ColoredTimeInterval>();
			for (ColoredTimeInterval interval : arc.getColorTimeIntervals()) {
				ColoredTimeInterval newInterval = (ColoredTimeInterval)modifyIntervals(interval, approximationDenominator);

				if (newInterval != null) {
					newIntervals.add(newInterval);
				} else if (!transitionsToDelete.contains(arc.transition())) {
					transitionsToDelete.add(arc.transition());
				}
			}
			arc.setColorTimeIntervals(newIntervals);

			TimeInterval oldInterval = arc.interval();
			TimeInterval newInterval = modifyIntervals(oldInterval, approximationDenominator);
			
			//New interval can be null if lower bound surpassed upper bound in rounding
			if (newInterval != null)
				arc.setTimeInterval(newInterval);
			else {
				if(!transitionsToDelete.contains(arc.transition())){
					transitionsToDelete.add(arc.transition());
				}
			}
		}

		for (TimedTransition transitionToDelete : transitionsToDelete) {
			if (guiModel != null){
				deleteTransitionFromGuiModel(transitionToDelete, guiModel);
			} else {
				deleteArcsFromTransition(transitionToDelete);
			}
			
			transitionToDelete.delete();
		}	
		
		// Fix invariants in places
		for (TimedPlace place : net.places()) {
			List<ColoredTimeInvariant> newInvariants = new ArrayList<ColoredTimeInvariant>();
			for (ColoredTimeInvariant cti : place.getCtiList()) {
				if (!(cti.upperBound() instanceof Bound.InfBound) && cti.upperBound().value() > 0) {
					int newInvariantBound = (int)Math.ceil(cti.upperBound().value() / (double)approximationDenominator);
					newInvariants.add(new ColoredTimeInvariant(cti.isUpperNonstrict(), new IntBound(newInvariantBound), cti.getColor()));
				}
			}
			place.setCtiList(newInvariants);

			// Default age invariants
			if (!(place.invariant().upperBound() instanceof Bound.InfBound) && place.invariant().upperBound().value() > 0) {					
				TimeInvariant oldInvariant = place.invariant();
				int newInvariantBound = (int)Math.floor(oldInvariant.upperBound().value() / (double)approximationDenominator);
				place.setInvariant(new TimeInvariant(oldInvariant.isUpperNonstrict(), new IntBound(newInvariantBound)));
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
		List<TimedInputArc> inputArcs = new ArrayList<TimedInputArc>();
		//Add all did not work properly, so we do it manually.
		for(TimedInputArc arc : transitionToDelete.getInputArcs()){
			inputArcs.add(arc);
		}
		for(TimedInputArc arc : inputArcs){
			arc.delete();
		}
		
		List<TimedOutputArc> outputArcs = new ArrayList<TimedOutputArc>();
		for(TimedOutputArc arc : transitionToDelete.getOutputArcs()){
			outputArcs.add(arc);
		}
		for(TimedOutputArc arc : outputArcs){
			arc.delete();
		}
		
		List<TransportArc> transportArcs = new ArrayList<TransportArc>();
		for(TransportArc arc : transitionToDelete.getTransportArcsGoingThrough()){
			transportArcs.add(arc);
		}
		for(TransportArc arc : transportArcs){
			arc.delete();
		}
		
		List<TimedInhibitorArc> inhibitorArcs = new ArrayList<TimedInhibitorArc>();
		for(TimedInhibitorArc arc : transitionToDelete.getInhibitorArcs()){
			inhibitorArcs.add(arc);
		}
		for(TimedInhibitorArc arc : inhibitorArcs){
			arc.delete();
		}
	}
	
	//Returns a copy of an approximated interval
    private TimeInterval modifyIntervals(TimeInterval oldInterval, int denominator) {
        Bound newUpperBound;
        // Do not calculate upper bound for infinite
        if (!(oldInterval.upperBound() instanceof Bound.InfBound)) {

            // Calculate the new upper bound value.
            int oldUpperBoundValue = oldInterval.upperBound().value();
            newUpperBound = new IntBound((int) Math.floor((double) oldUpperBoundValue / denominator));
        } else {
            newUpperBound = Bound.Infinity;
        }

        // Calculate the new lower bound
        IntBound newLowerBound = new IntBound((int) Math.ceil((double) oldInterval.lowerBound().value() / denominator));

        // if the lower bound has become greater than the upper bound by rounding
        if (!(oldInterval.upperBound() instanceof Bound.InfBound) && newLowerBound.value() > newUpperBound.value()) {
            newLowerBound = new IntBound((int) Math.floor((double) oldInterval.lowerBound().value() / denominator));
            newUpperBound = new IntBound((int) Math.floor((double) oldInterval.upperBound().value() / denominator));
        }

        boolean isLowerBoundNonStrict = oldInterval.isLowerBoundNonStrict();
        boolean isUpperBoundNonStrict = oldInterval.isUpperBoundNonStrict();

        // if the interval becomes too small we make it a bit bigger to secure, that we do not have to delete the arc
        if ((newUpperBound.value() == newLowerBound.value()) && !(oldInterval.isLowerBoundNonStrict() && oldInterval.isUpperBoundNonStrict())) {
            isUpperBoundNonStrict = true;
            isLowerBoundNonStrict = true;
        }

		if (oldInterval instanceof ColoredTimeInterval) {
			return new ColoredTimeInterval(
				isLowerBoundNonStrict,
				newLowerBound,
				newUpperBound,
				isUpperBoundNonStrict,
				((ColoredTimeInterval) oldInterval).getColor()
			);
		}
        return new TimeInterval(
            isLowerBoundNonStrict,
            newLowerBound,
            newUpperBound,
            isUpperBoundNonStrict
        );
    }

}
