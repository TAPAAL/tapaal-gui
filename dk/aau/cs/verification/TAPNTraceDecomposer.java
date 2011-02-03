package dk.aau.cs.verification;

import java.util.ArrayList;

import dk.aau.cs.model.TapaalTrace;
import dk.aau.cs.model.TapaalTraceStep;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedToken;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.model.tapn.simulation.TimeDelayStep;
import dk.aau.cs.model.tapn.simulation.TimedTrace;
import dk.aau.cs.model.tapn.simulation.TimedTransitionStep;
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
	public TAPNTraceDecomposer(TAPNTrace trace, TimedArcPetriNetNetwork tapnNetwork, NameMapping mapping){
		this.trace = trace;
		this.tapnNetwork = tapnNetwork;
		this.mapping = mapping;
	}
	
	public TapaalTrace decompose(){
		if(!trace.isConcreteTrace()) throw new RuntimeException("Untimed Traces not yet implemented");
		
		TimedTrace decomposedTrace = new TimedTrace();
		
		for(TAPNFiringAction action : trace.firingActions()){
			decomposedTrace.add(decomposeAction(action));
		}
		
		return decomposedTrace;
	}

	private TapaalTraceStep decomposeAction(TAPNFiringAction action) {
		TapaalTraceStep decomposedAction = null;
		if(action instanceof TransitionFiringAction){
			TransitionFiringAction transitionFiring = (TransitionFiringAction)action;
			decomposedAction = decomposeTransitionFiring(transitionFiring);
		}else if(action instanceof TimeDelayFiringAction){
			decomposedAction = new TimeDelayStep(((TimeDelayFiringAction)action).delay());
		}
		return decomposedAction;
	}

	private TapaalTraceStep decomposeTransitionFiring(TransitionFiringAction transitionFiring) {
		Tuple<String, String> originalName = mapping.map(transitionFiring.transition());
		TimedTransition transition = tapnNetwork.getTAPNByName(originalName.value1()).getTransitionByName(originalName.value2());
		
		ArrayList<TimedToken> convertedTokens = new ArrayList<TimedToken>(transitionFiring.consumedTokens().size());
		for(Token token : transitionFiring.consumedTokens()){
			Tuple<String,String> remappedName = mapping.map(token.place().getName());
			TimedPlace place = tapnNetwork.getTAPNByName(remappedName.value1()).getPlaceByName(remappedName.value2());
			
			convertedTokens.add(new TimedToken(place, token.age()));
		}
		return new TimedTransitionStep(transition, convertedTokens);
	}
}
