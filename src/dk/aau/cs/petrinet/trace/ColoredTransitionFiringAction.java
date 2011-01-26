package dk.aau.cs.petrinet.trace;

import java.util.List;

import dk.aau.cs.petrinet.TAPNTransition;
import dk.aau.cs.petrinet.colors.ColoredToken;


public class ColoredTransitionFiringAction 
		implements TAPNFiringAction {

	private final List<ColoredToken> consumedTokens;
	private final List<ColoredToken> producedTokens;
	private final TAPNTransition transition;

	public ColoredTransitionFiringAction(TAPNTransition transition, List<ColoredToken> consumedTokens, List<ColoredToken> producedTokens) {
				this.transition = transition;
				this.consumedTokens = consumedTokens;
				this.producedTokens = producedTokens;
	}
	
	public String transition(){
		return transition.getName();
	}
	
	public List<ColoredToken> consumedTokens(){
		return consumedTokens;
	}
	
	public List<ColoredToken> producedTokens(){
		return producedTokens;
	}

}
