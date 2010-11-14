package dk.aau.cs.model.tapn;

import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.util.Require;


public class TimedPlace {
	private String name;
	private TimeInvariant invariant;
	private List<TimedOutputArc> preset;
	private List<TimedInputArc> postset;
	private List<TransportArc> presetTransportArcs;
	private List<TransportArc> postsetTransportArcs;

	public TimedPlace(String name){
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
	
	public String name(){
		return name;
	}
	
	public void setName(String name){
		Require.that(name != null && !name.isEmpty(), "A timed place must have a valid name");
		
		this.name = name;
	}
	
	public TimeInvariant invariant(){
		return invariant;
	}
	
	public void setInvariant(TimeInvariant invariant){
		Require.that(invariant != null, "A timed place must have a non-null invariant");
		this.invariant = invariant;
	}
	
	public void addToPreset(TimedOutputArc arc){
		Require.that(arc != null, "Cannot add null to preset");
		preset.add(arc);
	}
	
	public void addToPreset(TransportArc arc) {
		Require.that(arc != null, "Cannot add null to preset");
		presetTransportArcs.add(arc);	
	}
	
	
	public void addToPostset(TimedInputArc arc){
		Require.that(arc != null, "Cannot add null to postset");
		postset.add(arc);
	}

	public void addToPostset(TransportArc arc) {
		Require.that(arc != null, "Cannot add null to postset");
		postsetTransportArcs.add(arc);	
	}
}
