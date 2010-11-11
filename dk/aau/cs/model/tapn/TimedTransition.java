package dk.aau.cs.model.tapn;

import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.util.Require;

public class TimedTransition {
	private String name;
	private List<TimedOutputArc> postset;
	private List<TimedInputArc> preset;
	
	public TimedTransition(String name){
		setName(name);
		preset = new ArrayList<TimedInputArc>();
		postset = new ArrayList<TimedOutputArc>();
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
}
