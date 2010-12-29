package dk.aau.cs.model.tapn;

import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.util.Require;

public class TimedArcPetriNetNetwork {
	private List<TimedArcPetriNet> tapns;
	private NetworkMarking currentMarking;
	
	public TimedArcPetriNetNetwork(){
		tapns = new ArrayList<TimedArcPetriNet>();
		currentMarking = new NetworkMarking();
	}
	
	public void add(TimedArcPetriNet tapn){
		Require.that(tapn != null, "tapn must be non-null");
		Require.that(tapn.marking() != null, "Marking must be non-null");
		
		tapns.add(tapn);
		currentMarking.addMarking(tapn, tapn.marking());
	}
	
	public void remove(TimedArcPetriNet tapn){
		if(tapn != null){
			tapns.remove(tapn);
			currentMarking.removeMarkingFor(tapn);
			
		}
	}
	
	public List<TimedArcPetriNet> templates(){
		return tapns;
	}

	public boolean hasTAPNCalled(String newName) {
		for(TimedArcPetriNet tapn : tapns) if(tapn.getName().equals(newName)) return true;
		return false;
	}
}
