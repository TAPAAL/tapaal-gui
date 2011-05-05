package dk.aau.cs.model.tapn;

import java.util.Collection;
import java.util.Set;
import java.util.TreeMap;

import pipe.gui.undo.AddConstantEdit;
import pipe.gui.undo.RemoveConstantEdit;
import pipe.gui.undo.UpdateConstantEdit;
import dk.aau.cs.gui.undo.Command;

public class ConstantStore {
	private TreeMap<String, Constant> constants = new TreeMap<String, Constant>();
	private int largest = -1;

	public ConstantStore() {

	}
	
	public ConstantStore(Iterable<Constant> constants){
		for(Constant c : constants){
			this.constants.put(c.name(), c);
		}			
	}

	public Collection<Constant> getConstants() {
		return constants.values();
	}

	public void buildConstraints(TimedArcPetriNetNetwork model) {
		for (Constant c : constants.values()) {
			c.reset();
		}

		for (TimedArcPetriNet tapn : model.allTemplates()) {
			for (TimedPlace place : tapn.places()) {
				buildConstraints(place);
			}

			for (TimedInputArc inputArc : tapn.inputArcs()) {
				buildConstraints(inputArc);
			}

			for (TransportArc transArc : tapn.transportArcs()) {
				buildConstraints(transArc);
			}

			for (TimedInhibitorArc inhibArc : tapn.inhibitorArcs()) {
				buildConstraints(inhibArc);
			}
		}

	}

	private void buildConstraints(TimedPlace place) {
		TimeInvariant invariant = place.invariant();

		Bound bound = invariant.upperBound();
		if (bound instanceof ConstantBound) {
			ConstantBound cb = (ConstantBound) bound;

			if (constants.containsKey(cb.name())) {
				Constant constant = constants.get(cb.name());
				constant.setIsUsed(true);
				if (!invariant.isUpperNonstrict()) {
					constant.setLowerBound(1);
				}
			} else
				throw new RuntimeException("An undefined constant " + cb.name()
						+ " was used in an invariant.");
		}

	}

	private void buildConstraints(TimedInputArc inputArc) {
		buildConstraintsFromTimeInterval(inputArc.interval());
	}

	private void buildConstraints(TransportArc transArc) {
		buildConstraintsFromTimeInterval(transArc.interval());
	}

	private void buildConstraints(TimedInhibitorArc inhibArc) {
		buildConstraintsFromTimeInterval(inhibArc.interval());
	}

	private void buildConstraintsFromTimeInterval(TimeInterval interval) {
		Bound lower = interval.lowerBound();
		Bound upper = interval.upperBound();

		int diff = interval.IsLowerBoundNonStrict()
				&& interval.IsUpperBoundNonStrict() ? 0 : 1;

		if (lower instanceof ConstantBound && upper instanceof ConstantBound) {
			Constant lowerConstant = constants.get(((ConstantBound) lower)
					.name());
			Constant upperConstant = constants.get(((ConstantBound) upper)
					.name());

			lowerConstant.setIsUsed(true);
			upperConstant.setIsUsed(true);

			if (lower != upper) {
				if (lowerConstant.value() + diff > upperConstant.lowerBound()) {
					upperConstant.setLowerBound(lowerConstant.value() + diff);
				}

				if (upperConstant.value() - diff < lowerConstant.upperBound()) {
					lowerConstant.setUpperBound(upperConstant.value() - diff);
				}
			}
		} else if (lower instanceof ConstantBound) {
			Constant lowerConstant = constants.get(((ConstantBound) lower)
					.name());
			lowerConstant.setIsUsed(true);
			if (upper.value() - diff < lowerConstant.upperBound()) {
				lowerConstant.setUpperBound(upper.value() - diff);
			}
		} else if (upper instanceof ConstantBound) {
			Constant upperConstant = constants.get(((ConstantBound) upper)
					.name());
			upperConstant.setIsUsed(true);
			if (lower.value() + diff > upperConstant.lowerBound()) {
				upperConstant.setLowerBound(lower.value() + diff);
			}
		}
	}

	public Command addConstant(String name, int val) {
		if (isNameInf(name))
			return null;

		if (!constants.containsKey(name)) {
			Constant c = new Constant(name, val);
			add(c);
			return new AddConstantEdit(c, this);
		}

		return null;
	}

	public void add(Constant constant) {
		constants.put(constant.name(), constant);
		if (constant.value() > largest)
			largest = constant.value();
	}

	public Command removeConstant(String name) {
		if (!isConstantInUse(name)) {
			if (constants.containsKey(name)) {
				Constant c = constants.get(name);
				remove(c);
				findLargestConstantValue();
				return new RemoveConstantEdit(c, this);
			}
		}

		return null;
	}

	private boolean isConstantInUse(String name) {
		if (constants.containsKey(name)) {
			return constants.get(name).isUsed();
		}

		return false;
	}

	public void remove(Constant constant) {
		constants.remove(constant.name());
		findLargestConstantValue();
	}

	private void findLargestConstantValue() {
		largest = -1;

		for (Constant c : constants.values()) {
			if (c.value() > largest)
				largest = c.value();
		}

	}

	public int getLargestConstantValue() {
		return largest;
	}

	public boolean isConstantNameUsed(String constantName) {
		return constants.containsKey(constantName);
	}

	private boolean isNameInf(String name) {
		return name.toLowerCase().equals("inf");
	}

	public Command updateConstant(String oldName, Constant updatedConstant,
			TimedArcPetriNetNetwork model) {
		if (oldName.equals(updatedConstant.name())
				|| !constants.containsKey(updatedConstant.name())) {
			if (constants.containsKey(oldName)) {
				Constant old = constants.get(oldName);
				updatedConstant.setLowerBound(old.lowerBound());
				updatedConstant.setUpperBound(old.upperBound());
				updatedConstant.setIsUsed(old.isUsed());
				constants.remove(oldName);
				constants.put(updatedConstant.name(), updatedConstant);
				findLargestConstantValue();
				return new UpdateConstantEdit(old, updatedConstant, this, model);
			}
		}
		return null;
	}

	public Set<String> getConstantNames() {
		return constants.keySet();
	}

	public Constant getConstant(String name) {
		return constants.get(name);
	}
}
