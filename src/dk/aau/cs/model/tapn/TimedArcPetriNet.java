package dk.aau.cs.model.tapn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pipe.gui.undo.AddArcPathPointEdit;
import dk.aau.cs.model.tapn.Bound.InfBound;
import dk.aau.cs.util.IntervalOperations;
import dk.aau.cs.util.Require;

public class TimedArcPetriNet {
	private String name;
	private TimedArcPetriNetNetwork parentNetwork;
	private boolean isActive;
	private Set<String> usedNames = new HashSet<String>();

	private Map<String, TimedPlace> places = new HashMap<String, TimedPlace>();
	private Map<String, TimedTransition> transitions = new HashMap<String, TimedTransition>();
	private Map<String, TimedInputArc> inputArcs = new HashMap<String, TimedInputArc>();
	private Map<String, TimedOutputArc> outputArcs = new HashMap<String, TimedOutputArc>();
	private Map<String, TimedInhibitorArc> inhibitorArcs = new HashMap<String, TimedInhibitorArc>();
	private Map<String, TransportArc> transportArcs = new HashMap<String, TransportArc>();

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
		Require.that(!isNameUsed(place.name()) || (place.isShared() && !places.containsKey(place.name())), "A place or transition with the specified name \"" + place + "\" already exists in the petri net.");

		if(!place.isShared()) ((LocalTimedPlace)place).setModel(this);
		places.put(place.name(), place);
		place.setCurrentMarking(currentMarking);
	}

	public void add(TimedTransition transition) {
		Require.that(transition != null, "Argument must be a non-null transition");
		Require.that(!isNameUsed(transition.name()) || transition.isShared(), "A place or transition with the specified name \"" + transition + "\" already exists in the petri net.");

		transition.setModel(this);
		transitions.put(transition.name(), transition);
	}

	public void add(TimedInputArc arc) {
		Require.that(arc != null, "Argument must be a non-null input arc.");
		Require.that(places.containsKey(arc.source().name()),	"The source place \"" + arc.source() + "\" must be part of the petri net.");
		Require.that(transitions.containsKey(arc.destination().name()), "The destination transition \"" + arc.destination() + "\" must be part of the petri net");
		Require.that(!inputArcs.containsKey(arc.source().name() + arc.destination().name()), "The specified arc is already a part of the petri net.");
		
		arc.setModel(this);
		inputArcs.put(arc.source().name() + arc.destination().name(), arc);
		arc.destination().addToPreset(arc);
	}

	public void add(TimedOutputArc arc) {
		Require.that(arc != null, "Argument must be a non-null output arc.");
		Require.that(places.containsKey(arc.destination().name()), "The destination place \"" + arc.destination() + "\" must be part of the petri net.");
		Require.that(transitions.containsKey(arc.source().name()), "The source transition \"" + arc.source() + "\" must be part of the petri net");
		Require.that(!outputArcs.containsKey(arc.source().name() + arc.destination().name()),	"The specified arc is already a part of the petri net.");
	
		arc.setModel(this);
		outputArcs.put(arc.source().name() + arc.destination().name(), arc);
		arc.source().addToPostset(arc);
	}

	public void add(TimedInhibitorArc arc) {
		Require.that(arc != null, "Argument must be a non-null output arc.");
		Require.that(places.containsKey(arc.source().name()),	"The source place \"" + arc.source() + "\" must be part of the petri net.");
		Require.that(transitions.containsKey(arc.destination().name()), "The destination transition " + arc.destination() + " must be part of the petri net");
		Require.that(!inhibitorArcs.containsKey(arc.source().name() + arc.destination().name()), "The specified arc is already a part of the petri net.");
		Require.that(!hasArcFromPlaceToTransition(arc.source(), arc.destination()), "Cannot have two arcs between the same place and transition");

		arc.setModel(this);
		inhibitorArcs.put(arc.source().name() + arc.destination().name(), arc);
		arc.destination().addInhibitorArc(arc);
	}

	public void add(TransportArc arc) {
		Require.that(arc != null, "Argument must be a non-null output arc.");
		Require.that(places.containsKey(arc.source().name()), "The source place \"" + arc.source() + "\" must be part of the petri net.");
		Require.that(transitions.containsKey(arc.transition().name()), "The transition \"" + arc.transition() + "\" must be part of the petri net");
		Require.that(places.containsKey(arc.destination()), "The destination place \"" + arc.destination() + "\" must be part of the petri net.");
		Require.that(!transportArcs.containsKey(arc.source().name() + arc.transition().name() + arc.destination().name()), "The specified arc is already a part of the petri net.");
		Require.that(!hasArcFromPlaceToTransition(arc.source(), arc.transition()), "Cannot have two arcs between the same place and transition");
		Require.that(!hasArcFromTransitionToPlace(arc.transition(), arc.destination()),	"Cannot have two arcs between the same transition and place");

		arc.setModel(this);
		transportArcs.put(arc.source().name() + arc.transition().name() + arc.destination().name(), arc);
		arc.transition().addTransportArcGoingThrough(arc);
	}

	public void addToken(TimedToken token) {
		currentMarking.add(token);
	}

	public void removeToken(TimedToken token) {
		currentMarking.remove(token);
	}

	public void remove(TimedPlace place) {
		TimedPlace removed = places.remove(place.name());
		usedNames.remove(place.name());
		if (removed != null && !place.isShared()){
			currentMarking.removePlaceFromMarking(place);
			((LocalTimedPlace)place).setModel(null);
		}
	}

	public void remove(TimedTransition transition) { // TODO: These methods must clean up arcs also
		transitions.remove(transition.name());
		usedNames.remove(transition.name());
		// TODO: Removed to fix bug #891944 
		//if (removed)
		//	transition.setModel(null);
	}

	public void remove(TimedInputArc arc) {
		TimedInputArc removed = inputArcs.remove(arc.source().name() + arc.destination().name());
		if (removed != null) {
			arc.setModel(null);
			arc.destination().removeFromPreset(arc);
		}
	}

	public void remove(TransportArc arc) {
		TransportArc removed = transportArcs.remove(arc.source().name() + arc.transition().name() + arc.destination().name());
		if (removed != null) {
			arc.setModel(null);
			arc.transition().removeTransportArcGoingThrough(arc);
		}
	}

	public void remove(TimedOutputArc arc) {
		TimedOutputArc removed = outputArcs.remove(arc.source().name() + arc.destination().name());
		if (removed != null) {
			arc.setModel(null);
			arc.source().removeFromPostset(arc);
		}
	}

	public void remove(TimedInhibitorArc arc) {
		TimedInhibitorArc removed = inhibitorArcs.remove(arc.source().name() + arc.destination().name());
		if (removed != null) {
			arc.setModel(null);
			arc.destination().removeInhibitorArc(arc);
		}
	}

	public boolean hasArcFromPlaceToTransition(TimedPlace source, TimedTransition destination) {
		if(inputArcs.containsKey(source.name() + destination.name())
				|| inhibitorArcs.containsKey(source.name() + destination.name())){
			return true;
		}
		
		//Hard to hash on
		for (TransportArc arc : transportArcs.values())
			if (arc.source().equals(source) && arc.transition().equals(destination))
				return true;

		return false;
	}

	public boolean hasArcFromTransitionToPlace(TimedTransition source, TimedPlace destination) {
		if(outputArcs.containsKey(inputArcs.containsKey(source.name() + destination.name()))){
			return true;
		}
		
		//Hard to hash on
		for (TransportArc arc : transportArcs.values()){
			if (arc.transition().equals(source) && arc.destination().equals(destination))
				return true;
		}
		return false;
	}

	public boolean isNameUsed(String name) {
		if(parentNetwork != null && parentNetwork.isNameUsedForShared(name)) return true;
		return places.containsKey(name) || transitions.containsKey(name);
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
		return places.get(placeName);
	}

	public TimedTransition getTransitionByName(String transitionName) {
		return transitions.get(transitionName);
	}

	public void setMarking(TimedMarking marking) {
		Require.that(marking != null, "marking must not be null");
		currentMarking = marking;

		for (TimedPlace p : places.values()) {
			p.setCurrentMarking(marking);
		}
	}

	public Collection<TimedPlace> places() {
		return places.values();
	}

	public Collection<TimedTransition> transitions() {
		return transitions.values();
	}

	public Collection<TimedInputArc> inputArcs() {
		return inputArcs.values();
	}

	public Collection<TimedOutputArc> outputArcs() {
		return outputArcs.values();
	}

	public Collection<TransportArc> transportArcs() {
		return transportArcs.values();
	}

	public Collection<TimedInhibitorArc> inhibitorArcs() {
		return inhibitorArcs.values();
	}

	public TimedArcPetriNet copy() {
		TimedArcPetriNet tapn = new TimedArcPetriNet(name);

		for(TimedPlace p : places.values()) {
			TimedPlace copy = p.copy();
			tapn.add(copy);
			if(!p.isShared()){
				for(int i = 0; i < p.numberOfTokens(); i++) {
					tapn.addToken(new TimedToken(copy));
				}
			}
		}

		for(TimedTransition t : transitions.values()){
			TimedTransition copy = t.copy();
			tapn.add(copy);
			if(t.isShared()){
				t.sharedTransition().makeShared(copy);
			}
		}

		for(TimedInputArc inputArc : inputArcs.values())
			tapn.add(inputArc.copy(tapn));

		for(TimedOutputArc outputArc : outputArcs.values())
			tapn.add(outputArc.copy(tapn));

		for(TransportArc transArc : transportArcs.values())
			tapn.add(transArc.copy(tapn));

		for(TimedInhibitorArc inhibArc : inhibitorArcs.values())
			tapn.add(inhibArc.copy(tapn));

		tapn.setActive(isActive());
		
		return tapn;
	}

	public TimedInputArc getInputArcFromPlaceToTransition(TimedPlace place, TimedTransition transition) {
		return inputArcs.get(place.name() + transition.name());
	}

	public TimedOutputArc getOutputArcFromTransitionAndPlace(TimedTransition transition, TimedPlace place) {
		return outputArcs.get(transition.name() + place.name());
	}

	public TransportArc getTransportArcFromPlaceTransitionAndPlace(TimedPlace sourcePlace, TimedTransition transition, TimedPlace destinationPlace) {
		return transportArcs.get(sourcePlace.name() + transition.name() + destinationPlace.name());
	}

	public TimedInhibitorArc getInhibitorArcFromPlaceAndTransition(TimedPlace place, TimedTransition transition) {
		return inhibitorArcs.get(place.name() + transition.name());
	}

	public boolean hasInhibitorArcs() {
		return inhibitorArcs.size() > 0;
	}
	
	public boolean isDegree2(){
		for(TimedTransition t : this.transitions()) {
			if(t.presetSize() > 2 || t.postsetSize() > 2)
				return false;
		}
		return true;
	}
	
	public boolean isActive() {
		return isActive;
	}
	
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
	
	private void fillStatistics(Iterable<TimedArcPetriNet> nets, Object[][] array, int columnNumber){
		int numberOfComponents = 0;
		int numberOfPlaces = 0;
		int numberOfTransitions = 0;
		int numberOfInputArcs = 0;
		int numberOfOutputArcs = 0;
		int numberOfInhibitorArcs = 0;
		int numberOfTransportArcs = 0;
		int numberOfTotalNumberOfArcs = 0;
		int numberOfTokens = 0;
		int numberOfOrphans = 0;
		boolean networkUntimed = true; 
		boolean networkWeighted = false; 
		int numberOfUntimedInputArcs = 0;
		int numberOfUntimedTransportArcs = 0;
		
		//For comparing to 
		TimeInterval infInterval = new TimeInterval(true, new IntBound(0), Bound.Infinity, false);
		TimeInterval infIntervalConst = new TimeInterval(true, new IntBound(0), Bound.Infinity, false);
		
		for(TimedArcPetriNet t : nets){
			numberOfComponents += 1;
			numberOfPlaces += t.places.size();
			numberOfTransitions += t.transitions.size();
			numberOfInputArcs += t.inputArcs.size();
			numberOfOutputArcs += t.outputArcs.size();
			numberOfInhibitorArcs += t.inhibitorArcs.size();
			numberOfTransportArcs += t.transportArcs.size();
			numberOfTokens += t.getNumberOfTokensInNet();
			numberOfOrphans += t.getOrphanTransitions().size();
			//Test if all inputarcs is untimed and get the number of untimed input arcs
			for(TimedInputArc in : t.inputArcs()){
				if(!((in.interval().lowerBound().value() == 0 && in.interval().IsLowerBoundNonStrict() && in.interval().upperBound().equals(Bound.Infinity)))){
					networkUntimed = false;
				} else {
					numberOfUntimedInputArcs++;
				}
				if(!networkWeighted && in.getWeight().value() > 1){
					networkWeighted = true;
				}
			}
			//Test if all tansportarcs is untimed and get the number of untimed transport arcs
			for(TransportArc in : t.transportArcs()){
				if(!((in.interval().lowerBound().value() == 0 && in.interval().IsLowerBoundNonStrict() && in.interval().upperBound().equals(Bound.Infinity)))){
					networkUntimed = false;
				} else {
					numberOfUntimedTransportArcs++;
				}
				if(!networkWeighted && in.getWeight().value() > 1){
					networkWeighted = true;
				}
			}
			
			// Test all output arcs for weights
			if(!networkWeighted){
				for(TimedOutputArc in : t.outputArcs()){
					if(in.getWeight().value() > 1){
						networkWeighted = true;
						break;
					}
				}
			}
			
			// Test all inhibitor arcs for weights
			if(!networkWeighted){
				for(TimedInhibitorArc in : t.inhibitorArcs()){
					if(in.getWeight().value() > 1){
						networkWeighted = true;
						break;
					}
				}
			}
			
		}
		
		numberOfTotalNumberOfArcs = numberOfInputArcs + numberOfOutputArcs + numberOfInhibitorArcs + numberOfTransportArcs;
		
		int rowNumber = 0;
		array[rowNumber++][columnNumber] = numberOfComponents;
		array[rowNumber++][columnNumber] = numberOfPlaces;
		array[rowNumber++][columnNumber] = numberOfTransitions;
		array[rowNumber++][columnNumber] = numberOfInputArcs;
		array[rowNumber++][columnNumber] = numberOfOutputArcs;
		array[rowNumber++][columnNumber] = numberOfInhibitorArcs;
		array[rowNumber++][columnNumber] = numberOfTransportArcs;
		array[rowNumber++][columnNumber] = numberOfTotalNumberOfArcs;
		array[rowNumber++][columnNumber] = numberOfTokens;
		array[rowNumber++][columnNumber] = numberOfUntimedInputArcs;
		array[rowNumber++][columnNumber] = numberOfUntimedTransportArcs;
		//Make space for number of shared transitions and places
		rowNumber += 2;
		array[rowNumber++][columnNumber] = networkUntimed ? "yes" : "no";
		array[rowNumber++][columnNumber] = networkWeighted ? "yes" : "no";
		array[rowNumber++][columnNumber] = numberOfOrphans;
	}
	
	public Object[][] getStatistics(){
		
		Object[][] result = new Object[16][4];
		int rowNumber = 0;
		int columnNumber = 0;
		result[rowNumber++][columnNumber] = "Number of components considered: ";
		result[rowNumber++][columnNumber] = "Number of places: ";
		result[rowNumber++][columnNumber] = "Number of transitions: ";
		result[rowNumber++][columnNumber] = "Number of input arcs: ";
		result[rowNumber++][columnNumber] = "Number of output arcs: ";
		result[rowNumber++][columnNumber] = "Number of inhibitor arcs: ";
		result[rowNumber++][columnNumber] = "Number of pairs of transport arcs: ";
		result[rowNumber++][columnNumber] = "Total number of arcs: ";
		result[rowNumber++][columnNumber] = "Number of tokens: ";
		result[rowNumber++][columnNumber] = "Number of untimed input arcs: ";
		result[rowNumber++][columnNumber] = "Number of untimed transport arcs";
		result[rowNumber++][columnNumber] = "Number of shared places: ";
		result[rowNumber++][columnNumber] = "Number of shared transitions: ";
		result[rowNumber++][columnNumber] = "The network is untimed: ";
		result[rowNumber++][columnNumber] = "The network is weighted: ";
		result[rowNumber++][columnNumber] = "Number of orphan transitions: ";
		
		fillStatistics(Arrays.asList(new TimedArcPetriNet[] {this}), result, 1);
		fillStatistics(this.parentNetwork().activeTemplates(), result, 2);
		fillStatistics(this.parentNetwork().allTemplates(), result, 3);
		
		//Add the number of shared places and transitions
		result[11][3] = this.parentNetwork().numberOfSharedPlaces();
		result[12][3] = this.parentNetwork().numberOfSharedTransitions();
		
		return result;
	}
	
	public List<TimedTransition> getOrphanTransitions(){
		List<TimedTransition> orphans = new ArrayList<TimedTransition>();
		
		for(TimedTransition transition:transitions.values()){
			if(transition.isOrphan()){
				orphans.add(transition);
			}
		}
		
		return orphans;
	}
	
	public int getNumberOfTokensInNet(){
		int result = 0;
		for(TimedPlace place : places.values()){
			result += place.numberOfTokens();
		}
		
		return result;
	}

	public boolean hasWeights() {
		for(TimedInputArc t : inputArcs.values()){
			if(t.getWeight().value() != 1){
				return true;
			}
		}
		
		for(TimedOutputArc t : outputArcs.values()){
			if(t.getWeight().value() != 1){
				return true;
			}
		}
		
		for(TimedInhibitorArc t : inhibitorArcs.values()){
			if(t.getWeight().value() != 1){
				return true;
			}
		}
		
		for(TransportArc t : transportArcs.values()){
			if(t.getWeight().value() != 1){
				return true;
			}
		}
		
		
		return false;
	}
	
	public boolean isUntimed() {
		for(TimedInputArc t : inputArcs.values()){
			if(!t.interval().equals(t.interval().ZERO_INF)){
				return false;
			}
		}
		
		for(TransportArc t : transportArcs.values()){
			if(!t.interval().equals(t.interval().ZERO_INF)){
				return false;
			}
		}
		
		return true;
	}
	
	public boolean hasUrgentTransitions() {
		for(TimedTransition t : transitions.values()){
			if(t.isUrgent()){
				return true;
			}
		}
		
		return false;
	}
	
	public boolean isNonStrict(){
		for(TimedInputArc t : inputArcs.values()){
			if(!t.interval().IsLowerBoundNonStrict() || (!t.interval().IsUpperBoundNonStrict() && !(t.interval().upperBound() instanceof InfBound))){
				return false;
			}
		}
		
		for(TransportArc t : transportArcs.values()){
			if(!t.interval().IsLowerBoundNonStrict() || (!t.interval().IsUpperBoundNonStrict() && !(t.interval().upperBound() instanceof InfBound))){
				return false;
			}
		}
		
		for(TimedPlace p : places.values()){
			if(!p.invariant().isUpperNonstrict() && !(p.invariant().upperBound() instanceof InfBound)){
				return false;
			}
		}
		
		return true;
	}

	
	/**
	 * Finds the biggest constant in the net
	 * @return the biggest constant in the net or -1 if there are no constants in the net
	 */
	public int getBiggestConstant(){
		int biggestConstant = -1;
		for(TimedInputArc t : inputArcs.values()){
			Bound max = IntervalOperations.getMaxNoInfBound(t.interval());
			if(max.value() > biggestConstant){
				biggestConstant = max.value();
			}
		}
		
		for(TransportArc t : transportArcs.values()){
			Bound max = IntervalOperations.getMaxNoInfBound(t.interval());
			if(max.value() > biggestConstant){
				biggestConstant = max.value();
			}
		}
		
		for(TimedPlace t : places.values()){
			if(!(t.invariant().upperBound() instanceof InfBound) && t.invariant().upperBound().value() > biggestConstant){
				biggestConstant = t.invariant().upperBound().value();
			}
		}
		
		return biggestConstant;
	}
	
	/**
	 * Finds the biggest constant which is associated with an enabled transition
	 * @return the biggest constant which is associated with an enabled transition or -1 if there are no such constants 
	 */
	public int getBiggestConstantEnabledTransitions(){
		int biggestConstant = -1;
		
		for(TimedTransition t : transitions.values()){
			if(t.isDEnabled()){
				int tmp = t.getLagestAssociatedConstant(); 
				if(tmp > biggestConstant){
					biggestConstant = tmp;
				}
			}
		}
		return biggestConstant;
	}
}
