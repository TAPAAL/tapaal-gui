package dk.aau.cs.model.tapn;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import dk.aau.cs.model.tapn.event.TimedPlaceEvent;
import dk.aau.cs.model.tapn.event.TimedPlaceListener;
import dk.aau.cs.util.Require;

public class TimedPlace extends TAPNElement {
	private static final Pattern namePattern = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");

	private String name;
	private TimeInvariant invariant;
	private List<TimedOutputArc> preset = new ArrayList<TimedOutputArc>();
	private List<TimedInputArc> postset = new ArrayList<TimedInputArc>();
	private List<TransportArc> presetTransportArcs = new ArrayList<TransportArc>();
	private List<TransportArc> postsetTransportArcs = new ArrayList<TransportArc>();
	private List<TimedInhibitorArc> inhibitorArcs = new ArrayList<TimedInhibitorArc>();

	private TimedMarking currentMarking;
	private SharedPlace sharedPlace;

	private List<TimedPlaceListener> listeners = new ArrayList<TimedPlaceListener>();

	public TimedPlace(String name) {
		this(name, TimeInvariant.LESS_THAN_INFINITY);
	}

	public TimedPlace(String name, TimeInvariant invariant) {
		setName(name);
		setInvariant(invariant);
	}

	public void addTimedPlaceListener(TimedPlaceListener listener){
		Require.that(listener != null, "Listener cannot be null");
		listeners.add(listener);
	}

	public void removeTimedPlaceListener(TimedPlaceListener listener){
		Require.that(listener != null, "Listener cannot be null");
		listeners.remove(listener);
	}

	public void makeShared(SharedPlace sharedPlace) {
		Require.that(sharedPlace != null, "sharedPlace cannot be null");
		
		if(this.sharedPlace != sharedPlace){
			unshare();
			this.sharedPlace = sharedPlace;
			setName(sharedPlace.name());
			setInvariant(sharedPlace.invariant());
			currentMarking.removePlaceFromMarking(this);
			fireSharedStateChanged();
		}
	}	

	public void unshare() {
		if(isShared()){
			sharedPlace.unshare(this);
			sharedPlace = null;
			fireSharedStateChanged();
		}
	}

	public boolean isShared() {
		return sharedPlace != null;
	}

	public SharedPlace sharedPlace() {
		return sharedPlace;
	}

	public void setCurrentMarking(TimedMarking newMarking) {
		Require.that(newMarking != null, "newMarking cannot be null");
		this.currentMarking = newMarking;
	}

	public String name() {
		return name;
	}

	public void setName(String newName) {
		Require.that(newName != null && !newName.isEmpty(), "A timed transition must have a name");
		Require.that(isValid(newName), "The specified name must conform to the pattern [a-zA-Z_][a-zA-Z0-9_]*");
		this.name = newName;
		fireNameChanged();
	}

	private void fireNameChanged() {
		for(TimedPlaceListener listener : listeners){
			listener.nameChanged(new TimedPlaceEvent(this));
		}
	}
	

	private void fireSharedStateChanged() {
		for(TimedPlaceListener listener : listeners){
			listener.sharedStateChanged(new TimedPlaceEvent(this));
		}
	}
	
	private void fireInvariantChanged(){
		for(TimedPlaceListener listener : listeners){
			listener.invariantChanged(new TimedPlaceEvent(this));
		}
	}
	
	private void fireMarkingChanged(){
		for(TimedPlaceListener listener : listeners){
			listener.markingChanged(new TimedPlaceEvent(this));
		}
	}
	

	private boolean isValid(String newName) {
		return namePattern.matcher(newName).matches();
	}

	public TimeInvariant invariant() {
		return invariant;
	}

	public void setInvariant(TimeInvariant invariant) {
		Require.that(invariant != null, "A timed place must have a non-null invariant");
		this.invariant = invariant;
		fireInvariantChanged();
	}

	public void addToPreset(TimedOutputArc arc) {
		Require.that(arc != null, "Cannot add null to preset");
		preset.add(arc);
	}

	public void addToPreset(TransportArc arc) {
		Require.that(arc != null, "Cannot add null to preset");
		presetTransportArcs.add(arc);
	}

	public void addToPostset(TimedInputArc arc) {
		Require.that(arc != null, "Cannot add null to postset");
		postset.add(arc);
	}

	public void addToPostset(TransportArc arc) {
		Require.that(arc != null, "Cannot add null to postset");
		postsetTransportArcs.add(arc);
	}

	public void addInhibitorArc(TimedInhibitorArc arc){
		Require.that(arc != null, "arc cannot be null");
		inhibitorArcs.add(arc);
	}

	public void removeInhibitorArc(TimedInhibitorArc arc){
		Require.that(arc != null, "arc cannot be null");
		inhibitorArcs.remove(arc);
	}

	public boolean hasTokenSatisfyingInterval(TimeInterval interval) {
		List<TimedToken> tokens = currentMarking.getTokensFor(this);

		for (TimedToken t : tokens) {
			if (interval.isIncluded(t.age()))
				return true;
		}

		return false;
	}

	public List<TimedToken> tokensSatisfyingInterval(TimeInterval interval) {
		List<TimedToken> tokens = currentMarking.getTokensFor(this);
		ArrayList<TimedToken> toReturn = new ArrayList<TimedToken>();
		for (TimedToken t : tokens) {
			if (interval.isIncluded(t.age()))
				toReturn.add(t);
		}

		return toReturn;
	}

	public List<TimedToken> tokens() {
		return currentMarking.getTokensFor(this);
	}

	public int numberOfTokens() {
		return currentMarking.getTokensFor(this).size();
	}


	public void removeFromPostset(TimedInputArc arc) {
		postset.remove(arc);
	}

	public void removeFromPostset(TransportArc arc) {
		postsetTransportArcs.remove(arc);
	}

	public void removeFromPreset(TransportArc arc) {
		presetTransportArcs.remove(arc);
	}

	public void removeFromPreset(TimedOutputArc arc) {
		preset.remove(arc);
	}

	public void delete() {
		unshare();
		model().remove(this);
	}

	public void addToken(TimedToken timedToken) {
		Require.that(timedToken != null, "timedToken cannot be null");
		currentMarking.add(timedToken);
		fireMarkingChanged();
	}
	
	public void addTokens(Iterable<TimedToken> tokens) {
		Require.that(tokens != null, "tokens cannot be null");
		
		for(TimedToken token : tokens){
			currentMarking.add(token); // avoid firing marking changed on every add
		}
		fireMarkingChanged();
	}

	public void removeToken(TimedToken timedToken) {
		Require.that(timedToken != null, "timedToken cannot be null");
		currentMarking.remove(timedToken);
		fireMarkingChanged();
	}

	public void removeToken() {
		if (numberOfTokens() > 0) {
			currentMarking.removeArbitraryTokenFrom(this);
			fireMarkingChanged();
		}
	}

	public TimedPlace copy() {
		TimedPlace p = new TimedPlace(this.name);

		p.invariant = this.invariant.copy();

		return p;
	}

	@Override
	public String toString() {
		if (model() != null)
			return model().getName() + "." + name;
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
		if (!(obj instanceof TimedPlace))
			return false;
		TimedPlace other = (TimedPlace) obj;
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
}
