package dk.aau.cs.model.tapn.simulation;

import java.util.List;

import dk.aau.cs.model.tapn.NetworkMarking;
import dk.aau.cs.model.tapn.TimedToken;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.util.Require;

public class TAPNNetworkTimedTransitionStep extends TAPNNetworkTraceStep {
	private TimedTransition transition;
	private List<TimedToken> consumedTokens;

	public TAPNNetworkTimedTransitionStep(TimedTransition transition, List<TimedToken> consumedTokens) {
		Require.that(transition != null, "transition cannot be null");
		this.transition = transition;
		this.consumedTokens = consumedTokens;
	}

	public NetworkMarking performStepFrom(NetworkMarking marking) {
		return marking.fireTransition(transition, consumedTokens);
	}
	
	public TimedTransition getTransition(){
		return transition;
	}
	
	public List<TimedToken> getConsumedTokens(){
		return consumedTokens;
	}

	@Override
	public String toString() {
		
		String toReturn = transition.isShared() ? transition.sharedTransition().name() : transition.toString();
		return isLoopStep() ? formatAsLoopStep(toReturn) : toReturn;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if(!(obj instanceof TAPNNetworkTimedTransitionStep))	return false; 
		TAPNNetworkTimedTransitionStep other = (TAPNNetworkTimedTransitionStep) obj;
		if(!transition.equals(other.transition))	return false;
		if(!consumedTokens.equals(other.consumedTokens))	return false;
		return true;
	}
}
