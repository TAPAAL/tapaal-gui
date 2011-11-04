package dk.aau.cs.model.tapn;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

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
	
	private List<TimedTransition> orphans;
	
	public Object getStatistics(){
		
		JPanel result = new JPanel(new GridBagLayout());
		 
		
		JLabel textPlaces = new JLabel("Number of places: ");
		JLabel numberOfPlaces = new JLabel(Integer.toString(places().size()));
		JLabel textTransitions = new JLabel("Number of transitions: ");
		JLabel numberOfTransitions = new JLabel(Integer.toString(transitions().size()));
		JLabel textInputArcs = new JLabel("Number of input arcs: ");
		JLabel numberOfInputArcs = new JLabel(Integer.toString(inputArcs.size()));
		JLabel textOutputArcs = new JLabel("Number of output arcs: ");
		JLabel numberOfOutputArcs = new JLabel(Integer.toString(outputArcs.size()));
		JLabel textInhibitorArcs = new JLabel("Number of inhibitor arcs: ");
		JLabel numberOfInhibitorArcs = new JLabel(Integer.toString(inhibitorArcs.size()));
		JLabel textTransportArcs = new JLabel("Number of transport arcs: ");
		JLabel numberOfTransportArcs = new JLabel(Integer.toString(transportArcs.size()));
		JLabel textTotalNumberOfArcs = new JLabel("Total number of arcs: ");
		JLabel numberOfTotalNumberOfArcs = new JLabel((inputArcs.size()+outputArcs.size()+inhibitorArcs.size()+transportArcs.size()) + "\n");
		
		//Tokens in net
		JLabel textTokens = new JLabel("Number of tokens: ");
		JLabel numberOfTokens = new JLabel(Integer.toString(getNumberOfTokensInNet()));
		
		//Orphan transitions
		JLabel textOrphans = new JLabel("Number of orphan transitions: ");
		orphans = getOrphanTransitions();
		JLabel numberOfOrphans = new JLabel(Integer.toString(orphans.size()));
		
		int rightMargin = 10;
		int bottomMargin = 3;
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, bottomMargin, rightMargin);
		result.add(textPlaces, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 0, bottomMargin, rightMargin);
		result.add(numberOfPlaces, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, bottomMargin, rightMargin);
		result.add(textTransitions, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 0, bottomMargin, rightMargin);
		result.add(numberOfTransitions, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, bottomMargin, rightMargin);
		result.add(textInputArcs, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 0, bottomMargin, rightMargin);
		result.add(numberOfInputArcs, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, bottomMargin, rightMargin);
		result.add(textOutputArcs, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 3;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 0, bottomMargin, rightMargin);
		result.add(numberOfOutputArcs, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, bottomMargin, rightMargin);
		result.add(textInhibitorArcs, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 4;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 0, bottomMargin, rightMargin);
		result.add(numberOfInhibitorArcs, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 5;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, bottomMargin, rightMargin);
		result.add(textTransportArcs, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 5;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 0, bottomMargin, rightMargin);
		result.add(numberOfTransportArcs, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 6;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, bottomMargin, rightMargin);
		result.add(textTotalNumberOfArcs, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 6;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 0, bottomMargin, rightMargin);
		result.add(numberOfTotalNumberOfArcs, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 7;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, bottomMargin, rightMargin);
		result.add(textTokens, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 7;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 0, bottomMargin, rightMargin);
		result.add(numberOfTokens, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 8;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, bottomMargin, rightMargin);
		result.add(textOrphans, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 8;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 0, bottomMargin, rightMargin);
		result.add(numberOfOrphans, gbc);

		return result;
		/*
		StringBuilder sb = new StringBuilder();
		
		sb.append("Number of places: ");
		sb.append(places().size() + "\n");
		sb.append("Number of transitions: ");
		sb.append(transitions().size() + "\n");
		sb.append("Number of input arcs: ");
		sb.append(inputArcs.size() + "\n");
		sb.append("Number of output arcs: ");
		sb.append(outputArcs.size() + "\n");
		sb.append("Number of inhibitor arcs: ");
		sb.append(inhibitorArcs.size() + "\n");
		sb.append("Number of transport arcs: ");
		sb.append(transportArcs.size() + "\n");
		sb.append("Total number of arcs: ");
		sb.append((inputArcs.size()+outputArcs.size()+inhibitorArcs.size()+transportArcs.size()) + "\n");
		
		return sb.toString();
		*/
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
