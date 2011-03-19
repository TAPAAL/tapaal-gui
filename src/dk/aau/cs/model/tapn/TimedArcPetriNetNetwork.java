package dk.aau.cs.model.tapn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.util.Require;

public class TimedArcPetriNetNetwork {
	private List<TimedArcPetriNet> tapns = new ArrayList<TimedArcPetriNet>();
	private List<SharedPlace> sharedPlaces = new ArrayList<SharedPlace>();
	private List<SharedTransition> sharedTransitions = new ArrayList<SharedTransition>();
	
	private NetworkMarking currentMarking;
	private ConstantStore constants;

	public TimedArcPetriNetNetwork() {
		this(new ConstantStore());
	}
	
	public TimedArcPetriNetNetwork(ConstantStore constants){
		this.constants = constants;
		this.currentMarking = new NetworkMarking();
	}

	public void add(TimedArcPetriNet tapn) {
		Require.that(tapn != null, "tapn must be non-null");

		tapn.setParentNetwork(this);
		tapns.add(tapn);
		currentMarking.addMarking(tapn, new LocalTimedMarking());
		tapn.setMarking(currentMarking);
	}
	
	public void add(SharedTransition sharedTransition){
		Require.that(sharedTransition != null, "sharedTransition must not be null");
		Require.that(!isNameUsed(sharedTransition.name()), "There is already a transition or place with that name");
		
		sharedTransition.setNetwork(this);
		sharedTransitions.add(sharedTransition);
	}
	
	public void add(SharedPlace sharedPlace) {
		Require.that(sharedPlace != null, "sharedPlace must not be null");
		Require.that(!isNameUsed(sharedPlace.name()), "There is already a transition or place with that name");
		
		sharedPlace.setNetwork(this);
		sharedPlace.setCurrentMarking(currentMarking);
		sharedPlaces.add(sharedPlace);		
	}

	public boolean isNameUsedForShared(String name){
		for(SharedTransition transition : sharedTransitions){
			if(transition.name().equalsIgnoreCase(name)) return true;
		}
		
		for(SharedPlace place : sharedPlaces){
			if(place.name().equalsIgnoreCase(name)) return true;
		}
		
		return false;
	}
	
	private boolean isNameUsedInTemplates(String name){
		for(TimedArcPetriNet net : tapns){
			if(net.isNameUsed(name)) return true;
		}
		return false;
	}
		
	public boolean isNameUsed(String name) {
		return isNameUsedForShared(name) || isNameUsedInTemplates(name);
	}

	public void remove(TimedArcPetriNet tapn) {
		if (tapn != null) {
			tapn.setParentNetwork(null);
			tapns.remove(tapn);
			currentMarking.removeMarkingFor(tapn);
		}
	}
	
	public void remove(SharedPlace sharedPlace) {
		if (sharedPlace != null) {
			sharedPlace.setNetwork(null);
			sharedPlaces.remove(sharedPlace);
		}
	}
	
	public void remove(SharedTransition sharedTransition) {
		if (sharedTransition != null) {
			sharedTransition.setNetwork(null);
			sharedTransitions.remove(sharedTransition);
			sharedTransition.delete();
		}
	}
	

	public List<TimedArcPetriNet> templates() {
		return tapns;
	}

	public boolean hasTAPNCalled(String newName) {
		for (TimedArcPetriNet tapn : tapns)
			if (tapn.getName().equalsIgnoreCase(newName))
				return true;
		return false;
	}

	public NetworkMarking marking() {
		return currentMarking;
	}

	public void setMarking(NetworkMarking marking) {
		currentMarking = marking;
		for (TimedArcPetriNet tapn : tapns) {
			tapn.setMarking(currentMarking);
		}
	}

	public boolean isConstantNameUsed(String newName) {
		return constants.isConstantNameUsed(newName);
	}

	public void buildConstraints() {
		constants.buildConstraints(this);
	}

	public Command addConstant(String name, int val) {
		return constants.addConstant(name, val);
	}

	public Command removeConstant(String name) {
		return constants.removeConstant(name);
	}

	public Command updateConstant(String oldName, Constant constant) {
		Command edit = constants.updateConstant(oldName, constant, this);

		if (edit != null) {
			updateGuardsWithNewConstant(oldName, constant);
		}

		return edit;
	}

	public void updateGuardsWithNewConstant(String oldName, Constant newConstant) {
		for (TimedArcPetriNet tapn : templates()) {
			for (TimedPlace place : tapn.places()) {
				updatePlaceInvariant(oldName, newConstant, place);
			}

			for (TimedInputArc inputArc : tapn.inputArcs()) {
				updateTimeInterval(oldName, newConstant, inputArc.interval());
			}

			for (TransportArc transArc : tapn.transportArcs()) {
				updateTimeInterval(oldName, newConstant, transArc.interval());
			}

			for (TimedInhibitorArc inhibArc : tapn.inhibitorArcs()) {
				updateTimeInterval(oldName, newConstant, inhibArc.interval());
			}
		}

	}

	private void updatePlaceInvariant(String oldName, Constant newConstant, TimedPlace place) {
		updateBound(oldName, newConstant, place.invariant().upperBound());
	}

	private void updateTimeInterval(String oldName, Constant newConstant, TimeInterval interval) {
		updateBound(oldName, newConstant, interval.lowerBound());
		updateBound(oldName, newConstant, interval.upperBound());
	}

	private void updateBound(String oldName, Constant newConstant, Bound bound) {
		if (bound instanceof ConstantBound) {
			ConstantBound cb = (ConstantBound) bound;

			if (cb.name().equals(oldName)) {
				cb.setConstant(newConstant);
			}
		}
	}

	public Collection<Constant> constants() {
		return constants.getConstants();
	}

	public Set<String> getConstantNames() {
		return constants.getConstantNames();
	}

	public int getConstantValue(String name) {
		return constants.getConstant(name).value();
	}

	public int getLargestConstantValue() {
		return constants.getLargestConstantValue();
	}

	public void setConstants(Iterable<Constant> constants) {
		for (Constant c : constants) {
			this.constants.add(c);
		}
	}

	public Constant getConstant(String constantName) {
		return constants.getConstant(constantName);
	}

	public TimedArcPetriNet getTAPNByName(String name) {
		for (TimedArcPetriNet tapn : tapns) {
			if (tapn.getName().equals(name))
				return tapn;
		}
		return null;
	}

	
	
	public int numberOfSharedPlaces() {
		return sharedPlaces.size();
	}
	
	public int numberOfSharedTransitions() {
		return sharedTransitions.size();
	}

	public SharedPlace getSharedPlaceByIndex(int index) {
		return sharedPlaces.get(index);
	}

	public Object getSharedTransitionByIndex(int index) {
		return sharedTransitions.get(index);
	}

	public Collection<SharedTransition> sharedTransitions() {
		return sharedTransitions;
	}

	public Collection<SharedPlace> sharedPlaces() {
		return sharedPlaces;
	}

	public SharedTransition getSharedTransitionByName(String name) {
		for(SharedTransition t : sharedTransitions){
			if(t.name().equalsIgnoreCase(name)) return t;
		}
		return null;
	}

	public TimedPlace getSharedPlaceByName(String name) {
		for(SharedPlace place : sharedPlaces){
			if(place.name().equalsIgnoreCase(name)) return place;
		}
		return null;
	}
}
