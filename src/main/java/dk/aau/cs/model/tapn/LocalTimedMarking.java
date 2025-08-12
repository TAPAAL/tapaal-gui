package dk.aau.cs.model.tapn;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import dk.aau.cs.model.NTA.trace.TraceToken;
import dk.aau.cs.model.tapn.simulation.FiringMode;
import dk.aau.cs.util.Require;
import dk.aau.cs.util.Tuple;

// This class must remain immutable wrt. delays and transition firings!
public class LocalTimedMarking implements TimedMarking { // TODO: Consider removing the interface here?
	private final HashMap<TimedPlace, List<TimedToken>> placesToTokensMap;
	private NetworkMarking parent;

	public LocalTimedMarking() {
		placesToTokensMap = new HashMap<TimedPlace, List<TimedToken>>();
	}

	public void setNetworkMarking(NetworkMarking marking){
		parent = marking;
	}

    public NetworkMarking getNetworkMarking() {
        return parent;
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
			if(!tokens.remove(token) && token instanceof TraceToken){
				for(TimedToken t : tokens){
					if(t.age().compareTo(token.age()) >= 0){
						tokens.remove(t);
						break;
					}
				}
			}
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
            placesToTokensMap.remove(place);
		}
	}

	private List<TimedToken> getTokensFor(LocalTimedPlace place) {
		if (!placesToTokensMap.containsKey(place)) {
            return new ArrayList<TimedToken>();
        }
		return placesToTokensMap.get(place);
	}

    public Map<TimedPlace, List<TimedToken>> getPlacesToTokensMap() {
        return placesToTokensMap;
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
	
	public List<TimedPlace> getBlockingPlaces(BigDecimal delay){
		Require.that(delay.compareTo(BigDecimal.ZERO) >= 0, "cannot delay with negative numbers");
		List<TimedPlace> result = new ArrayList<TimedPlace>();
		
		for (Entry<TimedPlace, List<TimedToken>> entry : placesToTokensMap.entrySet()) {
			boolean blocked = false;
			for (TimedToken token : entry.getValue()) {
				TimeInvariant invariant = token.place().invariant();
				if (!invariant.isSatisfied(token.age().add(delay))) {
					blocked = true;
				}
			}
			if (blocked){
				result.add(entry.getKey());
			}
		}
		return result;
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

	public Tuple<LocalTimedMarking, List<TimedToken>> fireTransition(TimedTransition transition, FiringMode firingMode) {
		Require.that(transition != null, "transition must not be null");
		Require.that(transition.isEnabled(), "Transition must be enabled");

		List<TimedToken> tokensToConsume = transition.calculateConsumedTokens(firingMode);

		return new Tuple<>(fireTransition(transition, tokensToConsume), tokensToConsume);
	}

	public int size() {
		int size = 0;
		for (List<TimedToken> tokens : placesToTokensMap.values()) {
			size += tokens.size();
		}
		return size;
	}

	/**
	 * Removes all tokens from marking
	 */
	public void clear() {
		placesToTokensMap.clear();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj)	return true;
		if(!(obj instanceof LocalTimedMarking))	return false;
		LocalTimedMarking other = (LocalTimedMarking) obj;
		
		if(placesToTokensMap.size() != other.placesToTokensMap.size())	return false;
		for(TimedPlace key : placesToTokensMap.keySet()){
			if(other.placesToTokensMap.get(key) == null)	return false;
			if(other.placesToTokensMap.get(key).size() != placesToTokensMap.get(key).size())	return false;
			int i = 0;
			for(TimedToken t : other.placesToTokensMap.get(key)){
				if(!t.equals(placesToTokensMap.get(key).get(i++)))	return false;
			}
		}
		
		return true;
	}

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Entry<TimedPlace, List<TimedToken>> entry : placesToTokensMap.entrySet()) {
            sb.append(entry.getKey().name()).append(" -> ");
            for (TimedToken token : entry.getValue()) {
                sb.append(token.toString()).append(" ");
            }
            
            sb.append("\n");
        }

        return sb.toString();
    }
}
