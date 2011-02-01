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

	private TimedMarking currentMarking;

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

		setCurrentMarking(new TimedMarking());
	}

	private void setCurrentMarking(TimedMarking marking) {
		currentMarking = marking;
		for(TimedPlace place : places){
			place.setCurrentMarking(marking);
		}
	}

	public void add(TimedPlace place){
		Require.that(place != null, "Argument must be a non-null place");
		Require.that(!isNameUsed(place.name()), "A place or transition with the specified name already exists in the petri net.");

		place.setModel(this);
		places.add(place);
		place.setCurrentMarking(currentMarking);
	}

	public void add(TimedTransition transition){
		Require.that(transition != null, "Argument must be a non-null transition");
		Require.that(!isNameUsed(transition.name()), "A place or transition with the specified name already exists in the petri net.");

		transition.setModel(this);
		transitions.add(transition);
	}

	public void add(TimedInputArc arc){
		Require.that(arc != null, "Argument must be a non-null input arc.");
		Require.that(places.contains(arc.source()), "The source place must be part of the petri net.");
		Require.that(transitions.contains(arc.destination()), "The destination transition must be part of the petri net");
		Require.that(!inputArcs.contains(arc), "The specified arc is already a part of the petri net.");
		Require.that(!hasArcFromPlaceToTransition(arc.source(), arc.destination()), "Cannot have two arcs between the same place and transition");

		arc.setModel(this);
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

		arc.setModel(this);
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

		arc.setModel(this);
		inhibitorArcs.add(arc);
		arc.destination().addInhibitorArc(arc);
	}

	public void add(TransportArc arc){
		Require.that(arc != null, "Argument must be a non-null output arc.");
		Require.that(places.contains(arc.source()), "The source place must be part of the petri net.");
		Require.that(transitions.contains(arc.transition()), "The transition must be part of the petri net");
		Require.that(places.contains(arc.destination()), "The destination place must be part of the petri net.");
		Require.that(!inhibitorArcs.contains(arc), "The specified arc is already a part of the petri net.");
		Require.that(!hasArcFromPlaceToTransition(arc.source(), arc.transition()), "Cannot have two arcs between the same place and transition");
		Require.that(!hasArcFromTransitionToPlace(arc.transition(), arc.destination()), "Cannot have two arcs between the same transition and place");

		arc.setModel(this);
		transportArcs.add(arc);
		arc.source().addToPostset(arc);
		arc.transition().addTransportArcGoingThrough(arc);
		arc.destination().addToPreset(arc);
	}

	public void addToken(TimedToken token){
		currentMarking.add(token);
	}

	public void removeToken(TimedToken token){
		currentMarking.remove(token);
	}

	public void remove(TimedPlace place){
		boolean removed = places.remove(place);
		if(removed) place.setModel(null);
	}

	public void remove(TimedTransition transition){
		boolean removed = transitions.remove(transition);
		if(removed) transition.setModel(null);
	}

	public void remove(TimedInputArc arc){
		boolean removed = inputArcs.remove(arc);
		if(removed){
			arc.setModel(null);
			arc.source().removeFromPostset(arc);
			arc.destination().removeFromPreset(arc);
		}
	}

	public void remove(TransportArc arc){
		boolean removed = transportArcs.remove(arc);
		if(removed){
			arc.setModel(null);
			arc.source().removeFromPostset(arc);
			arc.transition().removeTransportArcGoingThrough(arc);
			arc.destination().removeFromPreset(arc);
		}
	}

	public void remove(TimedOutputArc arc){
		boolean removed = outputArcs.remove(arc);
		if(removed){
			arc.setModel(null);
			arc.source().removeFromPostset(arc);
			arc.destination().removeFromPreset(arc);
		}
	}

	public void remove(TimedInhibitorArc arc){
		boolean removed = inhibitorArcs.remove(arc);
		if(removed){
			arc.setModel(null);
			arc.destination().removeInhibitorArc(arc);
		}
	}

	private boolean hasArcFromPlaceToTransition(TimedPlace source,
			TimedTransition destination) {
		for(TimedInputArc arc : inputArcs) 
			if(arc.source().equals(source) && arc.destination().equals(destination)) return true;
		for(TimedInhibitorArc arc : inhibitorArcs) 
			if(arc.source().equals(source) && arc.destination().equals(destination)) return true;
		for(TransportArc arc : transportArcs) 
			if(arc.source().equals(source) && arc.transition().equals(destination)) return true;

		return false;
	}

	private boolean hasArcFromTransitionToPlace(TimedTransition source,
			TimedPlace destination) {
		for(TimedOutputArc arc : outputArcs) if(arc.source().equals(source) && arc.destination().equals(destination)) return true;
		for(TransportArc arc : transportArcs) if(arc.transition().equals(source) && arc.destination().equals(destination)) return true;
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
	
	public TimedMarking marking(){
		return currentMarking;
	}

	public void setName(String newName) {
		if(name != null && name != "")
			name = newName;
	}
	
	public TimedPlace getPlaceByName(String placeName) {
		for(TimedPlace p : places) {
			if(p.name() == placeName) {
				return p;
			}
		}
		return null;
	}

	public TimedTransition getTransitionByName(String transitionName) {
		for(TimedTransition t : transitions) {
			if(t.name() == transitionName) {
				return t;
			}
		}
		return null;
	}

	public void setMarking(TimedMarking marking) {
		this.currentMarking = marking;
		
		for (TimedPlace p : places) {
			p.setCurrentMarking(marking);
		}
	}

	public Iterable<TimedPlace> places() {
		return places;
	}

	public Iterable<TimedTransition> transitions() {
		return transitions;
	}

	public Iterable<TimedInputArc> inputArcs() {
		return inputArcs;
	}

	public Iterable<TimedOutputArc> outputArcs() {
		return outputArcs;
	}
	
	public Iterable<TransportArc> transportArcs() {
		return transportArcs;
	}
	
	public Iterable<TimedInhibitorArc> inhibitorArcs() {
		return inhibitorArcs;
	}
}
