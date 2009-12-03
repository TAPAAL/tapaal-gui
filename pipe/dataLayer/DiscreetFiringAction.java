package pipe.dataLayer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

public class DiscreetFiringAction implements FiringAction {

	Transition firedtransition;
	HashMap<Place, ArrayList<BigDecimal>> consumedTokens = new HashMap<Place, ArrayList<BigDecimal>>();
	
	public DiscreetFiringAction(Transition transition) {
		firedtransition= transition;
	}
	
	public void addConsumedToken(Place p, BigDecimal token){
		
		//XXX  - This will break if two tokens from the same place is consumed
		ArrayList<BigDecimal> tmp = new ArrayList<BigDecimal>();
		tmp.add(token);
		consumedTokens.put(p, tmp);
		
	}
	
	public HashMap<Place, ArrayList<BigDecimal>> getConsumedTokensList(){
		return consumedTokens;
	}



	public Transition getTransition() {
		
		return firedtransition;
	}
	
}
