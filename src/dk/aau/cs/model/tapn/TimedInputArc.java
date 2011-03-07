package dk.aau.cs.model.tapn;

import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.util.Require;

public class TimedInputArc extends TAPNElement {
	private TimedPlace source;
	private TimeInterval interval;
	private TimedTransition destination;

	public TimedInputArc(TimedPlace source, TimedTransition destination, TimeInterval interval) {
		Require.that(source != null, "A timed input arc cannot have a null source place");
		Require.that(destination != null, "A timed input arc cannot have a null destination transition");
		
		this.source = source;
		this.destination = destination;
		setTimeInterval(interval);
	}

	public TimedPlace source() {
		return source;
	}

	public TimeInterval interval() {
		return interval;
	}

	public void setTimeInterval(TimeInterval interval) {
		Require.that(interval != null, "A timed input arc cannot have a null interval");

		this.interval = interval;
	}

	public TimedTransition destination() {
		return destination;
	}

	public boolean isEnabled() {
		return getElligibleTokens().size() > 0;
	}

	public boolean isEnabledBy(TimedToken token) {
		Require.that(source.equals(token.place()), "This token is from a different place");
		return interval.isIncluded(token.age());
	}
	
	public List<TimedToken> getElligibleTokens(){
		ArrayList<TimedToken> elligbleTokens = new ArrayList<TimedToken>();
		List<TimedToken> tokens = source.tokens();

		for (TimedToken t : tokens) {
			if (isEnabledBy(t)) elligbleTokens.add(t);
		}

		return elligbleTokens;
	}

	public void delete() {
		model().remove(this);
	}

	public TimedInputArc copy(TimedArcPetriNet tapn) {
		return new TimedInputArc(tapn.getPlaceByName(source.name()), tapn.getTransitionByName(destination.name()), interval.copy());
	}

	// This method should ONLY be called in relation to sharing/unsharing a place
	public void setSource(TimedPlace place) {
		Require.that(place != null, "place cannot be null");
		this.source = place;		
	}
}
