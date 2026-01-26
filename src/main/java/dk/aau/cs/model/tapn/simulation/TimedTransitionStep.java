package dk.aau.cs.model.tapn.simulation;

import java.util.List;

import dk.aau.cs.model.tapn.TimedToken;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.util.Require;

public class TimedTransitionStep implements PetriNetStep {
	private final TimedTransition transition;
	private final List<TimedToken> consumedTokens;

	public TimedTransitionStep(TimedTransition transition, List<TimedToken> consumedTokens) {
		Require.that(transition != null, "transition cannot be null");
		this.transition = transition;
		this.consumedTokens = consumedTokens;
	}
	
	public TimedTransition transition() {
		return transition;
	}
	
	public List<TimedToken> consumedTokens() {
		return consumedTokens;
	}

	@Override
	public String toString() {
		return transition.toString();
	}	
}
