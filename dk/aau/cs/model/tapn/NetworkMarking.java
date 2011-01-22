package dk.aau.cs.model.tapn;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import dk.aau.cs.util.Require;

public class NetworkMarking {
	private HashMap<TimedArcPetriNet, TimedMarking> markings = new HashMap<TimedArcPetriNet, TimedMarking>();
	
	public NetworkMarking(){
	}
	
	public TimedMarking getMarkingFor(TimedArcPetriNet tapn){
		return markings.get(tapn);
	}
	
	public void addMarking(TimedArcPetriNet tapn, TimedMarking marking){
		markings.put(tapn, marking);
	}
	
	public NetworkMarking clone(){
		return delay(BigDecimal.ZERO);
	}

	public void removeMarkingFor(TimedArcPetriNet tapn) {
		Require.that(tapn != null, "tapn must be non-null");
		
		if(markings.containsKey(tapn))
			markings.remove(tapn);
	}
	
	public boolean isDelayPossible(BigDecimal delay){
		for(TimedMarking marking : markings.values()){
			if(!marking.isDelayPossible(delay)) return false;
		}
		return true;
	}

	public NetworkMarking delay(BigDecimal amount) {
		Require.that(amount != null, "Delay must not be null");
		Require.that(isDelayPossible(amount), "Delay breaks invariant.");
		
		NetworkMarking newMarking = new NetworkMarking();
		HashMap<TimedArcPetriNet, TimedMarking> newMarkings = new HashMap<TimedArcPetriNet, TimedMarking>(markings.size());
		for(Entry<TimedArcPetriNet, TimedMarking> entry : markings.entrySet()){
			newMarkings.put(entry.getKey(), entry.getValue().delay(amount));
		}
		newMarking.markings = newMarkings;
		return newMarking;
	}
	
	public NetworkMarking fireTransition(TimedTransition transition){
		Require.that(transition != null, "transition cannot be null");
		
		NetworkMarking clone = clone();
		TimedMarking markingToEdit = clone.getMarkingFor(transition.model());
		
		
		
		
		return clone;
	}
}
