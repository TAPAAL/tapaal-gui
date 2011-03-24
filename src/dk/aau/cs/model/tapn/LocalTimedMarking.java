package dk.aau.cs.model.tapn;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import dk.aau.cs.model.tapn.simulation.FiringMode;
import dk.aau.cs.util.Require;

// This class must remain immutable wrt. delays and transition firings!
public class LocalTimedMarking implements TimedMarking { // TODO: Consider removing the interface here?
	private HashMap<TimedPlace, List<TimedToken>> placesToTokensMap;
	private NetworkMarking parent;

	public LocalTimedMarking() {
		placesToTokensMap = new HashMap<TimedPlace, List<TimedToken>>();
	}

	public void setNetworkMarking(NetworkMarking marking){
		this.parent = marking;
	}

	public void add(TimedToken token) {
		if(token.place().isShared()) {
			parent.add(token);
		} else {
			if (!placesToTokensMap.containsKey(token.place())) {
				placesToTokensMap.put(token.place(), new ArrayList<TimedToken>());
			}

			List<TimedToken> tokens = placesToTokensMap.get(token.place());
			tokens.add(token);
		}
	}

	private void add(List<TimedToken> producedTokens) {
		for (TimedToken token : producedTokens) {
			add(token);
		}
	}

	public void remove(TimedToken token) {
		if(token.place().isShared()) {
			parent.remove(token);
		} else if (placesToTokensMap.containsKey(token.place())) {
			List<TimedToken> tokens = placesToTokensMap.get(token.place());
			tokens.remove(token);
		}
	}


	private void remove(List<TimedToken> tokensToConsume) {
		for (TimedToken token : tokensToConsume) {
			remove(token);
		}
	}

	public void removePlaceFromMarking(TimedPlace place) {
		if(place.isShared()){
			parent.removePlaceFromMarking(place);
		}else{
			if(placesToTokensMap.containsKey(place)){
				placesToTokensMap.remove(place);
			}
		}
	}

	private List<TimedToken> getTokensFor(LocalTimedPlace place) {
		if (!placesToTokensMap.containsKey(place))
			return new ArrayList<TimedToken>();
		return placesToTokensMap.get(place);
	}

	public List<TimedToken> getTokensFor(TimedPlace place){
		if(place.isShared()){
			return parent.getTokensFor(place);
		}else{
			return getTokensFor((LocalTimedPlace)place);
		}
	}

	public boolean isDelayPossible(BigDecimal delay) {
		Require.that(delay.compareTo(BigDecimal.ZERO) >= 0, "cannot delay with negative numbers");
		for (Entry<TimedPlace, List<TimedToken>> entry : placesToTokensMap.entrySet()) {
			for (TimedToken token : entry.getValue()) {
				TimeInvariant invariant = token.place().invariant();
				if (!invariant.isSatisfied(token.age().add(delay))) {
					return false;
				}
			}
		}
		return true;
	}

	public LocalTimedMarking clone() {
		return delay(BigDecimal.ZERO);
	}

	public LocalTimedMarking delay(BigDecimal amount) {
		Require.that(amount.compareTo(BigDecimal.ZERO) >= 0, "cannot delay with negative numbers");
		Require.that(isDelayPossible(amount), "The specified delay is not possible due to an invariant.");

		LocalTimedMarking clone = new LocalTimedMarking();
		for (Entry<TimedPlace, List<TimedToken>> entry : placesToTokensMap.entrySet()) {
			ArrayList<TimedToken> newTokens = new ArrayList<TimedToken>(entry.getValue().size());
			for (TimedToken token : entry.getValue()) {
				newTokens.add(token.delay(amount));
			}
			clone.placesToTokensMap.put(entry.getKey(), newTokens);
		}
		
		clone.parent = parent;
		return clone;
	}

	public LocalTimedMarking fireTransition(TimedTransition transition, List<TimedToken> tokensToConsume) {
		Require.that(transition != null, "transition must not be null");
		Require.that(transition.isEnabledBy(tokensToConsume), "Tokens do not enable transition");

		LocalTimedMarking clone = clone();

		List<TimedToken> producedTokens = transition.calculateProducedTokensFrom(tokensToConsume);

		clone.remove(tokensToConsume);
		clone.add(producedTokens);

		return clone;
	}

	public LocalTimedMarking fireTransition(TimedTransition transition, FiringMode firingMode) {
		Require.that(transition != null, "transition must not be null");
		Require.that(transition.isEnabled(), "Transition must be enabled");

		List<TimedToken> tokensToConsume = transition.calculateConsumedTokens(this, firingMode);

		return fireTransition(transition, tokensToConsume);
	}

	public int size() {
		int size = 0;
		for (List<TimedToken> tokens : placesToTokensMap.values()) {
			size += tokens.size();
		}
		return size;
	}
}
