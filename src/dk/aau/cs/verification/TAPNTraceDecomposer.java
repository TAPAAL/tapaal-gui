package dk.aau.cs.verification;

import java.util.ArrayList;

import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedToken;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.model.tapn.simulation.TAPNNetworkTrace;
import dk.aau.cs.model.tapn.simulation.TAPNNetworkTraceStep;
import dk.aau.cs.model.tapn.simulation.TAPNNetworkTimeDelayStep;
import dk.aau.cs.model.tapn.simulation.TimedTAPNNetworkTrace;
import dk.aau.cs.model.tapn.simulation.TAPNNetworkTimedTransitionStep;
import dk.aau.cs.model.tapn.simulation.UntimedTAPNNetworkTrace;
import dk.aau.cs.petrinet.Token;
import dk.aau.cs.petrinet.trace.TAPNFiringAction;
import dk.aau.cs.petrinet.trace.TAPNTrace;
import dk.aau.cs.petrinet.trace.TimeDelayFiringAction;
import dk.aau.cs.petrinet.trace.TransitionFiringAction;
import dk.aau.cs.util.Tuple;

public class TAPNTraceDecomposer {
	private final TAPNTrace trace;
	private final NameMapping mapping;
	private final TimedArcPetriNetNetwork tapnNetwork;

	public TAPNTraceDecomposer(TAPNTrace trace, TimedArcPetriNetNetwork tapnNetwork, NameMapping mapping) {
		this.trace = trace;
		this.tapnNetwork = tapnNetwork;
		this.mapping = mapping;
	}

	public TAPNNetworkTrace decompose() {
		if (!trace.isConcreteTrace()){
			return decomposeUntimedTrace();
		} else {
			return decomposeTimedTrace();
		}
	}

	private TAPNNetworkTrace decomposeUntimedTrace() {
		UntimedTAPNNetworkTrace decomposedTrace = new UntimedTAPNNetworkTrace();

		for (TAPNFiringAction action : trace.firingActions()) {
			decomposedTrace.add((TAPNNetworkTimedTransitionStep)decomposeAction(action));
		}

		return decomposedTrace;
	}

	private TAPNNetworkTrace decomposeTimedTrace() {
		TimedTAPNNetworkTrace decomposedTrace = new TimedTAPNNetworkTrace();

		for (TAPNFiringAction action : trace.firingActions()) {
			decomposedTrace.add(decomposeAction(action));
		}

		return decomposedTrace;
	}

	private TAPNNetworkTraceStep decomposeAction(TAPNFiringAction action) {
		TAPNNetworkTraceStep decomposedAction = null;
		if (action instanceof TransitionFiringAction) {
			TransitionFiringAction transitionFiring = (TransitionFiringAction) action;
			decomposedAction = decomposeTransitionFiring(transitionFiring);
		} else if (action instanceof TimeDelayFiringAction) {
			decomposedAction = new TAPNNetworkTimeDelayStep(((TimeDelayFiringAction) action).delay());
		}
		return decomposedAction;
	}

	private TAPNNetworkTraceStep decomposeTransitionFiring(TransitionFiringAction transitionFiring) {
		Tuple<String, String> originalName = mapping.map(transitionFiring.transition());
		TimedTransition transition = tapnNetwork.getTAPNByName(originalName.value1()).getTransitionByName(originalName.value2());

		ArrayList<TimedToken> convertedTokens = null;
		if(trace.isConcreteTrace()){
			convertedTokens = new ArrayList<TimedToken>(transitionFiring.consumedTokens().size());
			for (Token token : transitionFiring.consumedTokens()) {
				Tuple<String, String> remappedName = mapping.map(token.place().getName());
				TimedPlace place = tapnNetwork.getTAPNByName(remappedName.value1()).getPlaceByName(remappedName.value2());

				convertedTokens.add(new TimedToken(place, token.age()));
			}
		}
		return new TAPNNetworkTimedTransitionStep(transition, convertedTokens);
	}
}
