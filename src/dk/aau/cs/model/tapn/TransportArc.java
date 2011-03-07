package dk.aau.cs.model.tapn;

import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.util.Require;

public class TransportArc extends TAPNElement {
	private TimedPlaceInterface source;
	private TimedTransition transition;
	private TimedPlaceInterface destination;

	private TimeInterval interval;

	public TransportArc(TimedPlaceInterface source, TimedTransition transition, TimedPlaceInterface destination, TimeInterval interval) {
		Require.that(source != null, "The source place cannot be null");
		Require.that(transition != null, "The associated transition cannot be null");
		Require.that(destination != null, "The destination place cannot be null");
		
		this.source = source;
		this.transition = transition;
		this.destination = destination;
		setTimeInterval(interval);
	}

	public TimedPlaceInterface source() {
		return source;
	}

	public TimedTransition transition() {
		return transition;
	}

	public TimedPlaceInterface destination() {
		return destination;
	}

	public TimeInterval interval() {
		return interval;
	}

	public void setTimeInterval(TimeInterval interval) {
		Require.that(interval != null, "A transport arc must have an associated interval");

		this.interval = interval;
	}

	public boolean isEnabled() {
		return getElligibleTokens().size() > 0;
	}

	public boolean isEnabledBy(TimedToken token) {
		Require.that(source.equals(token.place()), "Token must be in the correct place");

		return interval.isIncluded(token.age()) && destination.invariant().isSatisfied(token.age());
	}
	
	public List<TimedToken> getElligibleTokens(){
		List<TimedToken> elligibleTokens = new ArrayList<TimedToken>();
		Iterable<TimedToken> tokens = source.tokens();
		for (TimedToken token : tokens) {
			if (isEnabledBy(token)) elligibleTokens.add(token);
		}
		return elligibleTokens;
	}
	
	

	@Override
	public void delete() {
		model().remove(this);
	}

	public TransportArc copy(TimedArcPetriNet tapn) {
		return new TransportArc(tapn.getPlaceByName(source.name()), 
								tapn.getTransitionByName(transition.name()), 
								tapn.getPlaceByName(destination.name()), 
								interval.copy());
	}

	// Should ONLY be called in relation to sharing/unsharing places
	public void setSource(TimedPlaceInterface place) {
		Require.that(place != null, "place cannot be null");
		this.source = place;		
	}
	
	// Should ONLY be called in relation to sharing/unsharing places
	public void setDestination(TimedPlaceInterface place) {
		Require.that(place != null, "place cannot be null");
		this.destination = place;		
	}
}
