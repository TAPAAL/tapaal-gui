package dk.aau.cs.model.tapn;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import dk.aau.cs.model.tapn.simulation.FiringMode;
import dk.aau.cs.util.Require;

public class NetworkMarking implements TimedMarking {
	private HashMap<TimedArcPetriNet, LocalTimedMarking> markings = new HashMap<TimedArcPetriNet, LocalTimedMarking>();
	private HashMap<TimedPlace, List<TimedToken>> sharedPlacesTokens = new HashMap<TimedPlace, List<TimedToken>>();

	public NetworkMarking() {
	}

	public void addMarking(TimedArcPetriNet tapn, LocalTimedMarking marking) {
		Require.that(tapn != null, "tapn must not be null");
		Require.that(!markings.containsKey(tapn), "There is already a marking for that tapn");

		marking.setNetworkMarking(this);
		markings.put(tapn, marking);
	}
	
	public void removeMarkingFor(TimedArcPetriNet tapn) {
		Require.that(tapn != null, "tapn must be non-null");

		if (markings.containsKey(tapn)){
			LocalTimedMarking marking = markings.remove(tapn);
			marking.setNetworkMarking(null);
		}
	}
	
	private LocalTimedMarking getMarkingFor(TimedArcPetriNet tapn) {
		return markings.get(tapn);
	}

	public NetworkMarking clone() {
		return delay(BigDecimal.ZERO);
	}

	private NetworkMarking shallowCopy() {
		NetworkMarking shallowCopy = new NetworkMarking();

		for (Entry<TimedArcPetriNet, LocalTimedMarking> entry : markings.entrySet()) {
			shallowCopy.markings.put(entry.getKey(), entry.getValue());
		}
		
		for(Entry<TimedPlace, List<TimedToken>> entry : sharedPlacesTokens.entrySet()){
			shallowCopy.sharedPlacesTokens.put(entry.getKey(), entry.getValue());
		}

		return shallowCopy;
	}

	public boolean isDelayPossible(BigDecimal delay) {
		for (LocalTimedMarking marking : markings.values()) {
			if (!marking.isDelayPossible(delay))
				return false;
		}
		return true;
	}

	public NetworkMarking delay(BigDecimal amount) {
		Require.that(amount != null, "Delay must not be null");
		Require.that(isDelayPossible(amount), "Delay breaks invariant.");

		NetworkMarking newMarking = new NetworkMarking();
		HashMap<TimedArcPetriNet, LocalTimedMarking> newMarkings = new HashMap<TimedArcPetriNet, LocalTimedMarking>(markings.size());
		for (Entry<TimedArcPetriNet, LocalTimedMarking> entry : markings.entrySet()) {
			newMarkings.put(entry.getKey(), entry.getValue().delay(amount));
		}
		newMarking.markings = newMarkings;
		return newMarking;
	}

	public NetworkMarking fireTransition(TimedTransition transition, FiringMode firingMode) {
		Require.that(transition != null, "transition cannot be null");

		NetworkMarking shallowCopy = shallowCopy(); // conserve memory by reusing unchanged markings (they are immutable wrt. transition firing and delay)
		LocalTimedMarking newMarking = getMarkingFor(transition.model()).fireTransition(transition, firingMode);

		shallowCopy.removeMarkingFor(transition.model());
		shallowCopy.addMarking(transition.model(), newMarking);

		return shallowCopy;
	}

	public NetworkMarking fireTransition(TimedTransition transition, List<TimedToken> tokensToConsume) {
		Require.that(transition != null, "transition cannot be null");
		Require.that(tokensToConsume != null, "Must specify a list of tokens");

		NetworkMarking shallowCopy = shallowCopy(); // conserve memory by reusing unchanged markings (they are immutable wrt. transition firing and delay)
		LocalTimedMarking newMarking = getMarkingFor(transition.model()).fireTransition(transition, tokensToConsume);

		shallowCopy.removeMarkingFor(transition.model());
		shallowCopy.addMarking(transition.model(), newMarking);

		return shallowCopy;
	}

	public int size() {
		int size = 0;
		for (LocalTimedMarking marking : markings.values()) {
			size += marking.size();
		}
		
		for(List<TimedToken> tokens : sharedPlacesTokens.values()){
			size += tokens.size();
		}
		return size;
	}

	public void add(TimedToken token) {
		if(token.place().isShared()){
			addTokenToSharedPlace(token);
		}else{
			getMarkingFor(((LocalTimedPlace)token.place()).model()).add(token); // TODO: ugly cast, but only way to get model?
		}
	}

	private void addTokenToSharedPlace(TimedToken token) {
		Require.that(token.place().isShared(), "Token must be located in a shared place");
		if(!sharedPlacesTokens.containsKey(token.place())){
			sharedPlacesTokens.put(token.place(), new ArrayList<TimedToken>());
		}

		sharedPlacesTokens.get(token.place()).add(token);
	}

	public List<TimedToken> getTokensFor(TimedPlace place){
		if(place.isShared()){
			if(!sharedPlacesTokens.containsKey(place)) return new ArrayList<TimedToken>();
			return sharedPlacesTokens.get(place);
		}else{
			LocalTimedPlace timedPlace = (LocalTimedPlace)place;
			return getMarkingFor(timedPlace.model()).getTokensFor(timedPlace);
		}
	}
	
	public void remove(TimedToken token) {
		TimedPlace place = token.place();
		if(place.isShared()){
			if(sharedPlacesTokens.containsKey(place)){
				sharedPlacesTokens.get(place).remove(token);
			}
		}else{
			getMarkingFor(((LocalTimedPlace)place).model()).remove(token); // TODO: this is ugly but only way to obtain the model?
		}
	}

	public void removePlaceFromMarking(TimedPlace place) {
		if(place.isShared()){
			if(sharedPlacesTokens.containsKey(place)){
				sharedPlacesTokens.remove(place);
			}
		}else{
			getMarkingFor(((LocalTimedPlace)place).model()).removePlaceFromMarking(place);	
		}
		
	}
}
