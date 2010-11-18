package dk.aau.cs.model.tapn;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class TimedMarking {
	private Hashtable<TimedPlace, List<TimedToken>> placesToTokensMap;

	public TimedMarking(){
		placesToTokensMap = new Hashtable<TimedPlace, List<TimedToken>>();
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
}
