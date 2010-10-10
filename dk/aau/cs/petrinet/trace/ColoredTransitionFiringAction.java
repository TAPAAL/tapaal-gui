package dk.aau.cs.petrinet.trace;

import java.util.List;

import dk.aau.cs.petrinet.TAPNTransition;
import dk.aau.cs.petrinet.colors.ColoredToken;


public class ColoredTransitionFiringAction 
		implements TAPNFiringAction {

	private final List<ColoredToken> tokens;
	private final TAPNTransition transition;

	public ColoredTransitionFiringAction(TAPNTransition transition, List<ColoredToken> tokens) {
				this.transition = transition;
				this.tokens = tokens;
	}
	
	public String transition(){
		return transition.getName();
	}
	
	public List<ColoredToken> consumedTokens(){
		return tokens;
	}

}
