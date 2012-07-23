package dk.aau.cs.model.tapn.simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dk.aau.cs.model.tapn.TimedToken;
import dk.aau.cs.util.Require;

public class RandomFiringMode implements FiringMode {
	private Random random = new Random();

	public List<TimedToken> pickTokensFrom(List<TimedToken> elligibleTokens, int numberOfTokensToPick) {
		Require.that(elligibleTokens.size() > 0,
				"Must contain at least one token");
		ArrayList<TimedToken> tokensToPick = new ArrayList<TimedToken>();

		for(int i = 0; i < numberOfTokensToPick; i++){
			int index = random.nextInt(elligibleTokens.size());
			tokensToPick.add(elligibleTokens.get(index));
			elligibleTokens.remove(index);
		}
		
		return tokensToPick;
	}

	@Override
	public String toString() {
		return "Random";
	}

}
