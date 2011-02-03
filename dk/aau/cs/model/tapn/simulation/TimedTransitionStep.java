package dk.aau.cs.model.tapn.simulation;

import java.util.List;

import dk.aau.cs.model.TapaalTraceStep;
import dk.aau.cs.model.tapn.TimedToken;
import dk.aau.cs.model.tapn.TimedTransition;

public class TimedTransitionStep implements TapaalTraceStep {
	private TimedTransition transition;
	private List<TimedToken> consumedTokens;
	
	public TimedTransitionStep(TimedTransition transition, List<TimedToken> consumedTokens){
		this.transition = transition;
		this.consumedTokens = consumedTokens;
	}
}
