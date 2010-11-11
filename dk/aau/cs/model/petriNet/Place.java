package dk.aau.cs.model.petriNet;

import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.util.Require;

public class Place {
	private String name;
	private List<OutputArc> preset;
	private List<InputArc> postset;

	public Place(String name){
		setName(name);
		preset = new ArrayList<OutputArc>();
		postset = new ArrayList<InputArc>();
	}
	
	public void setName(String name) {
		Require.that(name != null && !name.isEmpty(), "A place must have a name");
		this.name = name;
	}

	public String name() {
		return name;
	}
	
	public void addToPreset(OutputArc arc){
		Require.that(arc != null, "Cannot add null to preset");
		preset.add(arc);
	}
	
	public void addToPostset(InputArc arc){
		Require.that(arc != null, "Cannot add null to postset");
		postset.add(arc);
	}
}
