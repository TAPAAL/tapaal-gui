package dk.aau.cs.model.tapn.simulation;

import java.util.List;
import java.util.Random;

import dk.aau.cs.model.tapn.TimedToken;
import dk.aau.cs.util.Require;

public class RandomFiringMode implements FiringMode {
	private Random random = new Random();
	
	public TimedToken pickTokenFrom(List<TimedToken> elligibleTokens) {
		Require.that(elligibleTokens.size() > 0, "Must contain at least one token");
		
		int index = random.nextInt(elligibleTokens.size());
		return elligibleTokens.get(index);
	}
	
	@Override
	public String toString() {
		return "Random";
	}

}
