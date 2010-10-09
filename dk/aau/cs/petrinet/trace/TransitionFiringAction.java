package dk.aau.cs.petrinet.trace;

import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.petrinet.TAPNTransition;
import dk.aau.cs.petrinet.Token;

public class TransitionFiringAction implements TAPNFiringAction {

	private TAPNTransition firedtransition;
	private List<Token> consumedTokens;
		
	public TransitionFiringAction(TAPNTransition transition) {
		this(transition, new ArrayList<Token>());
	}
	
	public TransitionFiringAction(TAPNTransition transition, List<Token> consumedTokens) {
		firedtransition = transition;
		this.consumedTokens = consumedTokens;
	}
	
	public String transition(){
		return firedtransition.getName();
	}
	
	public List<Token> consumedTokens(){
		return consumedTokens;
	}
}
