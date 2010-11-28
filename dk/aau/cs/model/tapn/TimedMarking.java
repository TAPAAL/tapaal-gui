package dk.aau.cs.model.tapn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class TimedMarking {
	private HashMap<TimedPlace, List<TimedToken>> placesToTokensMap;

	public TimedMarking(){
		placesToTokensMap = new HashMap<TimedPlace, List<TimedToken>>();
	}

	public void add(TimedPlace place, TimedToken token){
		if(!placesToTokensMap.containsKey(place)){
			placesToTokensMap.put(place, new ArrayList<TimedToken>());
		}

		List<TimedToken> tokens = placesToTokensMap.get(place);
		tokens.add(token);
	}

	public void remove(TimedPlace place, TimedToken token){
		if(placesToTokensMap.containsKey(place)){
			List<TimedToken> tokens = placesToTokensMap.get(place);
			tokens.remove(token);
		}
	}
	
	public List<TimedToken> getTokensFor(TimedPlace place){
		if(!placesToTokensMap.containsKey(place)) return new ArrayList<TimedToken>();
		return placesToTokensMap.get(place);
	}

	public void removeArbitraryTokenFrom(TimedPlace timedPlace) {
		placesToTokensMap.get(timedPlace).remove(0);
	}
	
	public TimedMarking clone(){
		TimedMarking clone = new TimedMarking();
		HashMap<TimedPlace, List<TimedToken>> newMap = new HashMap<TimedPlace, List<TimedToken>>(placesToTokensMap.size());
		for(Entry<TimedPlace, List<TimedToken>> entry : placesToTokensMap.entrySet()){
			ArrayList<TimedToken> newTokens = new ArrayList<TimedToken>(entry.getValue().size());
			for(TimedToken token : entry.getValue()){
				newTokens.add(token.clone());
			}
			newMap.put(entry.getKey(), newTokens);
		}
		
		clone.placesToTokensMap = newMap;
		return clone;
	}

}
