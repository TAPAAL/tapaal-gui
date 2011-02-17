package dk.aau.cs.model.tapn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.util.Require;

public class TimedArcPetriNetNetwork {
	private List<TimedArcPetriNet> tapns;
	private List<SharedPlace> sharedPlaces;
	private List<SharedTransition> sharedTransitions;
	private NetworkMarking currentMarking;

	private ConstantStore constants;

	public TimedArcPetriNetNetwork() {
		tapns = new ArrayList<TimedArcPetriNet>();
		sharedPlaces = new ArrayList<SharedPlace>();
		sharedTransitions = new ArrayList<SharedTransition>();
		currentMarking = new NetworkMarking();
		constants = new ConstantStore();
	}

	public void add(TimedArcPetriNet tapn) {
		Require.that(tapn != null, "tapn must be non-null");
		Require.that(tapn.marking() != null, "Marking must be non-null");

		tapns.add(tapn);
		currentMarking.addMarking(tapn, tapn.marking());
	}

	public void remove(TimedArcPetriNet tapn) {
		if (tapn != null) {
			tapns.remove(tapn);
			currentMarking.removeMarkingFor(tapn);

		}
	}

	public List<TimedArcPetriNet> templates() {
		return tapns;
	}

	public boolean hasTAPNCalled(String newName) {
		for (TimedArcPetriNet tapn : tapns)
			if (tapn.getName().toLowerCase().equals(newName.toLowerCase()))
				return true;
		return false;
	}

	public NetworkMarking marking() {
		return currentMarking;
	}

	public void setMarking(NetworkMarking marking) {
		currentMarking = marking;
		for (TimedArcPetriNet tapn : tapns) {
			tapn.setMarking(marking.getMarkingFor(tapn));
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

	private void updatePlaceInvariant(String oldName, Constant newConstant,
			TimedPlace place) {
		updateBound(oldName, newConstant, place.invariant().upperBound());
	}

	private void updateTimeInterval(String oldName, Constant newConstant,
			TimeInterval interval) {
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

}
