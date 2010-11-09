package dk.aau.cs.model.petriNet;

import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.util.Require;

public class PetriNet {
	private List<Place> places;
	private List<Transition> transitions;
	private List<InputArc> inputArcs;
	private List<OutputArc> outputArcs;
	
	public PetriNet(){
		places = new ArrayList<Place>();
		transitions = new ArrayList<Transition>();
		inputArcs = new ArrayList<InputArc>();
		outputArcs = new ArrayList<OutputArc>();
	}
	
	public void add(Place place){
		Require.that(!isNameUsed(place.name()), "A place or transition with the specified name already exists in the petri net.");

		places.add(place);
	}
	
	public void add(Transition transition){
		Require.that(!isNameUsed(transition.name()), "A place or transition with the specified name already exists in the petri net.");
		
		transitions.add(transition);
	}
	
	public void add(InputArc arc){
		Require.that(places.contains(arc.source()), "The source place must be part of the petri net.");
		Require.that(transitions.contains(arc.destination()), "The destination transition must be part of the petri net");
		Require.that(!inputArcs.contains(arc), "The specified arc is already a part of the petri net.");
		
		inputArcs.add(arc);
		arc.source().addToPostset(arc);
		arc.destination().addToPreset(arc);
	}
	
	public void add(OutputArc arc){
		Require.that(places.contains(arc.destination()), "The destination place must be part of the petri net.");
		Require.that(transitions.contains(arc.source()), "The source transition must be part of the petri net");
		Require.that(!outputArcs.contains(arc), "The specified arc is already a part of the petri net.");
		
		outputArcs.add(arc);
		arc.source().addToPreset(arc);
		arc.destination().addToPostset(arc);
	}
	
	private boolean isNameUsed(String name) {
		for(Place place : places) if (place.name().equals(name)) return true;
		for(Transition transition : transitions) if(transition.name().equals(name)) return true;
		return false;
	}
}
