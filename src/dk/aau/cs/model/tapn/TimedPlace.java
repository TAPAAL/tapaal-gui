package dk.aau.cs.model.tapn;

import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.util.Require;

public class TimedPlace extends TAPNElement {
	private String name;
	private TimeInvariant invariant;
	private List<TimedOutputArc> preset;
	private List<TimedInputArc> postset;
	private List<TransportArc> presetTransportArcs;
	private List<TransportArc> postsetTransportArcs;

	private TimedMarking currentMarking;

	public TimedPlace(String name) {
		this(name, TimeInvariant.LESS_THAN_INFINITY);
	}

	public TimedPlace(String name, TimeInvariant invariant) {
		setName(name);
		setInvariant(invariant);
		preset = new ArrayList<TimedOutputArc>();
		postset = new ArrayList<TimedInputArc>();
		presetTransportArcs = new ArrayList<TransportArc>();
		postsetTransportArcs = new ArrayList<TransportArc>();
	}

	public void setCurrentMarking(TimedMarking currentMarking) {
		this.currentMarking = currentMarking;
	}

	public String name() {
		return name;
	}

	public void setName(String name) {
		Require.that(name != null && !name.isEmpty(),
				"A timed place must have a valid name");

		this.name = name;
	}

	public TimeInvariant invariant() {
		return invariant;
	}

	public void setInvariant(TimeInvariant invariant) {
		Require.that(invariant != null,
				"A timed place must have a non-null invariant");
		this.invariant = invariant;
	}

	public void addToPreset(TimedOutputArc arc) {
		Require.that(arc != null, "Cannot add null to preset");
		preset.add(arc);
	}

	public void addToPreset(TransportArc arc) {
		Require.that(arc != null, "Cannot add null to preset");
		presetTransportArcs.add(arc);
	}

	public void addToPostset(TimedInputArc arc) {
		Require.that(arc != null, "Cannot add null to postset");
		postset.add(arc);
	}

	public void addToPostset(TransportArc arc) {
		Require.that(arc != null, "Cannot add null to postset");
		postsetTransportArcs.add(arc);
	}

	public boolean hasTokenSatisfyingInterval(TimeInterval interval) {
		List<TimedToken> tokens = currentMarking.getTokensFor(this);

		for (TimedToken t : tokens) {
			if (interval.isIncluded(t.age()))
				return true;
		}

		return false;
	}

	public List<TimedToken> tokensSatisfyingInterval(TimeInterval interval) {
		List<TimedToken> tokens = currentMarking.getTokensFor(this);
		ArrayList<TimedToken> toReturn = new ArrayList<TimedToken>();
		for (TimedToken t : tokens) {
			if (interval.isIncluded(t.age()))
				toReturn.add(t);
		}

		return toReturn;
	}

	public List<TimedToken> tokens() {
		return currentMarking.getTokensFor(this);
	}

	public int numberOfTokens() {
		return currentMarking.getTokensFor(this).size();
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof TimedPlace))
			return false;
		TimedPlace other = (TimedPlace) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	public void removeFromPostset(TimedInputArc arc) {
		postset.remove(arc);
	}

	public void removeFromPostset(TransportArc arc) {
		postsetTransportArcs.remove(arc);
	}

	public void removeFromPreset(TransportArc arc) {
		presetTransportArcs.remove(arc);
	}

	public void removeFromPreset(TimedOutputArc arc) {
		preset.remove(arc);
	}

	public void delete() {
		model().remove(this);
	}

	public void addToken(TimedToken timedToken) {
		currentMarking.add(timedToken);
	}

	public void removeToken(TimedToken timedToken) {
		currentMarking.remove(timedToken);
	}

	public void removeToken() {
		if (numberOfTokens() > 0) {
			currentMarking.removeArbitraryTokenFrom(this);
		}
	}

}
