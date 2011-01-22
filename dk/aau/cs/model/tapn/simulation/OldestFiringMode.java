package dk.aau.cs.model.tapn.simulation;

import java.util.List;

import dk.aau.cs.model.tapn.TimedToken;
import dk.aau.cs.util.Require;

public class OldestFiringMode implements FiringMode {

	public TimedToken pickTokenFrom(List<TimedToken> elligibleTokens) {
		Require.that(elligibleTokens.size() > 0, "There has to be at least one token");
		
		TimedToken oldest = elligibleTokens.get(0);
		
		for(TimedToken token : elligibleTokens){
			if(token.age().compareTo(oldest.age()) > 0){
				oldest = token;
			}
		}
		
		return oldest;
	}
	
	@Override
	public String toString() {
		return "Oldest";
	}
}
