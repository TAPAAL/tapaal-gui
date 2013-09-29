package dk.aau.cs.verification;

import java.util.ArrayList;

import dk.aau.cs.model.NTA.trace.TraceToken;
import dk.aau.cs.model.tapn.LocalTimedPlace;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedToken;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.model.tapn.simulation.TAPNNetworkTimeDelayStep;
import dk.aau.cs.model.tapn.simulation.TAPNNetworkTimedTransitionStep;
import dk.aau.cs.model.tapn.simulation.TAPNNetworkTrace;
import dk.aau.cs.model.tapn.simulation.TAPNNetworkTraceStep;
import dk.aau.cs.model.tapn.simulation.TimeDelayStep;
import dk.aau.cs.model.tapn.simulation.TimedArcPetriNetStep;
import dk.aau.cs.model.tapn.simulation.TimedArcPetriNetTrace;
import dk.aau.cs.model.tapn.simulation.TimedTAPNNetworkTrace;
import dk.aau.cs.model.tapn.simulation.TimedTransitionStep;
import dk.aau.cs.model.tapn.simulation.UntimedTAPNNetworkTrace;
import dk.aau.cs.util.Tuple;

public class TraceConverter {
	private final TAPNNetworkTrace trace;
	private final TimedArcPetriNetNetwork tapnNetwork;

	public TraceConverter(TAPNNetworkTrace trace, TimedArcPetriNetNetwork tapnNetwork) {
		this.trace = trace;
		this.tapnNetwork = tapnNetwork;
	}

	public TAPNNetworkTrace convert() {
		if (trace instanceof UntimedTAPNNetworkTrace){
			return decomposeUntimedTrace();
		} else {
			return decomposeTimedTrace();
		}
	}

	private TAPNNetworkTrace decomposeUntimedTrace() {
		UntimedTAPNNetworkTrace decomposedTrace = new UntimedTAPNNetworkTrace();

		for (TAPNNetworkTraceStep action : trace) {
			decomposedTrace.add((TAPNNetworkTimedTransitionStep)decomposeAction(action));
		}

		return decomposedTrace;
	}

	private TAPNNetworkTrace decomposeTimedTrace() {
		TimedTAPNNetworkTrace decomposedTrace = new TimedTAPNNetworkTrace(((TimedTAPNNetworkTrace) trace).getLoopToIndex());
		decomposedTrace.setTraceType(((TimedTAPNNetworkTrace) trace).getTraceType());

		for (TAPNNetworkTraceStep action : trace) {
			TAPNNetworkTraceStep step = decomposeAction(action);
			if(step != null)	decomposedTrace.add(step);
		}

		return decomposedTrace;
	}

	private TAPNNetworkTraceStep decomposeAction(TAPNNetworkTraceStep action) {
		TAPNNetworkTraceStep decomposedAction = null;
		if (action instanceof TAPNNetworkTimedTransitionStep) {
			TAPNNetworkTimedTransitionStep transitionFiring = (TAPNNetworkTimedTransitionStep) action;
			decomposedAction = decomposeTransitionFiring(transitionFiring);
		} else if (action instanceof TAPNNetworkTimeDelayStep) {
			decomposedAction = new TAPNNetworkTimeDelayStep(((TAPNNetworkTimeDelayStep) action).getDelay());
		}
		return decomposedAction;
	}

	private TAPNNetworkTraceStep decomposeTransitionFiring(TAPNNetworkTimedTransitionStep transitionFiring) {
		TimedTransition transition = transitionFiring.getTransition().isShared() ? tapnNetwork.getSharedTransitionByName(transitionFiring.getTransition().name()).transitions().iterator().next() 
				: tapnNetwork.getTAPNByName(transitionFiring.getTransition().model().name()).getTransitionByName(transitionFiring.getTransition().name());

		ArrayList<TimedToken> convertedTokens = null;
		convertedTokens = new ArrayList<TimedToken>(transitionFiring.getConsumedTokens().size());
		for (TimedToken token : transitionFiring.getConsumedTokens()) {
			TimedPlace place = token.place().isShared() ? tapnNetwork.getSharedPlaceByName(token.place().name()) : tapnNetwork.getTAPNByName(((LocalTimedPlace) token.place()).model().name()).getPlaceByName(token.place().name());
			if(token instanceof TraceToken){
				convertedTokens.add(new TraceToken(place, token.age(), ((TraceToken)token).isGreaterThanOrEqual()));
			} else {
				convertedTokens.add(new TimedToken(place, token.age()));
			}
		}

		return transition == null? null:new TAPNNetworkTimedTransitionStep(transition, convertedTokens);
	}
}
