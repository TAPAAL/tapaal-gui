package dk.aau.cs.model.tapn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import pipe.gui.undo.AddArcPathPointEdit;

import dk.aau.cs.util.Require;

public class TimedArcPetriNet {
	private String name;
	private TimedArcPetriNetNetwork parentNetwork;
	private boolean isActive;

	private List<TimedPlace> places = new ArrayList<TimedPlace>();
	private List<TimedTransition> transitions = new ArrayList<TimedTransition>();
	private List<TimedInputArc> inputArcs = new ArrayList<TimedInputArc>();
	private List<TimedOutputArc> outputArcs = new ArrayList<TimedOutputArc>();
	private List<TimedInhibitorArc> inhibitorArcs = new ArrayList<TimedInhibitorArc>();
	private List<TransportArc> transportArcs = new ArrayList<TransportArc>();

	private TimedMarking currentMarking;

	public TimedArcPetriNet(String name) {
		setName(name);
		setMarking(new LocalTimedMarking());
		isActive = true;
	}

	public TimedMarking marking(){
		return currentMarking;
	}

	public void setParentNetwork(TimedArcPetriNetNetwork network){
		parentNetwork = network;
	}

	public TimedArcPetriNetNetwork parentNetwork(){
		return parentNetwork;
	}

	public void add(TimedPlace place) {
		Require.that(place != null, "Argument must be a non-null place");
		Require.that(!isNameUsed(place.name()) || (place.isShared() && !places.contains(place)), "A place or transition with the specified name already exists in the petri net.");

		if(!place.isShared()) ((LocalTimedPlace)place).setModel(this);
		places.add(place);
		place.setCurrentMarking(currentMarking);
	}

	public void add(TimedTransition transition) {
		Require.that(transition != null, "Argument must be a non-null transition");
		Require.that(!isNameUsed(transition.name()) || transition.isShared(), "A place or transition with the specified name already exists in the petri net.");

		transition.setModel(this);
		transitions.add(transition);
	}

	public void add(TimedInputArc arc) {
		Require.that(arc != null, "Argument must be a non-null input arc.");
		Require.that(places.contains(arc.source()),	"The source place must be part of the petri net.");
		Require.that(transitions.contains(arc.destination()), "The destination transition must be part of the petri net");
		Require.that(!inputArcs.contains(arc), "The specified arc is already a part of the petri net.");
		
		arc.setModel(this);
		inputArcs.add(arc);
		arc.destination().addToPreset(arc);
	}

	public void add(TimedOutputArc arc) {
		Require.that(arc != null, "Argument must be a non-null output arc.");
		Require.that(places.contains(arc.destination()), "The destination place must be part of the petri net.");
		Require.that(transitions.contains(arc.source()), "The source transition must be part of the petri net");
		Require.that(!outputArcs.contains(arc),	"The specified arc is already a part of the petri net.");
	
		arc.setModel(this);
		outputArcs.add(arc);
		arc.source().addToPostset(arc);
	}

	public void add(TimedInhibitorArc arc) {
		Require.that(arc != null, "Argument must be a non-null output arc.");
		Require.that(places.contains(arc.source()),	"The source place must be part of the petri net.");
		Require.that(transitions.contains(arc.destination()), "The destination transition must be part of the petri net");
		Require.that(!inhibitorArcs.contains(arc), "The specified arc is already a part of the petri net.");
		Require.that(!hasArcFromPlaceToTransition(arc.source(), arc.destination()), "Cannot have two arcs between the same place and transition");

		arc.setModel(this);
		inhibitorArcs.add(arc);
		arc.destination().addInhibitorArc(arc);
	}

	public void add(TransportArc arc) {
		Require.that(arc != null, "Argument must be a non-null output arc.");
		Require.that(places.contains(arc.source()), "The source place must be part of the petri net.");
		Require.that(transitions.contains(arc.transition()), "The transition must be part of the petri net");
		Require.that(places.contains(arc.destination()), "The destination place must be part of the petri net.");
		Require.that(!inhibitorArcs.contains(arc), "The specified arc is already a part of the petri net.");
		Require.that(!hasArcFromPlaceToTransition(arc.source(), arc.transition()), "Cannot have two arcs between the same place and transition");
		Require.that(!hasArcFromTransitionToPlace(arc.transition(), arc.destination()),	"Cannot have two arcs between the same transition and place");

		arc.setModel(this);
		transportArcs.add(arc);
		arc.transition().addTransportArcGoingThrough(arc);
	}

	public void addToken(TimedToken token) {
		currentMarking.add(token);
	}

	public void removeToken(TimedToken token) {
		currentMarking.remove(token);
	}

	public void remove(TimedPlace place) {
		boolean removed = places.remove(place);
		if (removed && !place.isShared()){
			currentMarking.removePlaceFromMarking(place);
			((LocalTimedPlace)place).setModel(null);
		}
	}

	public void remove(TimedTransition transition) { // TODO: These methods must clean up arcs also
		boolean removed = transitions.remove(transition);
		if (removed)
			transition.setModel(null);
	}

	public void remove(TimedInputArc arc) {
		boolean removed = inputArcs.remove(arc);
		if (removed) {
			arc.setModel(null);
			arc.destination().removeFromPreset(arc);
		}
	}

	public void remove(TransportArc arc) {
		boolean removed = transportArcs.remove(arc);
		if (removed) {
			arc.setModel(null);
			arc.transition().removeTransportArcGoingThrough(arc);
		}
	}

	public void remove(TimedOutputArc arc) {
		boolean removed = outputArcs.remove(arc);
		if (removed) {
			arc.setModel(null);
			arc.source().removeFromPostset(arc);
		}
	}

	public void remove(TimedInhibitorArc arc) {
		boolean removed = inhibitorArcs.remove(arc);
		if (removed) {
			arc.setModel(null);
			arc.destination().removeInhibitorArc(arc);
		}
	}

	public boolean hasArcFromPlaceToTransition(TimedPlace source, TimedTransition destination) {
		for (TimedInputArc arc : inputArcs)
			if (arc.source().equals(source) && arc.destination().equals(destination))
				return true;
		for (TimedInhibitorArc arc : inhibitorArcs)
			if (arc.source().equals(source) && arc.destination().equals(destination))
				return true;
		for (TransportArc arc : transportArcs)
			if (arc.source().equals(source) && arc.transition().equals(destination))
				return true;

		return false;
	}

	public boolean hasArcFromTransitionToPlace(TimedTransition source, TimedPlace destination) {
		for (TimedOutputArc arc : outputArcs){
			if (arc.source().equals(source) && arc.destination().equals(destination))
				return true;
		}
		for (TransportArc arc : transportArcs){
			if (arc.transition().equals(source) && arc.destination().equals(destination))
				return true;
		}
		return false;
	}

	public boolean isNameUsed(String name) {
		if(parentNetwork != null && parentNetwork.isNameUsedForShared(name)) return true;

		for (TimedPlace place : places){
			if (place.name().equalsIgnoreCase(name)){
				return true;
			}
		}
		for (TimedTransition transition : transitions){
			if (transition.name().equalsIgnoreCase(name)){
				return true;
			}
		}
		return false;
	}

	public String name() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

	public void setName(String newName) {
		Require.that(newName != null && !newName.isEmpty(), "name cannot be null or empty");
		name = newName;
	}

	public TimedPlace getPlaceByName(String placeName) {
		for (TimedPlace p : places) {
			if (p.name().equals(placeName)) {
				return p;
			}
		}
		return null;
	}

	public TimedTransition getTransitionByName(String transitionName) {
		for (TimedTransition t : transitions) {
			if (t.name().equals(transitionName)) {
				return t;
			}
		}
		return null;
	}

	public void setMarking(TimedMarking marking) {
		Require.that(marking != null, "marking must not be null");
		currentMarking = marking;

		for (TimedPlace p : places) {
			p.setCurrentMarking(marking);
		}
	}

	public List<TimedPlace> places() {
		return places;
	}

	public List<TimedTransition> transitions() {
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

	public TimedArcPetriNet copy() {
		TimedArcPetriNet tapn = new TimedArcPetriNet(name);

		for(TimedPlace p : places) {
			TimedPlace copy = p.copy();
			tapn.add(copy);
			if(!p.isShared()){
				for(int i = 0; i < p.numberOfTokens(); i++) {
					tapn.addToken(new TimedToken(copy));
				}
			}
		}

		for(TimedTransition t : transitions){
			TimedTransition copy = t.copy();
			tapn.add(copy);
			if(t.isShared()){
				t.sharedTransition().makeShared(copy);
			}
		}

		for(TimedInputArc inputArc : inputArcs)
			tapn.add(inputArc.copy(tapn));

		for(TimedOutputArc outputArc : outputArcs)
			tapn.add(outputArc.copy(tapn));

		for(TransportArc transArc : transportArcs)
			tapn.add(transArc.copy(tapn));

		for(TimedInhibitorArc inhibArc : inhibitorArcs)
			tapn.add(inhibArc.copy(tapn));

		return tapn;
	}

	public TimedInputArc getInputArcFromPlaceToTransition(TimedPlace place, TimedTransition transition) {
		for(TimedInputArc inputArc : inputArcs) {
			if(inputArc.source().equals(place) && inputArc.destination().equals(transition))
				return inputArc;
		}
		return null;
	}

	public TimedOutputArc getOutputArcFromTransitionAndPlace(TimedTransition transition, TimedPlace place) {
		for(TimedOutputArc outputArc : outputArcs) {
			if(outputArc.source().equals(transition) && outputArc.destination().equals(place))
				return outputArc;
		}
		return null;
	}

	public TransportArc getTransportArcFromPlaceTransitionAndPlace(TimedPlace sourcePlace, TimedTransition transition, TimedPlace destinationPlace) {
		for(TransportArc transArc : transportArcs) {
			if(transArc.source().equals(sourcePlace) && transArc.transition().equals(transition) && transArc.destination().equals(destinationPlace))
				return transArc;
		}
		return null;
	}

	public TimedInhibitorArc getInhibitorArcFromPlaceAndTransition(TimedPlace place, TimedTransition transition) {
		for(TimedInhibitorArc inhibArc : inhibitorArcs) {
			if(inhibArc.source().equals(place) && inhibArc.destination().equals(transition))
				return inhibArc;
		}

		return null;
	}

	public boolean hasInhibitorArcs() {
		return inhibitorArcs.size() > 0;
	}
	
	public boolean isActive() {
		return isActive;
	}
	
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
	
	private void fillStatistics(Iterable<TimedArcPetriNet> nets, Object[][] array, int columnNumber){
		int numberOfPlaces = 0;
		int numberOfTransitions = 0;
		int numberOfInputArcs = 0;
		int numberOfOutputArcs = 0;
		int numberOfInhibitorArcs = 0;
		int numberOfTransportArcs = 0;
		int numberOfTotalNumberOfArcs = 0;
		int numberOfTokens = 0;
		int numberOfOrphans = 0;
		int numberOfComponents = 0;
		boolean networkUntimed = true; 
		int numberOfUntimedInputArcs = 0;
		int numberOfUntimedTransportArcs = 0;
		
		//For comparing to 
		TimeInterval infInterval = new TimeInterval(true, new IntBound(0), Bound.Infinity, false);
		TimeInterval infIntervalConst = new TimeInterval(true, new IntBound(0), Bound.Infinity, false);
		
		for(TimedArcPetriNet t : nets){
			numberOfPlaces += t.places().size();
			numberOfTransitions += t.transitions.size();
			numberOfInputArcs += t.inputArcs.size();
			numberOfOutputArcs += t.outputArcs.size();
			numberOfInhibitorArcs += t.inhibitorArcs.size();
			numberOfTransportArcs += t.transportArcs.size();
			numberOfTokens += t.getNumberOfTokensInNet();
			numberOfOrphans += t.getOrphanTransitions().size();
			numberOfComponents += 1;
			//Test if all inputarcs is untimed and get the number of untimed input arcs
			for(TimedInputArc in : t.inputArcs()){
				if(!((in.interval().lowerBound().value() == 0 && in.interval().IsLowerBoundNonStrict() && in.interval().upperBound().equals(Bound.Infinity)))){
					networkUntimed = false;
				} else {
					numberOfUntimedInputArcs++;
				}
			}
			//Test if all tansportarcs is untimed and get the number of untimed transport arcs
			for(TransportArc in : t.transportArcs()){
				if(!((in.interval().lowerBound().value() == 0 && in.interval().IsLowerBoundNonStrict() && in.interval().upperBound().equals(Bound.Infinity)))){
					networkUntimed = false;
				} else {
					numberOfUntimedTransportArcs++;
				}
			}
		}
		
		numberOfTotalNumberOfArcs = numberOfInputArcs + numberOfOutputArcs + numberOfInhibitorArcs + numberOfTransportArcs;
		
		int rowNumber = 0;
		array[rowNumber++][columnNumber] = numberOfPlaces;
		array[rowNumber++][columnNumber] = numberOfTransitions;
		array[rowNumber++][columnNumber] = numberOfInputArcs;
		array[rowNumber++][columnNumber] = numberOfOutputArcs;
		array[rowNumber++][columnNumber] = numberOfInhibitorArcs;
		array[rowNumber++][columnNumber] = numberOfTransportArcs;
		array[rowNumber++][columnNumber] = numberOfTotalNumberOfArcs;
		array[rowNumber++][columnNumber] = numberOfTokens;
		array[rowNumber++][columnNumber] = numberOfOrphans;
		array[rowNumber++][columnNumber] = numberOfComponents;
		array[rowNumber++][columnNumber] = networkUntimed ? "yes" : "no";
		array[rowNumber++][columnNumber] = numberOfUntimedInputArcs;
		array[rowNumber++][columnNumber] = numberOfUntimedTransportArcs;
	}
	
	public Object[][] getStatistics(){
		
		//Currently shown component
		Object[][] result = new Object[15][4];
		int rowNumber = 0;
		int columnNumber = 0;
		result[rowNumber++][columnNumber] = "Number of places: ";
		result[rowNumber++][columnNumber] = "Number of transitions: ";
		result[rowNumber++][columnNumber] = "Number of input arcs: ";
		result[rowNumber++][columnNumber] = "Number of output arcs: ";
		result[rowNumber++][columnNumber] = "Number of inhibitor arcs: ";
		result[rowNumber++][columnNumber] = "Number of pairs of transport arcs: ";
		result[rowNumber++][columnNumber] = "Total number of arcs: ";
		result[rowNumber++][columnNumber] = "Number of tokens: ";
		result[rowNumber++][columnNumber] = "Number of orphan transitions: ";
		result[rowNumber++][columnNumber] = "Number of components considered: ";
		result[rowNumber++][columnNumber] = "The network is untimed: ";
		result[rowNumber++][columnNumber] = "Number of untimed input arcs: ";
		result[rowNumber++][columnNumber] = "Number of untimed transport arcs";
		result[rowNumber++][columnNumber] = "Number of shared places: ";
		result[rowNumber++][columnNumber] = "Number of shared transitions: ";
		
		fillStatistics(Arrays.asList(new TimedArcPetriNet[] {this}), result, 1);
		fillStatistics(this.parentNetwork().activeTemplates(), result, 2);
		fillStatistics(this.parentNetwork().allTemplates(), result, 3);
		
		//Add the number of shared places and transitions
		result[13][3] = this.parentNetwork().numberOfSharedPlaces();
		result[14][3] = this.parentNetwork().numberOfSharedTransitions();
		
		return result;
	}
	
	public List<TimedTransition> getOrphanTransitions(){
		List<TimedTransition> orphans = new ArrayList<TimedTransition>();
		
		for(TimedTransition transition:transitions){
			if(transition.isOrphan()){
				orphans.add(transition);
			}
		}
		
		return orphans;
	}
	
	public int getNumberOfTokensInNet(){
		int result = 0;
		for(TimedPlace place : places){
			result += place.numberOfTokens();
		}
		
		return result;
	}
}
