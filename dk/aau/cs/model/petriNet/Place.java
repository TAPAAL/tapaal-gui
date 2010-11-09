package dk.aau.cs.model.petriNet;

import java.util.ArrayList;
import java.util.List;

public class Place {
	private String name;
	private List<OutputArc> preset;
	private List<InputArc> postset;

	public Place(String name){
		this.name = name;
		preset = new ArrayList<OutputArc>();
		postset = new ArrayList<InputArc>();
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String name() {
		return name;
	}
	
	public void addToPreset(OutputArc arc){
		preset.add(arc);
	}
	
	public void addToPostset(InputArc arc){
		postset.add(arc);
	}
}
