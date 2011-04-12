package dk.aau.cs.model.tapn.simulation;

import java.util.List;

import dk.aau.cs.model.tapn.LocalTimedMarking;
import dk.aau.cs.model.tapn.TimedToken;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.util.Require;

public class TimedTransitionStep implements TimedArcPetriNetStep {
	private TimedTransition transition;
	private List<TimedToken> consumedTokens;

	public TimedTransitionStep(TimedTransition transition, List<TimedToken> consumedTokens) {
		Require.that(transition != null, "transition cannot be null");
		this.transition = transition;
		this.consumedTokens = consumedTokens;
	}

	public LocalTimedMarking performStepFrom(LocalTimedMarking marking) {
		return marking.fireTransition(transition, consumedTokens);
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
