package pipe.dataLayer.simulation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.Place;
import pipe.dataLayer.TimedPlace;

public class Marking {
	private HashMap<Place, List<Token>> tokens;

	public Marking(List<Token> tokenList){
		this.tokens = new HashMap<Place, List<Token>>();

		for(Token token : tokenList){
			addToken(token);
		}
	}

	public Marking(){
		tokens = new HashMap<Place, List<Token>>();
	}

	public void addToken(Token token) {
		if(!tokens.containsKey(token.place())){
			tokens.put(token.place(), new ArrayList<Token>());
		}

		tokens.get(token.place()).add(token);
	}

	public void removeToken(Token token){
		tokens.get(token.place()).remove(token);
	}


	public List<Token> getTokensInPlace(Place place){
		if(!tokens.containsKey(place)){
			return new ArrayList<Token>();
		}else{
			return tokens.get(place);
		}
	}

	public Marking timeDelay(BigDecimal delay){
		Marking marking = new Marking();
		for(List<Token> tokenList : tokens.values()){
			for(Token token : tokenList){
				marking.addToken(new Token(token.place(), token.age().add(delay)));
			}
		}

		return marking;
	}

	public static Marking For(DataLayer tapn) {
		Marking marking = new Marking();
		for(Place place : tapn.getPlaces()){
			for(int i = 0; i < place.getCurrentMarking(); i++){
				marking.addToken(new Token((TimedPlace)place, BigDecimal.ZERO));
			}
		}

		return marking;
	}
}
