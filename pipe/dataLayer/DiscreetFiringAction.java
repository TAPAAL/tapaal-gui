package pipe.dataLayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DiscreetFiringAction implements FiringAction {

	Transition firedtransition;
	HashMap<Place, ArrayList<Float>> consumedTokens = new HashMap<Place, ArrayList<Float>>();
	
	public DiscreetFiringAction(Transition transition) {
		firedtransition= transition;
	}

	
	
	public void addConsumedToken(Place p, Float token){
		
		//XXX  - This will break if two tokens from the same place is consumed
		ArrayList<Float> tmp = new ArrayList<Float>();
		tmp.add(token);
		consumedTokens.put(p, tmp);
		
	}
	
	public HashMap<Place, ArrayList<Float>> getConsumedTokensList(){
		return consumedTokens;
	}



	public Transition getTransition() {
		
		return firedtransition;
	}
	
}
