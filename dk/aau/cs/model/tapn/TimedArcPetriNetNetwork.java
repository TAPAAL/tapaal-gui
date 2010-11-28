package dk.aau.cs.model.tapn;

import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.util.Require;

public class TimedArcPetriNetNetwork {
	private List<TimedArcPetriNet> tapns;
	
	public TimedArcPetriNetNetwork(){
		tapns = new ArrayList<TimedArcPetriNet>();
		tapns.add(new TimedArcPetriNet());
	}
	
	public void add(TimedArcPetriNet tapn){
		Require.that(tapn != null, "tapn must be non-null");
		
		tapns.add(tapn);
	}
	
	public void remove(TimedArcPetriNet tapn){
		if(tapn != null){
			tapns.remove(tapn);
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
