package dk.aau.cs.model.petriNet;

import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.util.Require;

public class Transition {
	private String name;
	private List<InputArc> preset;
	private List<OutputArc> postset;
	
	public Transition(String name){
		setName(name);
		this.preset = new ArrayList<InputArc>();
		this.postset = new ArrayList<OutputArc>();
	}
	
	public void setName(String name) {
		Require.that(name != null && !name.isEmpty(), "A transition must have a name");
		this.name = name;
	}

	public String name() {
		return name;
	}
	
	public void addToPreset(InputArc arc){
		Require.that(arc != null, "Cannot add null to preset");
		preset.add(arc);
	}
	
	public void addToPostset(OutputArc arc){
		Require.that(arc != null, "Cannot add null to postset");
		postset.add(arc);
	}
	
	
}
