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
	
	public pipe.dataLayer.TAPNTrace interpretTrace(dk.aau.cs.petrinet.trace.TAPNTrace trace){
		pipe.dataLayer.TAPNTrace pipeTrace = new pipe.dataLayer.TAPNTrace();
		for(dk.aau.cs.petrinet.trace.TAPNFiringAction action : trace){
			if(action instanceof dk.aau.cs.petrinet.trace.TimeDelayFiringAction){
				BigDecimal delay = ((dk.aau.cs.petrinet.trace.TimeDelayFiringAction)action).delay();
				pipeTrace.addFiringAction(new pipe.dataLayer.TimeDelayFiringAction(delay));
			}else if(action instanceof dk.aau.cs.petrinet.trace.TransitionFiringAction){
				dk.aau.cs.petrinet.trace.TransitionFiringAction transitionFiringAction = (dk.aau.cs.petrinet.trace.TransitionFiringAction)action;
				
				pipe.dataLayer.Transition transition = model.getTransitionByName(transitionFiringAction.transition());
				List<pipe.dataLayer.simulation.Token> tokens = convertTokens(transitionFiringAction.consumedTokens());
				
				pipeTrace.addFiringAction(new pipe.dataLayer.DiscreetFiringAction(transition, tokens));				
			}
		}
		
		return pipeTrace;
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
