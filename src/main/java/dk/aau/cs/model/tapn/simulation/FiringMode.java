package dk.aau.cs.model.tapn.simulation;

import java.util.List;

import dk.aau.cs.model.tapn.TimedToken;

public interface FiringMode {
	List<TimedToken> pickTokensFrom(List<TimedToken> elligibleTokens, int numberOfTokensToPick);
}
