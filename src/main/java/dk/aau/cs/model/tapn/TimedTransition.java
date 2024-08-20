package dk.aau.cs.model.tapn;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.ColoredTimeInterval;
import dk.aau.cs.model.CPN.Expressions.GuardExpression;
import pipe.gui.petrinet.animation.Animator;

import dk.aau.cs.model.tapn.Bound.InfBound;
import dk.aau.cs.model.tapn.event.TimedTransitionEvent;
import dk.aau.cs.model.tapn.event.TimedTransitionListener;
import dk.aau.cs.model.tapn.simulation.FiringMode;
import dk.aau.cs.util.IntervalOperations;
import dk.aau.cs.util.Require;

public class TimedTransition extends TAPNElement {
	private static final Pattern namePattern = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");

	private String name;
	private final List<TimedOutputArc> postset = new ArrayList<TimedOutputArc>();
	private final List<TimedInputArc> preset = new ArrayList<TimedInputArc>();
	private final List<TransportArc> transportArcsGoingThrough = new ArrayList<TransportArc>();
	private final List<TimedInhibitorArc> inhibitorArcs = new ArrayList<TimedInhibitorArc>();

	private boolean isUrgent = false;
	private boolean isUncontrollable = false;
    private SMCDistribution distribution = SMCDistribution.defaultDistribution();
    private Probability weight = new DoubleProbability(1.0);
	private FiringMode firingMode;
    private GuardExpression guard;

	private SharedTransition sharedTransition;

	private final List<TimedTransitionListener> listeners = new ArrayList<TimedTransitionListener>();

	public TimedTransition(String name) {
		this(name, false, null);
	}

    public TimedTransition(String name, GuardExpression guard) {
        this(name, false, guard);
    }
	
	public TimedTransition(String name, boolean isUrgent, GuardExpression guard) {
		setName(name);
		setUrgent(isUrgent);
		this.guard = guard;
	}

    public TimedTransition(String name, boolean isUrgent, GuardExpression guard, SMCDistribution distribution) {
        setName(name);
        setUrgent(isUrgent);
        setDistribution(distribution);
        this.guard = guard;
    }

    public TimedTransition(String name, boolean isUrgent, GuardExpression guard, SMCDistribution distribution, Probability weight, FiringMode firingMode) {
        setName(name);
        setUrgent(isUrgent);
        setDistribution(distribution);
        setWeight(weight);
		setFiringMode(firingMode);
        this.guard = guard;
    }

	public void addTimedTransitionListener(TimedTransitionListener listener){
		Require.that(listener != null, "listener cannot be null");
		listeners.add(listener);
	}

	public void removeListener(TimedTransitionListener listener){
		Require.that(listener != null, "listener cannot be null");
		listeners.remove(listener);
	}
	
	public boolean isUrgent(){
		return isUrgent;
	}
	
	public void setUrgent(boolean value){
		setUrgent(value, true);
	}
	
	protected void setUrgent(boolean value, boolean cascade){
		isUrgent = value;
        if (isUrgent) {
            setDistribution(SMCDistribution.urgent());
        }
		if(isShared() && cascade){
			sharedTransition.setUrgent(value);
		}
	}

	public boolean isUncontrollable() {
	    return isUncontrollable;
    }

    public void setUncontrollable(boolean isUncontrollable) {
	    setUncontrollable(isUncontrollable, true);
    }

	public void setUncontrollable(boolean isUncontrollable, boolean cascade) {
	    this.isUncontrollable = isUncontrollable;
	    if (isShared() && cascade) {
	        sharedTransition.setUncontrollable(isUncontrollable);
        }
    }

    public SMCDistribution getDistribution() { return distribution; }

    public void setDistribution(SMCDistribution distrib) { setDistribution(distrib, true); }

    public void setDistribution(SMCDistribution distrib, boolean cascade) {
        this.distribution = distrib;
        if(isShared() && cascade) {
            sharedTransition.setDistribution(distrib);
        }
    }

    public Probability getWeight() { return weight; }

    public void setWeight(Probability weight) { setWeight(weight, true); }

    public void setWeight(Probability weight, boolean cascade) {
        this.weight = weight;
        if(isShared() && cascade) {
            sharedTransition.setWeight(weight);
        }
    }

	public void setFiringMode(FiringMode firingMode) {
		this.firingMode = firingMode;
	}

	public FiringMode getFiringMode() {
		return firingMode;
	}

    public boolean hasCustomDistribution() {
        return !this.distribution.equals(SMCDistribution.defaultDistribution());
    }
	
	public boolean hasUntimedPreset(){
		return hasUntimedPreset(true);
	}

	private boolean hasUntimedPreset(boolean cascade) {
		for (TimedInputArc arc : preset) {
			if (!arc.interval().equals(TimeInterval.ZERO_INF)) {
				return false;
			}
			for (ColoredTimeInterval interval : arc.getColorTimeIntervals()) {
                if (!interval.getInterval().equals(TimeInterval.ZERO_INF.toString()))
                    return false;
            }
		}
		for (TransportArc arc : transportArcsGoingThrough) {
			if (!arc.interval().equals(TimeInterval.ZERO_INF))
				return false;
            for (ColoredTimeInterval interval : arc.getColorTimeIntervals()) {
                if (interval.getInterval().equals(TimeInterval.ZERO_INF.toString()))
                    return false;
            }
		}
		
		if (cascade && isShared()) {
			for (TimedTransition trans : sharedTransition.transitions()) {
				if (!trans.hasUntimedPreset(false))
					return false;
			}
		}
		return true;
	}

	public boolean isShared(){
		return sharedTransition != null;
	}

	public SharedTransition sharedTransition(){
		return sharedTransition;
	}

	public void makeShared(SharedTransition sharedTransition){
		Require.that(sharedTransition != null, "sharedTransition cannot be null");

		if(this.sharedTransition != sharedTransition){
			unshare();
			this.sharedTransition = sharedTransition;
			setName(sharedTransition.name());
			fireSharedStateChanged();
		}
	}

	public void unshare() {
		if(isShared()){
			sharedTransition.unshare(this);
			sharedTransition = null;
			fireSharedStateChanged();
		}
	}

	public String name() {
		return name;
	}

	public void setName(String newName) {
		Require.that(newName != null && !newName.isEmpty(), "A timed transition must have a name");
		Require.that(isValid(newName), "The specified name must conform to the pattern [a-zA-Z_][a-zA-Z0-9_]*, Name: " + newName);
		name = newName;
		fireNameChanged();
	}

	private void fireNameChanged(){
		for(TimedTransitionListener listener : listeners){
			listener.nameChanged(new TimedTransitionEvent(this));
		}
	}

	private void fireSharedStateChanged() {
		for(TimedTransitionListener listener : listeners){
			listener.sharedStateChanged(new TimedTransitionEvent(this));
		}
	}

	private boolean isValid(String newName) {
		return namePattern.matcher(newName).matches();
	}

	public void addToPreset(TimedInputArc arc) {
		Require.that(arc != null, "Cannot add null to preset");
		preset.add(arc);
	}

	public void addToPostset(TimedOutputArc arc) {
		Require.that(arc != null, "Cannot add null to postset");
		postset.add(arc);
	}


	public void removeFromPreset(TimedInputArc arc) {
		preset.remove(arc);
	}

	public void removeFromPostset(TimedOutputArc arc) {
		postset.remove(arc);
	}

	public void addTransportArcGoingThrough(TransportArc arc) {
		Require.that(arc != null, "Cannot add null to preset");
		transportArcsGoingThrough.add(arc);
	}

	public void removeTransportArcGoingThrough(TransportArc arc) {
		transportArcsGoingThrough.remove(arc);
	}

	public void addInhibitorArc(TimedInhibitorArc arc) {
		inhibitorArcs.add(arc);
	}

	public void removeInhibitorArc(TimedInhibitorArc arc) {
		inhibitorArcs.remove(arc);
	}

	@Override
	public void delete() {
		unshare();
		model().remove(this);
	}

	//XXX: See bug #1887524, old degree-2 converter does not expect inhibitorArcs to count in the size of preset
	public int presetSizeWithoutInhibitorArcs() {
		return preset.size() + transportArcsGoingThrough.size();
	}
    public int presetSize() {
        return preset.size() + transportArcsGoingThrough.size() + inhibitorArcs.size();
    }

	public int postsetSize() {
		return postset.size() + transportArcsGoingThrough.size();
	}

	public boolean isDEnabled(){
		TimeInterval dInterval = calculateDInterval();

		return dInterval != null;
	}

	public TimeInterval calculateDInterval(){
		ArrayList<TimeInterval> result;
		if (this.isShared()) {
			result = sharedTransition.calculateDInterval();
		} else {
			result = this.calculateDIntervalAlone();
		}
		
		
		//Invariants
		for(TimedArcPetriNet model : model().parentNetwork().activeTemplates()){
			for(TimedPlace place : model.places()){
				if(!(place.invariant().upperBound() instanceof InfBound)){
					for(TimedToken x : place.tokens()){
						result = IntervalOperations.intersectingInterval(result, List.of(place.invariant().subtractToken(x.age())));
					}
				}
			}
		}
		
		//prevent delay if urgent transition is enabled
		if(Animator.isUrgentTransitionEnabled()){
			result = IntervalOperations.intersectingInterval(result, List.of(new TimeInterval(true, new IntBound(0), new IntBound(0), true)));
		}

        TimeInterval dInterval;
		//cache result
		if(result.isEmpty()){
			dInterval = null;
		} else {
			dInterval = result.get(0);
		}
		return dInterval;
	}

	public ArrayList<TimeInterval> calculateDIntervalAlone(){
		ArrayList<TimeInterval> result = new ArrayList<>();
		result.add(TimeInterval.ZERO_INF);

		for(TimedInputArc arc : this.getInputArcs()){
			result = IntervalOperations.intersectingInterval(arc.getDEnabledInterval(), result);
		}

		for(TransportArc arc : this.getTransportArcsGoingThrough()){
			result = IntervalOperations.intersectingInterval(arc.getDEnabledInterval(), result);
		}

		for(TimedInhibitorArc arc : this.getInhibitorArcs()){
			result = IntervalOperations.intersectingInterval(arc.getDEnabledInterval(), result);
		}

		return result;
	}

	public boolean isEnabled() {
		if(isShared()){
			return sharedTransition.isEnabled();
		}else{
			return isEnabledAlone();
		}
	}

	public boolean isEnabledAlone() {
		for (TimedInputArc arc : preset) {
			if (!arc.isEnabled())
				return false;
		}
		for (TransportArc arc : transportArcsGoingThrough) {
			if (!arc.isEnabled())
				return false;
		}
		for (TimedInhibitorArc arc : inhibitorArcs) {
			if (!arc.isEnabled())
				return false;
		}
		return true;
	}

	public boolean isEnabledBy(List<TimedToken> tokens) {

		for(TimedInputArc inputArc : preset){
			int tokensMissing = inputArc.getWeight().value();
			for (TimedToken token : tokens) {
				if (inputArc.source().equals(token.place()) && inputArc.isEnabledBy(token)) {
					tokensMissing--;
				}
			}
			if(tokensMissing != 0) return false;
		}

		for(TransportArc transportArc : transportArcsGoingThrough){
			int tokensMissing = transportArc.getWeight().value();
			for (TimedToken token : tokens) {
				if (transportArc.source().equals(token.place()) && transportArc.isEnabledBy(token)) {
					tokensMissing--;
				}
			}
			if(tokensMissing != 0) return false;
		}

		return true;
	}

    //TODO: If we ever want to simulate colored nets we need to add colors to this
    // for now it is okay since we cannot enter colored simulation
	public List<TimedToken> calculateProducedTokensFrom(List<TimedToken> consumedTokens) {
		// Assume that tokens enables transition

		ArrayList<TimedToken> producedTokens = new ArrayList<TimedToken>();
		for (TimedOutputArc arc : postset) {
			for(int i = 0; i < arc.getWeight().value(); i++){

				producedTokens.add(new TimedToken(arc.destination(), ColorType.COLORTYPE_DOT.getFirstColor()));
			}
		}

		for (TransportArc transportArc : transportArcsGoingThrough) {
			for (TimedToken token : consumedTokens) {
				if (token.place().equals(transportArc.source())) {
					producedTokens.add(new TimedToken(transportArc.destination(), token.age(), ColorType.COLORTYPE_DOT.getFirstColor()));
				}
			}
		}

		return producedTokens;
	}

	public List<TimedToken> calculateConsumedTokens(FiringMode firingMode) {
		List<TimedToken> tokensToConsume = new ArrayList<TimedToken>();

		for (TimedInputArc arc : preset) {
			List<TimedToken> tokens = firingMode.pickTokensFrom(arc.getElligibleTokens(), arc.getWeight().value());
			tokensToConsume.addAll(tokens);
		}

		for (TransportArc arc : transportArcsGoingThrough) {
			List<TimedToken> tokens = firingMode.pickTokensFrom(arc.getElligibleTokens(), arc.getWeight().value());
			tokensToConsume.addAll(tokens);
		}

		return tokensToConsume;
	}

	public boolean hasInhibitorArcs() {
		return inhibitorArcs.size() > 0;
	}

	public List<TimedInputArc> getInputArcs(){
		return preset;
	}

	public List<TimedOutputArc> getOutputArcs() {
		return postset;
	}

	public List<TransportArc> getTransportArcsGoingThrough(){
		return transportArcsGoingThrough;
	}

	public int getNumberOfTransportArcsGoingThrough() {
		return transportArcsGoingThrough.size();
	}

	public List<TimedInhibitorArc> getInhibitorArcs() {
		return inhibitorArcs;
	}

	public TimedTransition copy() {
	    if(guard == null){
            return new TimedTransition(name, isUrgent, null, distribution, weight, firingMode);
        }
		return new TimedTransition(name, isUrgent, guard.copy(), distribution, weight, firingMode);
	}

	@Override
	public String toString() {
		if (model() != null)
			return model().name() + "." + name;
		else
			return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((model() == null) ? 0 : model().hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof TimedTransition))
			return false;
		TimedTransition other = (TimedTransition) obj;
		if (model() == null) {
			if (other.model() != null)
				return false;
		} else if (!model().equals(other.model()))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	public boolean isOrphan() {
		if(isShared()){
			return sharedTransition.isOrphan();
		}else{
			return presetSize() == 0 && postsetSize() == 0;
		}
	}

	/*
	 * Returns the dInterval lastly calculated
	 */
	public TimeInterval getdInterval() {
		return calculateDInterval();
	}

	public int getLagestAssociatedConstant() {
		int biggestConstant = -1;
		for(TimedInputArc arc : preset){
			Bound max = IntervalOperations.getMaxNoInfBound(arc.interval());
			if(max.value() > biggestConstant){
				biggestConstant = max.value();
			}
			max = arc.source().invariant().upperBound();
			if(max instanceof InfBound && max.value() > biggestConstant){
				biggestConstant = max.value();
			}
		}

		for(TransportArc arc : transportArcsGoingThrough){
			Bound max = IntervalOperations.getMaxNoInfBound(arc.interval());
			if(max.value() > biggestConstant){
				biggestConstant = max.value();
			}
			max = arc.source().invariant().upperBound();
			if(max instanceof InfBound && max.value() > biggestConstant){
				biggestConstant = max.value();
			}

			max = arc.destination().invariant().upperBound();
			if(max instanceof InfBound && max.value() > biggestConstant){
				biggestConstant = max.value();
			}
		}

		return biggestConstant;
	}

    public GuardExpression getGuard() {return guard;}
    public void setGuard(GuardExpression guard) {
        setGuard(guard,true);

    }

    public void setGuard(GuardExpression guard, boolean cascade) {
        this.guard = guard;
	    if (isShared() && cascade) {
	        if(guard != null){
                sharedTransition.setGuard(guard.copy());
            } else{
	            sharedTransition.setGuard(null);
            }
        }
	}
}
