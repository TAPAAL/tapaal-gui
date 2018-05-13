package dk.aau.cs.model.tapn;

import dk.aau.cs.util.Require;

public class TimedOutputArc extends TAPNElement {
	private Weight weight;
	private TimedTransition source;
	private TimedPlace destination;
	
	public TimedOutputArc(TimedTransition source, TimedPlace destination){
		this(source, destination, new IntWeight(1));
	}

	public TimedOutputArc(TimedTransition source, TimedPlace destination, Weight weight) {
		Require.that(source != null, "An arc must have a non-null source transition");
		Require.that(destination != null, "An arc must have a non-null destination place");
		Require.that(!source.isShared() || !destination.isShared(), "You cannot draw an arc between a shared transition and shared place.");
		this.source = source;
		this.destination = destination;
		this.weight = weight;
	}
	
	public Weight getWeight(){
		return weight;
	}
        
        public Weight getWeightValue(){
                return new IntWeight(weight.value());
	}
	
	public void setWeight(Weight weight){
		this.weight = weight;
	}

	public TimedTransition source() {
		return source;
	}

	public TimedPlace destination() {
		return destination;
	}

	@Override
	public void delete() {
		model().remove(this);
	}

	public TimedOutputArc copy(TimedArcPetriNet tapn) {
		return new TimedOutputArc(tapn.getTransitionByName(source.name()), tapn.getPlaceByName(destination.name()), weight);
	}

	public void setDestination(TimedPlace place) {
		Require.that(place != null, "place cannot be null");
		destination = place;		
	}
	
	@Override
	public String toString() {
		return "From " + source.name() + " to " + destination.name();
	}
}
