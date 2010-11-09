package dk.aau.cs.model.petriNet;

import java.util.ArrayList;
import java.util.List;

public class Transition {
	private String name;
	private List<InputArc> preset;
	private List<OutputArc> postset;
	
	public Transition(String name){
		this.name = name;
		this.preset = new ArrayList<InputArc>();
		this.postset = new ArrayList<OutputArc>();
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String name() {
		return name;
	}
	
	public void addToPreset(InputArc arc){
		preset.add(arc);
	}
	
	public void addToPostset(OutputArc arc){
		postset.add(arc);
	}
}
