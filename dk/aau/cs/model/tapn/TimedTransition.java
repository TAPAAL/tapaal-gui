package dk.aau.cs.model.tapn;

import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.util.Require;

public class TimedTransition extends TAPNElement {
	private String name;
	private List<TimedOutputArc> postset;
	private List<TimedInputArc> preset;
	private List<TransportArc> transportArcsGoingThrough;
	private List<TimedInhibitorArc> inhibitorArcs;
	
	public TimedTransition(String name){
		setName(name);
		preset = new ArrayList<TimedInputArc>();
		postset = new ArrayList<TimedOutputArc>();
		transportArcsGoingThrough = new ArrayList<TransportArc>();
	}
	
	public void setName(String name){
		Require.that(name != null && !name.isEmpty(), "A timed transition must have a name");
		this.name = name;
	}
	
	public String name(){
		return name;
	}
	
	public void addToPreset(TimedInputArc arc){
		Require.that(arc != null, "Cannot add null to preset");
		preset.add(arc);
	}
	
	public void addToPostset(TimedOutputArc arc){
		Require.that(arc != null, "Cannot add null to postset");
		postset.add(arc);
	}
	
	public boolean isEnabled() {
		for(TimedInputArc arc : preset) {
			if(!arc.isEnabled()) return false;
		}
		for(TransportArc arc : transportArcsGoingThrough){
			if(!arc.isEnabled()) return false;
		}
		for(TimedInhibitorArc arc : inhibitorArcs){
			if(!arc.isEnabled()) return false;
		}
		return true;
	}

	public void removeFromPreset(TimedInputArc arc) {
		preset.remove(arc);
	}

	public void addTransportArcGoingThrough(TransportArc arc) {
		Require.that(arc != null, "Cannot add null to preset");
		transportArcsGoingThrough.add(arc);
	}

	public void removeTransportArcGoingThrough(TransportArc arc) {
		transportArcsGoingThrough.remove(arc);		
	}

	public void removeFromPostset(TimedOutputArc arc) {
		postset.remove(arc);		
	}
	
	public void addInhibitorArc(TimedInhibitorArc arc){
		inhibitorArcs.add(arc);
	}

	public void removeInhibitorArc(TimedInhibitorArc arc) {
		inhibitorArcs.remove(arc);
	}

	@Override
	public void delete() {
		model().remove(this);	
	}
}
