package pipe.dataLayer;

import java.util.HashMap;

import pipe.dataLayer.colors.ColoredTimedPlace;
import pipe.dataLayer.colors.ColoredToken;

public class ColoredDiscreteFiringAction implements FiringAction {
	private HashMap<ColoredTimedPlace, ColoredToken> consumedTokens = new HashMap<ColoredTimedPlace, ColoredToken>();
	private HashMap<ColoredTimedPlace, ColoredToken> producedTokens = new HashMap<ColoredTimedPlace, ColoredToken>();
	private TAPNTransition transition;
	
	public ColoredDiscreteFiringAction(TAPNTransition transition){
		this.transition = transition;
	}
		
	public void addConsumedToken(ColoredTimedPlace place, ColoredToken token){
		consumedTokens.put(place, token);
	}
	
	public void addProducedToken(ColoredTimedPlace place, ColoredToken token){
		producedTokens.put(place, token);
	}
	
	public HashMap<ColoredTimedPlace, ColoredToken> getConsumedTokens(){
		return consumedTokens;
	}
	
	public TAPNTransition getTransition(){
		return transition;
	}

	public HashMap<ColoredTimedPlace, ColoredToken> getProducedTokens() {
		return producedTokens;
	}
}
