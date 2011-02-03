package dk.aau.cs.model.tapn.simulation;

import java.util.List;

import dk.aau.cs.model.tapn.TimedToken;
import dk.aau.cs.util.Require;

public class YoungestFiringMode implements FiringMode {

	public TimedToken pickTokenFrom(List<TimedToken> elligibleTokens) {
		Require.that(elligibleTokens.size() > 0,
				"There has to be at least one token");
		TimedToken youngest = elligibleTokens.get(0);

		for (TimedToken token : elligibleTokens) {
			if (token.age().compareTo(youngest.age()) < 0) {
				youngest = token;
			}
		}
		return youngest;
	}

	@Override
	public String toString() {
		return "Youngest";
	}
}
