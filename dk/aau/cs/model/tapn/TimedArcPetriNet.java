package dk.aau.cs.model.tapn;

import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.util.Require;

public class TimedArcPetriNet {
	
	private static int uniqueId = 0;
	private String name;
	
	private List<TimedPlace> places;
	private List<TimedTransition> transitions;
	private List<TimedInputArc> inputArcs;
	private List<TimedOutputArc> outputArcs;
	private List<TimedInhibitorArc> inhibitorArcs;
	private List<TransportArc> transportArcs;
	
	public TimedArcPetriNet(){
		this("New Timed Arc Petri Net" + uniqueId++);
	}
	
	public TimedArcPetriNet(String name) {
		this.name = name;
		places = new ArrayList<TimedPlace>();
		transitions = new ArrayList<TimedTransition>();
		inputArcs = new ArrayList<TimedInputArc>();
		outputArcs = new ArrayList<TimedOutputArc>();
		inhibitorArcs = new ArrayList<TimedInhibitorArc>();
		transportArcs = new ArrayList<TransportArc>();
	}
	
	public void add(TimedPlace place){
		Require.that(place != null, "Argument must be a non-null place");
		Require.that(!isNameUsed(place.name()), "A place or transition with the specified name already exists in the petri net.");

		places.add(place);
	}
	
	public void add(TimedTransition transition){
		Require.that(transition != null, "Argument must be a non-null transition");
		Require.that(!isNameUsed(transition.name()), "A place or transition with the specified name already exists in the petri net.");
		
		transitions.add(transition);
	}
	
	public void add(TimedInputArc arc){
		Require.that(arc != null, "Argument must be a non-null input arc.");
		Require.that(places.contains(arc.source()), "The source place must be part of the petri net.");
		Require.that(transitions.contains(arc.destination()), "The destination transition must be part of the petri net");
		Require.that(!inputArcs.contains(arc), "The specified arc is already a part of the petri net.");
		Require.that(!hasArcFromPlaceToTransition(arc.source(), arc.destination()), "Cannot have two arcs between the same place and transition");
		
		inputArcs.add(arc);
		arc.source().addToPostset(arc);
		arc.destination().addToPreset(arc);
	}
	
	public void add(TimedOutputArc arc){
		Require.that(arc != null, "Argument must be a non-null output arc.");
		Require.that(places.contains(arc.destination()), "The destination place must be part of the petri net.");
		Require.that(transitions.contains(arc.source()), "The source transition must be part of the petri net");
		Require.that(!outputArcs.contains(arc), "The specified arc is already a part of the petri net.");
		Require.that(!hasArcFromTransitionToPlace(arc.source(), arc.destination()), "Cannot have two arcs between the same transition and place");
		
		outputArcs.add(arc);
		arc.source().addToPostset(arc);
		arc.destination().addToPreset(arc);
	}
	
	public void add(TimedInhibitorArc arc){
		Require.that(arc != null, "Argument must be a non-null output arc.");
		Require.that(places.contains(arc.source()), "The source place must be part of the petri net.");
		Require.that(transitions.contains(arc.destination()), "The destination transition must be part of the petri net");
		Require.that(!inhibitorArcs.contains(arc), "The specified arc is already a part of the petri net.");
		Require.that(!hasArcFromPlaceToTransition(arc.source(), arc.destination()), "Cannot have two arcs between the same place and transition");
		
		inhibitorArcs.add(arc);
	}
	
	public void add(TransportArc arc){
		Require.that(arc != null, "Argument must be a non-null output arc.");
		Require.that(places.contains(arc.source()), "The source place must be part of the petri net.");
		Require.that(transitions.contains(arc.transition()), "The transition must be part of the petri net");
		Require.that(places.contains(arc.destination()), "The destination place must be part of the petri net.");
		Require.that(!inhibitorArcs.contains(arc), "The specified arc is already a part of the petri net.");
		Require.that(!hasArcFromPlaceToTransition(arc.source(), arc.transition()), "Cannot have two arcs between the same place and transition");
		Require.that(!hasArcFromTransitionToPlace(arc.transition(), arc.destination()), "Cannot have two arcs between the same transition and place");
		
		transportArcs.add(arc);
		arc.source().addToPostset(arc);
		arc.destination().addToPreset(arc);
	}
	
	private boolean hasArcFromPlaceToTransition(TimedPlace source,
			TimedTransition destination) {
		for(TimedInputArc arc : inputArcs) 
			if(arc.source().equals(source) && arc.destination().equals(destination)) return true;
		for(TimedInhibitorArc arc : inhibitorArcs) 
			if(arc.source().equals(source) && arc.destination().equals(destination)) return true;
		for(TransportArc arc : transportArcs) 
			if(arc.source().equals(source) && arc.destination().equals(destination)) return true;
		
		return false;
	}
	
	private boolean hasArcFromTransitionToPlace(TimedTransition destination,
			TimedPlace source) {
		for(TimedOutputArc arc : outputArcs) if(arc.source().equals(source) && arc.destination().equals(destination)) return true;
	
		return false;
	}
	
	private boolean isNameUsed(String name) {
		for(TimedPlace place : places) if (place.name().equals(name)) return true;
		for(TimedTransition transition : transitions) if(transition.name().equals(name)) return true;
		return false;
	}

	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return name;
	}

	public void setName(String newName) {
		if(name != null && name != "")
			name = newName;
	}
}
