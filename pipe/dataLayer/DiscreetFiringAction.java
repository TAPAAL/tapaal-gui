package pipe.dataLayer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pipe.dataLayer.simulation.Token;

public class DiscreetFiringAction implements FiringAction {

	private Transition firedtransition;
	private List<Token> consumedTokens;
	
	
	public DiscreetFiringAction(Transition transition) {
		this(transition, new ArrayList<Token>());
	}
	
	public DiscreetFiringAction(Transition transition, List<Token> consumedTokens) {
		firedtransition = transition;
		this.consumedTokens = consumedTokens;
	}
	
	public void addConsumedToken(Place p, BigDecimal token){
		consumedTokens.add(new Token((TimedPlaceComponent)p, token));
	}
	
	public HashMap<Place, ArrayList<BigDecimal>> getConsumedTokensList(){
		HashMap<Place, ArrayList<BigDecimal>> map = new HashMap<Place, ArrayList<BigDecimal>>();
		for(Token token : consumedTokens){
			if(!map.containsKey(token.place())){
				map.put(token.place(), new ArrayList<BigDecimal>());
			}
			
			map.get(token.place()).add(token.age());
		}
		
		return map;
	}



	public Transition getTransition() {
		
		return firedtransition;
	}
	
	@Override
	public String toString() {
		return firedtransition.getName();
	}
	
}
