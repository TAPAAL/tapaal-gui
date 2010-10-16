package pipe.gui;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import pipe.dataLayer.DataLayer;

public class TraceTransformer {
	private final DataLayer model;

	public TraceTransformer(DataLayer model){
		this.model = model;
	}
	
	protected DataLayer model(){
		return model;
	}

	public pipe.dataLayer.TAPNTrace interpretTrace(dk.aau.cs.petrinet.trace.TAPNTrace trace){
		pipe.dataLayer.TAPNTrace pipeTrace = new pipe.dataLayer.TAPNTrace();
		for(dk.aau.cs.petrinet.trace.TAPNFiringAction action : trace){
			pipe.dataLayer.FiringAction firingAction = transformFiringAction(action);

			if(firingAction != null){
				pipeTrace.addFiringAction(firingAction);
			}
		}

		return pipeTrace;
	}

	protected pipe.dataLayer.FiringAction transformFiringAction(
			dk.aau.cs.petrinet.trace.TAPNFiringAction action) {
		pipe.dataLayer.FiringAction firingAction = null;
		if(action instanceof dk.aau.cs.petrinet.trace.TimeDelayFiringAction){
			BigDecimal delay = ((dk.aau.cs.petrinet.trace.TimeDelayFiringAction)action).delay();
			firingAction = new pipe.dataLayer.TimeDelayFiringAction(delay);
		}else {
			firingAction = transformDiscreteTransition(action);
		}
		return firingAction;
	}

	protected pipe.dataLayer.FiringAction transformDiscreteTransition(
			dk.aau.cs.petrinet.trace.TAPNFiringAction action) {
		pipe.dataLayer.FiringAction firingAction = null;
		if(action instanceof dk.aau.cs.petrinet.trace.TransitionFiringAction){
			dk.aau.cs.petrinet.trace.TransitionFiringAction transitionFiringAction = (dk.aau.cs.petrinet.trace.TransitionFiringAction)action;

			pipe.dataLayer.Transition transition = model.getTransitionByName(transitionFiringAction.transition());
			List<pipe.dataLayer.simulation.Token> tokens = convertTokens(transitionFiringAction.consumedTokens());

			firingAction = new pipe.dataLayer.DiscreetFiringAction(transition, tokens);			
		}

		return firingAction;
	}

	private List<pipe.dataLayer.simulation.Token> convertTokens(
			List<dk.aau.cs.petrinet.Token> consumedTokens) {
		ArrayList<pipe.dataLayer.simulation.Token> tokens = new ArrayList<pipe.dataLayer.simulation.Token>(consumedTokens.size());

		for(dk.aau.cs.petrinet.Token aauToken : consumedTokens){
			pipe.dataLayer.TimedPlace place = (pipe.dataLayer.TimedPlace)model.getPlaceByName(aauToken.place().getName());
			BigDecimal age = aauToken.age();
			pipe.dataLayer.simulation.Token pipeToken = new pipe.dataLayer.simulation.Token(place, age);
			tokens.add(pipeToken);
		}

		return tokens;
	}
}
