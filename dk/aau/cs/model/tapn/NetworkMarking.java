package dk.aau.cs.model.tapn;

import java.util.HashMap;
import java.util.Map.Entry;

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
		NetworkMarking clone = new NetworkMarking();
		HashMap<TimedArcPetriNet, TimedMarking> newMarkings = new HashMap<TimedArcPetriNet, TimedMarking>(markings.size());
		for(Entry<TimedArcPetriNet, TimedMarking> entry : markings.entrySet()){
			newMarkings.put(entry.getKey(), entry.getValue().clone());
		}
		clone.markings = newMarkings;
		return clone;
	}
}
