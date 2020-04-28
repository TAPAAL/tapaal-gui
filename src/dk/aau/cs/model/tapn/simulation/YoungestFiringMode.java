package dk.aau.cs.model.tapn.simulation;

import java.util.Comparator;
import java.util.List;

import dk.aau.cs.model.tapn.TimedToken;
import dk.aau.cs.util.Require;

public class YoungestFiringMode implements FiringMode {

	public List<TimedToken> pickTokensFrom(List<TimedToken> elligibleTokens, int numberOfTokensToPick) {
		Require.that(elligibleTokens.size() >= numberOfTokensToPick,
				"There has to be at least numberOfTokensToPick tokens");
		
		elligibleTokens.sort(Comparator.comparing(TimedToken::age));

		return elligibleTokens.subList(0, numberOfTokensToPick);
	}

	@Override
	public String toString() {
		return "Youngest";
	}
}
