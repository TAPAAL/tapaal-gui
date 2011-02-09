package dk.aau.cs.model.tapn;

import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.util.Require;

public class TransportArc extends TAPNElement {
	private TimedPlace source;
	private TimedTransition transition;
	private TimedPlace destination;

	private TimeInterval interval;

	public TransportArc(TimedPlace source, TimedTransition transition,
			TimedPlace destination, TimeInterval interval) {
		Require.that(source != null, "The source place cannot be null");
		Require.that(transition != null, "The associated transition cannot be null");
		Require.that(destination != null, "The destination place cannot be null");

		this.source = source;
		this.transition = transition;
		this.destination = destination;
		setTimeInterval(interval);
	}

	public TimedPlace source() {
		return source;
	}

	public TimedTransition transition() {
		return transition;
	}

	public TimedPlace destination() {
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
		Iterable<TimedToken> tokens = source.tokensSatisfyingInterval(interval);
		for (TimedToken token : tokens) {
			if (isEnabledBy(token))
				return true;
		}
		return false;
	}

	public boolean isEnabledBy(TimedToken token) {
		Require.that(source.equals(token.place()), "Token must be in the correct place");

		return interval.isIncluded(token.age()) && destination.invariant().isSatisfied(token.age());
	}
	
	public List<TimedToken> getElligibleTokens(){
		List<TimedToken> elligibleTokens = new ArrayList<TimedToken>();
		Iterable<TimedToken> tokens = source.tokensSatisfyingInterval(interval);
		for (TimedToken token : tokens) {
			if (isEnabledBy(token)) elligibleTokens.add(token);
		}
		return elligibleTokens;
	}
	
	

	@Override
	public void delete() {
		model().remove(this);
	}
}
