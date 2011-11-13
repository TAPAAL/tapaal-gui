package dk.aau.cs.model.tapn;

import java.awt.Dimension;
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
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

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
		
		//Currently shown component
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
		JLabel textNets = new JLabel("Number of components considered: ");
		JLabel numberOfNets = new JLabel(Integer.toString(1));
		
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
		
		//Add Labels
		int currentColumn = 0;
		gbc = new GridBagConstraints();
		gbc.gridx = currentColumn;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, bottomMargin, rightMargin);
		result.add(textPlaces, gbc);
		
		/*
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = GridBagConstraints.RELATIVE;
		//gbc.weightx =12;
		gbc.gridwidth = 4;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
		separator.setPreferredSize(new Dimension(1,5));
		result.add(separator, gbc);
		*/
		
		gbc = new GridBagConstraints();
		gbc.gridx = currentColumn;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, bottomMargin, rightMargin);
		result.add(textTransitions, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = currentColumn;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, bottomMargin, rightMargin);
		result.add(textInputArcs, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = currentColumn;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, bottomMargin, rightMargin);
		result.add(textOutputArcs, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = currentColumn;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, bottomMargin, rightMargin);
		result.add(textInhibitorArcs, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = currentColumn;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, bottomMargin, rightMargin);
		result.add(textTransportArcs, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = currentColumn;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, bottomMargin, rightMargin);
		result.add(textTotalNumberOfArcs, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = currentColumn;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, bottomMargin, rightMargin);
		result.add(textTokens, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = currentColumn;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, bottomMargin, rightMargin);
		result.add(textOrphans, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = currentColumn;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, bottomMargin, rightMargin);
		result.add(textNets, gbc);
		
		//For the currently viewed net
		currentColumn = 1;
		gbc.gridx = currentColumn;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, bottomMargin, rightMargin);
		result.add(new JLabel("In shown component"), gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = currentColumn;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 0, bottomMargin, rightMargin);
		result.add(numberOfPlaces, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = currentColumn;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 0, bottomMargin, rightMargin);
		result.add(numberOfTransitions, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = currentColumn;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 0, bottomMargin, rightMargin);
		result.add(numberOfInputArcs, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = currentColumn;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 0, bottomMargin, rightMargin);
		result.add(numberOfOutputArcs, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = currentColumn;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 0, bottomMargin, rightMargin);
		result.add(numberOfInhibitorArcs, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = currentColumn;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 0, bottomMargin, rightMargin);
		result.add(numberOfTransportArcs, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = currentColumn;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 0, bottomMargin, rightMargin);
		result.add(numberOfTotalNumberOfArcs, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = currentColumn;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 0, bottomMargin, rightMargin);
		result.add(numberOfTokens, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = currentColumn;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 0, bottomMargin, rightMargin);
		result.add(numberOfOrphans, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = currentColumn;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 0, bottomMargin, rightMargin);
		result.add(numberOfNets, gbc);
		
		//For all components
		int numberOfComponents = 0;
		int numberOfPlacesNet = 0;
		int numberOfTransitionsNet = 0;
		int numberOfInputArcsNet = 0;
		int numberOfOutputArcsNet = 0;
		int numberOfInhibitorArcsNet = 0;
		int numberOfTransportArcsNet = 0;
		int numberOfTokensNet = 0;
		int numberOfOrphansNet = 0;
		int numberOfTotalNumberOfArcsNet = 0;
		
		
		for(TimedArcPetriNet t : this.parentNetwork().allTemplates()){
			numberOfComponents += 1;
			numberOfPlacesNet += t.places().size();
			numberOfTransitionsNet += t.transitions.size();
			numberOfInputArcsNet += t.inputArcs.size();
			numberOfOutputArcsNet += t.outputArcs.size();
			numberOfInhibitorArcsNet += t.inhibitorArcs.size();
			numberOfTransportArcsNet += t.transportArcs.size();
			numberOfTokensNet += t.getNumberOfTokensInNet();
			numberOfOrphansNet += t.getOrphanTransitions().size();
		}
		
		numberOfTotalNumberOfArcsNet = numberOfInputArcsNet + numberOfOutputArcsNet + numberOfInhibitorArcsNet + numberOfTransportArcsNet;
		
		currentColumn = 2;
		
		gbc = new GridBagConstraints();
		gbc.gridx = currentColumn;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 0, bottomMargin, rightMargin);
		result.add(new JLabel("In all components"), gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = currentColumn;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 0, bottomMargin, rightMargin);
		result.add(new JLabel(Integer.toString(numberOfPlacesNet)), gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = currentColumn;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 0, bottomMargin, rightMargin);
		result.add(new JLabel(Integer.toString(numberOfTransitionsNet)), gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = currentColumn;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 0, bottomMargin, rightMargin);
		result.add(new JLabel(Integer.toString(numberOfInputArcsNet)), gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = currentColumn;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 0, bottomMargin, rightMargin);
		result.add(new JLabel(Integer.toString(numberOfOutputArcsNet)), gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = currentColumn;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 0, bottomMargin, rightMargin);
		result.add(new JLabel(Integer.toString(numberOfInhibitorArcsNet)), gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = currentColumn;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 0, bottomMargin, rightMargin);
		result.add(new JLabel(Integer.toString(numberOfTransportArcsNet)), gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = currentColumn;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 0, bottomMargin, rightMargin);
		result.add(new JLabel(Integer.toString(numberOfTotalNumberOfArcsNet)), gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = currentColumn;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 0, bottomMargin, rightMargin);
		result.add(new JLabel(Integer.toString(numberOfTokensNet)), gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = currentColumn;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 0, bottomMargin, rightMargin);
		result.add(new JLabel(Integer.toString(numberOfOrphansNet)), gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = currentColumn;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 0, bottomMargin, rightMargin);
		result.add(new JLabel(Integer.toString(numberOfComponents)), gbc);
		
		//For active components
		int numberOfActiveComponents = 0;
		int numberOfPlacesActiveComponents = 0;
		int numberOfTransitionsActiveComponents = 0;
		int numberOfInputArcsActiveComponents = 0;
		int numberOfOutputArcsActiveComponents = 0;
		int numberOfInhibitorArcsActiveComponents = 0;
		int numberOfTransportArcsActiveComponents = 0;
		int numberOfTokensActiveComponents = 0;
		int numberOfOrphansActiveComponents = 0;
		int numberOfTotalNumberOfArcsActiveComponents = 0;
		
		
		for(TimedArcPetriNet t : this.parentNetwork().activeTemplates()){
			numberOfActiveComponents += 1;
			numberOfPlacesActiveComponents += t.places().size();
			numberOfTransitionsActiveComponents += t.transitions.size();
			numberOfInputArcsActiveComponents += t.inputArcs.size();
			numberOfOutputArcsActiveComponents += t.outputArcs.size();
			numberOfInhibitorArcsActiveComponents += t.inhibitorArcs.size();
			numberOfTransportArcsActiveComponents += t.transportArcs.size();
			numberOfTokensActiveComponents += t.getNumberOfTokensInNet();
			numberOfOrphansActiveComponents += t.getOrphanTransitions().size();
		}
		
		numberOfTotalNumberOfArcsActiveComponents = numberOfInputArcsActiveComponents + numberOfOutputArcsActiveComponents
				+ numberOfInhibitorArcsActiveComponents + numberOfTransportArcsActiveComponents;
		
		currentColumn = 3;
		
		gbc = new GridBagConstraints();
		gbc.gridx = currentColumn;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 0, bottomMargin, rightMargin);
		result.add(new JLabel("In all active components"), gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = currentColumn;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 0, bottomMargin, rightMargin);
		result.add(new JLabel(Integer.toString(numberOfPlacesActiveComponents)), gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = currentColumn;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 0, bottomMargin, rightMargin);
		result.add(new JLabel(Integer.toString(numberOfTransitionsActiveComponents)), gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = currentColumn;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 0, bottomMargin, rightMargin);
		result.add(new JLabel(Integer.toString(numberOfInputArcsActiveComponents)), gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = currentColumn;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 0, bottomMargin, rightMargin);
		result.add(new JLabel(Integer.toString(numberOfOutputArcsActiveComponents)), gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = currentColumn;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 0, bottomMargin, rightMargin);
		result.add(new JLabel(Integer.toString(numberOfInhibitorArcsActiveComponents)), gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = currentColumn;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 0, bottomMargin, rightMargin);
		result.add(new JLabel(Integer.toString(numberOfTransportArcsActiveComponents)), gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = currentColumn;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 0, bottomMargin, rightMargin);
		result.add(new JLabel(Integer.toString(numberOfTotalNumberOfArcsActiveComponents)), gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = currentColumn;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 0, bottomMargin, rightMargin);
		result.add(new JLabel(Integer.toString(numberOfTokensActiveComponents)), gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = currentColumn;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 0, bottomMargin, rightMargin);
		result.add(new JLabel(Integer.toString(numberOfOrphansActiveComponents)), gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = currentColumn;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 0, bottomMargin, rightMargin);
		result.add(new JLabel(Integer.toString(numberOfActiveComponents)), gbc);
		
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
