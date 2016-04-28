package dk.aau.cs.model.tapn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import pipe.gui.Animator;

import dk.aau.cs.model.tapn.Bound.InfBound;
import dk.aau.cs.model.tapn.event.TimedTransitionEvent;
import dk.aau.cs.model.tapn.event.TimedTransitionListener;
import dk.aau.cs.model.tapn.simulation.FiringMode;
import dk.aau.cs.util.IntervalOperations;
import dk.aau.cs.util.Require;

public class TimedTransition extends TAPNElement {
	private static final Pattern namePattern = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");

	private String name;
	private List<TimedOutputArc> postset = new ArrayList<TimedOutputArc>();
	private List<TimedInputArc> preset = new ArrayList<TimedInputArc>();
	private List<TransportArc> transportArcsGoingThrough = new ArrayList<TransportArc>();
	private List<TimedInhibitorArc> inhibitorArcs = new ArrayList<TimedInhibitorArc>();
	private TimeInterval dInterval = null;
	private boolean isUrgent = false;

	private SharedTransition sharedTransition;

	private List<TimedTransitionListener> listeners = new ArrayList<TimedTransitionListener>();

	public TimedTransition(String name) {
		this(name, false);
	}
	
	public TimedTransition(String name, boolean isUrgent) {
		setName(name);
		setUrgent(isUrgent);
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
		if(isShared() && cascade){
			sharedTransition.setUrgent(value);
		}
	}
	
	public boolean hasUntimedPreset(){
		return hasUntimedPreset(true);
	}
	
	private boolean hasUntimedPreset(boolean cascade){
		for(TimedInputArc arc : preset){
			if(!arc.interval().equals(arc.interval().ZERO_INF)){
				return false;
			}
		}
		for (TransportArc arc : transportArcsGoingThrough){
			if(!arc.interval().equals(arc.interval().ZERO_INF)){
				return false;
			}
		}
		
		if(cascade && isShared()){
			for(TimedTransition trans : sharedTransition.transitions()){
				if(!trans.hasUntimedPreset(false)){
					return false;
				}
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

	public int presetSize() {
		return preset.size() + transportArcsGoingThrough.size();
	}

	public int postsetSize() {
		return postset.size() + transportArcsGoingThrough.size();
	}

	public boolean isDEnabled(){
		TimeInterval dInterval = calculateDInterval();

		if(dInterval != null){
			return true;
		}
		else{
			return false; 
		}
	}

	public TimeInterval calculateDInterval(){
		ArrayList<TimeInterval> result = new ArrayList<TimeInterval>();
		if(this.isShared()){
			result = sharedTransition.calculateDInterval();
		} else {
			result = this.calculateDIntervalAlone();
		}
		
		
		//Invariants
		for(TimedArcPetriNet model : model().parentNetwork().activeTemplates()){
			for(TimedPlace place : model.places()){
				if(!(place.invariant().upperBound() instanceof InfBound)){
					for(TimedToken x : place.tokens()){
						result = IntervalOperations.intersectingInterval(result, Arrays.asList(place.invariant().subtractToken(x.age())));
					}
				}
			}
		}
		
		//prevent delay if urgent transition is enabled
		if(Animator.isUrgentTransitionEnabled()){
			result = IntervalOperations.intersectingInterval(result, Arrays.asList(new TimeInterval(true, new IntBound(0), new IntBound(0), true)));
		}
		
		//cache result
		if(result.isEmpty()){
			dInterval = null;
		} else {
			dInterval = result.get(0);
		}
		return dInterval;
	}

	public ArrayList<TimeInterval> calculateDIntervalAlone(){
		ArrayList<TimeInterval> result = new ArrayList<TimeInterval>();
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

	public List<TimedToken> calculateProducedTokensFrom(List<TimedToken> consumedTokens) {
		// Assume that tokens enables transition

		ArrayList<TimedToken> producedTokens = new ArrayList<TimedToken>();
		for (TimedOutputArc arc : postset) {
			for(int i = 0; i < arc.getWeight().value(); i++){
				producedTokens.add(new TimedToken(arc.destination()));
			}
		}

		for (TransportArc transportArc : transportArcsGoingThrough) {
			for (TimedToken token : consumedTokens) {
				if (token.place().equals(transportArc.source())) {
					producedTokens.add(new TimedToken(transportArc.destination(), token.age()));
				}
			}
		}

		return producedTokens;
	}

	public List<TimedToken> calculateConsumedTokens(LocalTimedMarking timedMarking, FiringMode firingMode) { // TODO: timedMarking not being used, remove it?
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
		return new TimedTransition(name, isUrgent);
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
		if(dInterval == null){
			dInterval = calculateDInterval();
		}
		return dInterval;
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
}
