package pipe.dataLayer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pipe.dataLayer.simulation.Token;

public class DiscreetFiringAction implements FiringAction {

	private Transition firedtransition;
	private HashMap<Place, ArrayList<BigDecimal>> consumedTokensHashMap = new HashMap<Place, ArrayList<BigDecimal>>();
	private List<Token> consumedTokens;
	
	
	public DiscreetFiringAction(Transition transition) {
		this(transition, null);
	}
	
	public DiscreetFiringAction(Transition transition, List<Token> consumedTokens) {
		firedtransition = transition;
		this.consumedTokens = consumedTokens;
	}
	
	public void addConsumedToken(Place p, BigDecimal token){
		
		//XXX  - This will break if two tokens from the same place is consumed
		ArrayList<BigDecimal> tmp = new ArrayList<BigDecimal>();
		tmp.add(token);
		consumedTokensHashMap.put(p, tmp);
		
	}
	
	public HashMap<Place, ArrayList<BigDecimal>> getConsumedTokensList(){
		return consumedTokensHashMap;
	}



	public Transition getTransition() {
		
		return firedtransition;
	}
	
	@Override
	public String toString() {
		return firedtransition.getName();
	}
	
}
