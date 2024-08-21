package dk.aau.cs.model.tapn;

import java.util.*;

import net.tapaal.gui.petrinet.undo.AddConstantEditCommand;
import net.tapaal.gui.petrinet.undo.RemoveConstantEditCommand;
import net.tapaal.gui.petrinet.undo.UpdateConstantEditCommand;
import net.tapaal.gui.petrinet.undo.Command;
import dk.aau.cs.model.CPN.ColoredTimeInterval;
import dk.aau.cs.model.CPN.ColoredTimeInvariant;
import dk.aau.cs.util.StringComparator;

public class ConstantStore {
	private List<Constant> constants = new ArrayList<Constant>();
	private int largest = -1;

	public ConstantStore() {

	}
	
	public ConstantStore(List<Constant> constants){
		this.constants = constants;
	}

	public Collection<Constant> getConstants() {
		return constants;
	}

	public void buildConstraints(TimedArcPetriNetNetwork model) {
		for (Constant c : constants) {
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
			
			for (TimedOutputArc outputArc : tapn.outputArcs()){
				buildConstraints(outputArc);
			}
		}

	}

	private void buildConstraints(TimedPlace place) {
		TimeInvariant invariant = place.invariant();

		Bound bound = invariant.upperBound();
		if (bound instanceof ConstantBound) {
			ConstantBound cb = (ConstantBound) bound;

			if (containsConstantByName(cb.name())) {
				Constant constant = getConstantByName(cb.name());
				constant.setIsUsed(true);
				if (!invariant.isUpperNonstrict()) {
					constant.setLowerBound(1);
				}
			} else
				throw new RuntimeException("An undefined constant " + cb.name()	+ " was used in an invariant.");
		}

		for (ColoredTimeInvariant cti : place.getCtiList()) {
			if (cti.upperBound() instanceof ConstantBound) {
				ConstantBound ctiCb = (ConstantBound) cti.upperBound();
				if (containsConstantByName(ctiCb.name())) {
					Constant ctiConstant = getConstantByName(ctiCb.name());
					ctiConstant.setIsUsed(true);
					if (!cti.isUpperNonstrict()) {
						ctiConstant.setLowerBound(1);
					}
				}
			}
		}
	}

	public boolean containsConstantByName(String name) {
		for(Constant c : constants) {
			if(c.name().equals(name))
				return true;
		}
		return false;
	}

	public Constant getConstantByName(String name) {
		for(Constant c : constants) {
			if(c.name().equals(name))
				return c;
		}
		return null;
	}
	
	private void buildConstraints(TimedInputArc inputArc) {
		buildConstraintsFromTimeInterval(inputArc.interval());
		for (ColoredTimeInterval cti : inputArc.getColorTimeIntervals()) {
			buildConstraintsFromTimeInterval(cti);
		}

		buildConstraintsFromWeight(inputArc.getWeight());
	}

	private void buildConstraints(TransportArc transArc) {
		buildConstraintsFromTimeInterval(transArc.interval());
		for (ColoredTimeInterval cti : transArc.getColorTimeIntervals()) {
			buildConstraintsFromTimeInterval(cti);
		}

		buildConstraintsFromWeight(transArc.getWeight());
	}

	private void buildConstraints(TimedInhibitorArc inhibArc) {
		buildConstraintsFromTimeInterval(inhibArc.interval());
		buildConstraintsFromWeight(inhibArc.getWeight());
	}
	
	private void buildConstraints(TimedOutputArc outputArc){
		buildConstraintsFromWeight(outputArc.getWeight());
	}

	private void buildConstraintsFromTimeInterval(TimeInterval interval) {
		Bound lower = interval.lowerBound();
		Bound upper = interval.upperBound();

		int diff = interval.isLowerBoundNonStrict()
				&& interval.isUpperBoundNonStrict() ? 0 : 1;

		if (lower instanceof ConstantBound && upper instanceof ConstantBound) {
			Constant lowerConstant = getConstantByName(((ConstantBound) lower).name());
			Constant upperConstant = getConstantByName(((ConstantBound) upper).name());

			lowerConstant.setIsUsed(true);
			upperConstant.setIsUsed(true);

			if (!lower.equals(upper)) {
				if (lowerConstant.value() + diff > upperConstant.lowerBound()) {
					upperConstant.setLowerBound(lowerConstant.value() + diff);
				}

				if (upperConstant.value() - diff < lowerConstant.upperBound()) {
					lowerConstant.setUpperBound(upperConstant.value() - diff);
				}
			}
		} else if (lower instanceof ConstantBound) {
			Constant lowerConstant = getConstantByName(((ConstantBound) lower).name());
			lowerConstant.setIsUsed(true);
			if (upper.value() - diff < lowerConstant.upperBound()) {
				lowerConstant.setUpperBound(upper.value() - diff);
			}
		} else if (upper instanceof ConstantBound) {
			Constant upperConstant = getConstantByName(((ConstantBound) upper).name());
			upperConstant.setIsUsed(true);
			if (lower.value() + diff > upperConstant.lowerBound()) {
				upperConstant.setLowerBound(lower.value() + diff);
			}
		}
	}
	
	private void buildConstraintsFromWeight(Weight weight) {
		if(weight instanceof ConstantWeight){
			Constant weightConstant = getConstantByName(((ConstantWeight) weight).constant().name());
			if(weightConstant.lowerBound() < 1){
				weightConstant.setIsUsed(true);
				weightConstant.setLowerBound(1);
			}
		}
	}

	public Command addConstant(String name, int val) {
		if (isNameInf(name))
			return null;

		if (!containsConstantByName(name)) {
			Constant c = new Constant(name, val);
			add(c);
			return new AddConstantEditCommand(c, this);
		}
		

		return null;
	}

	public void add(Constant constant) {
		if(!containsConstantByName(constant.name())) {
			constants.add(constant);
			if (constant.value() > largest)
				largest = constant.value();
		}
	}

	public Command removeConstant(String name) {
		if (!isConstantInUse(name)) {
			if (containsConstantByName(name)) {
				Constant c = getConstantByName(name);
				remove(c);
				findLargestConstantValue();
				return new RemoveConstantEditCommand(c, this);
			}
		}

		return null;
	}

	public boolean isConstantInUse(String name) {
		if (containsConstantByName(name)) {
			return getConstantByName(name).isUsed();
		}

		return false;
	}

	public void remove(Constant constant) {
		constants.remove(constant);
		findLargestConstantValue();
	}

	private void findLargestConstantValue() {
		largest = -1;

		for (Constant c : constants) {
			if (c.value() > largest)
				largest = c.value();
		}

	}

	public int getLargestConstantValue() {
		return largest;
	}

	private boolean isNameInf(String name) {
		return name.equals("inf");
	}

	public Command updateConstant(String oldName, Constant updatedConstant,	TimedArcPetriNetNetwork model) {
		if (oldName.equals(updatedConstant.name()) || !containsConstantByName(updatedConstant.name())) {
			if (containsConstantByName(oldName)) {
				Constant old = getConstantByName(oldName);
				updatedConstant.setLowerBound(old.lowerBound());
				updatedConstant.setUpperBound(old.upperBound());
				updatedConstant.setIsUsed(old.isUsed());
				int index = constants.indexOf(old);
				constants.remove(old);
				constants.add(index, updatedConstant);
				findLargestConstantValue();
				return new UpdateConstantEditCommand(old, updatedConstant, this, model);
			}
		}
		return null;
	}

	public Set<String> getConstantNames() {
		Set<String> names = new HashSet<String>();
		for(Constant c : constants)
			names.add(c.name());
		
		return names;
	}

	public void swapConstants(int currentIndex, int newIndex) {
		Constant temp = constants.get(currentIndex);
		constants.set(currentIndex, constants.get(newIndex));
		constants.set(newIndex, temp);
	}
	
	public Constant[] sortConstants() {
		Constant[] oldOrder = constants.toArray(new Constant[0]);
		constants.sort(new StringComparator());
		return oldOrder;
	}
	
	public void undoSort(Constant[] oldOrder) {
		constants.clear();
		constants.addAll(Arrays.asList(oldOrder));
		
	}

	public Constant getConstantByIndex(int index) {
		return constants.get(index);
	}

	public int getIndexOf(Constant constant) {
		return constants.indexOf(constant);
	}
}
