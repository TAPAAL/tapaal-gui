package dk.aau.cs.model.tapn;

import java.util.*;

import dk.aau.cs.model.CPN.ColoredTimeInterval;
import dk.aau.cs.model.CPN.ColoredTimeInvariant;
import dk.aau.cs.util.StringComparator;

public class ConstantStore {
	private List<Constant> constants = new ArrayList<Constant>();
	private List<RealConstant> realConstants = new ArrayList<RealConstant>();
	private int largest = -1;

	public ConstantStore() {

	}

	public ConstantStore(List<Constant> constants){
		this.constants = constants;
	}

	public ConstantStore(List<Constant> constants, List<RealConstant> realConstants){
		this.constants = constants;
		this.realConstants = realConstants;
	}

	public Collection<Constant> getConstants() {
		return constants;
	}

	public List<RealConstant> getRealConstants() {
		return realConstants;
	}

	public void buildConstraints(TimedArcPetriNetNetwork model) {
		for (Constant c : constants) {
			c.reset();
		}

		for (var c : realConstants) {
			c.setIsUsed(false);
		}

		for (TimedArcPetriNet tapn : model.allTemplates()) {
			for (TimedPlace place : tapn.places()) {
				buildConstraints(place);
			}

            for (TimedTransition transition : tapn.transitions()) {
                buildConstraints(transition);
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

		for (SharedTransition sharedTransition : model.sharedTransitions()) {
			if (sharedTransition.getDistribution() != null) {
				for (SMCParameterConstant constant : sharedTransition.getDistribution().getParamRefs().values()) {
					constant.setIsUsed(true);
				}
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

    public void buildConstraints(TimedTransition transition) {
        Probability weight = transition.getWeight();
        if(weight instanceof ConstantProbability) {
            Constant constant = ((ConstantProbability) weight).constant();
            constant.setIsUsed(true);
        }

        if (transition.getDistribution() != null) {
            for (SMCParameterConstant constant : transition.getDistribution().getParamRefs().values()) {
                constant.setIsUsed(true);
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

	public boolean containsRealConstantByName(String name) {
		for (var c : realConstants) {
			if (c.name().equals(name)) {
                return true;
            }
		}

		return false;
	}

	public RealConstant getRealConstantByName(String name) {
		for (var c : realConstants) {
			if (c.name().equals(name)) {
				return c;
            }
		}

		return null;
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
				int lowerConstVal = lowerConstant.hasMultipleValues() ? Collections.max(lowerConstant.values()) : lowerConstant.value();
					int upperConstVal = upperConstant.hasMultipleValues() ? Collections.min(upperConstant.values()) : upperConstant.value();
					if (lowerConstVal + diff > upperConstant.lowerBound()) {
					upperConstant.setLowerBound(lowerConstVal + diff);
				}

				if (upperConstVal - diff < lowerConstant.upperBound()) {
					lowerConstant.setUpperBound(upperConstVal - diff);
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

	public Constant addConstantValue(String name, LinkedHashSet<Integer> vals) {
		if (isNameInf(name) || containsRealConstantByName(name))
			return null;

		if (!containsConstantByName(name)) {
			Constant c = new Constant(name, vals);
			add(c);
			Collections.sort(constants, new StringComparator());

			return c;
		}

		return null;
	}

	public Constant addConstantValue(String name, int val) {
		if (isNameInf(name) || containsRealConstantByName(name))
			return null;

		if (!containsConstantByName(name)) {
			Constant c = new Constant(name, val);
			add(c);
			return c;
		}
		

		return null;
	}

	public void add(Constant constant) {
		if(!containsConstantByName(constant.name())) {
			constants.add(constant);
			if (constant.hasMultipleValues()) {
				int max = Collections.max(constant.values());
				if (max > largest) largest = max;
			} else if (constant.value() > largest) {
				largest = constant.value();
			}
		}
	}

	public Constant removeConstantValue(String name) {
		if (!isConstantInUse(name)) {
			if (containsConstantByName(name)) {
				Constant c = getConstantByName(name);
				remove(c);
				findLargestConstantValue();
				return c;
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

	public void replace(Constant oldConstant, Constant newConstant) {
		int index = constants.indexOf(oldConstant);
		if (index < 0) {
			add(newConstant);
			return;
		}
		constants.set(index, newConstant);
		findLargestConstantValue();
	}

	private void findLargestConstantValue() {
		largest = -1;

		for (Constant c : constants) {
			if (c.hasMultipleValues()) {
				int max = Collections.max(c.values());
				if (max > largest) largest = max;
			} else if (c.value() > largest) {
				largest = c.value();
			}
		}

	}

	public int getLargestConstantValue() {
		return largest;
	}

	private boolean isNameInf(String name) {
		return name.equals("inf");
	}

	public Constant updateConstantValue(String oldName, Constant updatedConstant) {
		if (containsRealConstantByName(updatedConstant.name())) {
			return null;
        }

		if (oldName.equals(updatedConstant.name()) || !containsConstantByName(updatedConstant.name())) {
			if (containsConstantByName(oldName)) {
				Constant old = getConstantByName(oldName);
				updatedConstant.setLowerBound(old.lowerBound());
				updatedConstant.setUpperBound(old.upperBound());
				updatedConstant.setIsUsed(old.isUsed());
				replace(old, updatedConstant);
				return updatedConstant;
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

    public void clear() {
        constants.clear();
        realConstants.clear();
        largest = -1;
    }

	public Constant getConstantByIndex(int index) {
		return constants.get(index);
	}

	public int getIndexOf(Constant constant) {
		return constants.indexOf(constant);
	}

	public RealConstant addRealConstantValue(String name, LinkedHashSet<Double> vals) {
		if (isNameInf(name) || containsRealConstantByName(name) || containsConstantByName(name)) {
			return null;
		}

		var c = new RealConstant(name, vals);
		realConstants.add(c);
		realConstants.sort(new StringComparator());
		return c;
	}

	public void add(RealConstant constant) {
		if (!containsRealConstantByName(constant.name())) {
			realConstants.add(constant);
		}
	}

	public void remove(RealConstant constant) {
		realConstants.remove(constant);
	}

	public void replace(RealConstant oldConstant, RealConstant newConstant) {
		int index = realConstants.indexOf(oldConstant);
		if (index < 0) {
			add(newConstant);
			return;
		}
        
		realConstants.set(index, newConstant);
	}

	public RealConstant removeRealConstantValue(String name) {
		if (isRealConstantInUse(name)) {
			return null;
		}

		var c = getRealConstantByName(name);
		if (c == null) {
			return null;
		}

		realConstants.remove(c);
		return c;
	}

	public boolean isRealConstantInUse(String name) {
		var c = getRealConstantByName(name);
		return c != null && c.isUsed();
	}

	public RealConstant updateRealConstantValue(String oldName, RealConstant updatedConstant) {
		if (!oldName.equals(updatedConstant.name())
				&& (containsRealConstantByName(updatedConstant.name()) || containsConstantByName(updatedConstant.name()))) {
			return null;
		}

		var old = getRealConstantByName(oldName);
		if (old == null) {
			return null;
		}

		updatedConstant.setIsUsed(old.isUsed());
		replace(old, updatedConstant);
		return updatedConstant;
	}

	public void swapRealConstants(int currentIndex, int newIndex) {
		var temp = realConstants.get(currentIndex);
		realConstants.set(currentIndex, realConstants.get(newIndex));
		realConstants.set(newIndex, temp);
	}

	public List<RealConstant> sortRealConstants() {
		List<RealConstant> oldOrder = List.copyOf(realConstants);
		realConstants.sort(new StringComparator());
		return oldOrder;
	}

	public void undoSortRealConstants(List<RealConstant> oldOrder) {
		realConstants.clear();
		realConstants.addAll(oldOrder);
	}

	public RealConstant getRealConstantByIndex(int index) {
		return realConstants.get(index);
	}
}
