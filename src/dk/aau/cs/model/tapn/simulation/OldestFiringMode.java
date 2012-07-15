package dk.aau.cs.model.tapn.simulation;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import dk.aau.cs.model.tapn.TimedToken;
import dk.aau.cs.util.Require;

public class OldestFiringMode implements FiringMode {

	public List<TimedToken> pickTokensFrom(List<TimedToken> elligibleTokens, int numberOfTokensToPick) {
		Require.that(elligibleTokens.size() >= numberOfTokensToPick,
				"There has to be at least numberOfTokensToPick tokens");
		
		Collections.sort(elligibleTokens, new Comparator<TimedToken>() {
			public int compare(TimedToken o1, TimedToken o2) {
				//Sort decreasing
				return o1.age().compareTo(o2.age()) * -1;
			}
		});

		return elligibleTokens.subList(0, numberOfTokensToPick);
	}

	@Override
	public String toString() {
		return "Oldest";
	}
}
