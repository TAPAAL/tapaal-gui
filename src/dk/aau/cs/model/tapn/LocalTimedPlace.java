package dk.aau.cs.model.tapn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import dk.aau.cs.model.tapn.TimedPlace.PlaceType;
import dk.aau.cs.model.tapn.event.TimedPlaceEvent;
import dk.aau.cs.model.tapn.event.TimedPlaceListener;
import dk.aau.cs.util.Require;
import dk.aau.cs.util.Tuple;

public class LocalTimedPlace  extends TimedPlace {
	private static final Pattern namePattern = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");

	private String name;
	private TimeInvariant invariant;

	private TimedArcPetriNet model;
	private TimedMarking currentMarking;
	private List<TimedPlaceListener> listeners = new ArrayList<TimedPlaceListener>();
	
	private Tuple<PlaceType, Integer> extrapolation = new Tuple<TimedPlace.PlaceType, Integer>(PlaceType.Dead, -2);

	public LocalTimedPlace(String name) {
		this(name, TimeInvariant.LESS_THAN_INFINITY);
	}

	public LocalTimedPlace(String name, TimeInvariant invariant) {
		setName(name);
		setInvariant(invariant);
	}
	
	public TimedArcPetriNet model() {
		return model;
	}

	public void setModel(TimedArcPetriNet model) {
		this.model = model;
	}

	public void addTimedPlaceListener(TimedPlaceListener listener){
		Require.that(listener != null, "Listener cannot be null");
		listeners.add(listener);
	}

	public void removeTimedPlaceListener(TimedPlaceListener listener){
		Require.that(listener != null, "Listener cannot be null");
		listeners.remove(listener);
	}

	public boolean isShared() {
		return false;
	}

	public void setCurrentMarking(TimedMarking marking) {
		Require.that(marking != null, "marking cannot be null");
		currentMarking = marking;
		fireMarkingChanged();
	}

	public String name() {
		return name;
	}

	public void setName(String newName) {
		Require.that(newName != null && !newName.isEmpty(), "A timed place must have a name");
		Require.that(isValid(newName) && !newName.toLowerCase().equals("true") && !newName.toLowerCase().equals("false"), "The specified name must conform to the pattern [a-zA-Z_][a-zA-Z0-9_]*");
		this.name = newName;
		fireNameChanged();
	}

	private void fireNameChanged() {
		for(TimedPlaceListener listener : listeners){
			listener.nameChanged(new TimedPlaceEvent(this));
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

	public List<TimedToken> tokens() {
		return currentMarking.getTokensFor(this);
	}

	public int numberOfTokens() {
		return tokens().size();
	}

	public void addToken(TimedToken timedToken) {
		Require.that(timedToken != null, "timedToken cannot be null");
		Require.that(timedToken.place().equals(this), "token is located in a different place");
		
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
			currentMarking.remove(tokens().get(0));
			fireMarkingChanged();
		}
	}

	public LocalTimedPlace copy() {
		LocalTimedPlace p = new LocalTimedPlace(name);

		p.invariant = invariant.copy();

		return p;
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
		if (!(obj instanceof LocalTimedPlace))
			return false;
		LocalTimedPlace other = (LocalTimedPlace) obj;
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
	
	public Tuple<PlaceType, Integer> extrapolate(){
		if(extrapolation.value2() > -2)	return extrapolation;
		
		PlaceType type = PlaceType.Dead;
		int cmax = -1;
		
		extrapolation = new Tuple<TimedPlace.PlaceType, Integer>(type, cmax);
		
		if(invariant != TimeInvariant.LESS_THAN_INFINITY){
			cmax = Math.max(cmax, invariant.upperBound().value());
		}
		
		// Invariant place
		if(cmax > -1){
			extrapolation = new Tuple<TimedPlace.PlaceType, Integer>(PlaceType.Dead, -2);
			return new Tuple<TimedPlace.PlaceType, Integer>(PlaceType.Invariant, cmax);
		}
		
		
		for(TimedInputArc arc : model.inputArcs()){
			if(!arc.source().equals(this))	continue;
			if(!arc.interval().upperBound().equals(Bound.Infinity)){
				cmax = Math.max(cmax, arc.interval().upperBound().value());
			}else if(arc.interval().lowerBound().value() > 0){
				cmax = Math.max(cmax, arc.interval().lowerBound().value());
			}
			
			if(type == PlaceType.Dead && arc.interval().upperBound().equals(Bound.Infinity)){
				type = PlaceType.Standard;
			}
			
			extrapolation = new Tuple<TimedPlace.PlaceType, Integer>(type, cmax);
		}
		
		for(TransportArc arc : model.transportArcs()){
			if(!arc.source().equals(this))	continue;
			if(!arc.interval().upperBound().equals(Bound.Infinity)){
				cmax = Math.max(cmax, arc.interval().upperBound().value());
			}else if(arc.interval().lowerBound().value() > 0){
				cmax = Math.max(cmax, arc.interval().lowerBound().value());
			}
			
			if(type == PlaceType.Dead && arc.interval().upperBound().equals(Bound.Infinity)){
				type = PlaceType.Standard;
			}
			
			Tuple<PlaceType, Integer> other = arc.source().extrapolate();
			if(other.value2() > cmax){
				cmax = other.value2();
			}
			
			if(type == PlaceType.Dead && arc.interval().upperBound().equals(Bound.Infinity)){
				type = PlaceType.Standard;
			}
			
			extrapolation = new Tuple<TimedPlace.PlaceType, Integer>(type, cmax);
		}
		
		extrapolation = new Tuple<TimedPlace.PlaceType, Integer>(PlaceType.Dead, -2);
		
		return new Tuple<TimedPlace.PlaceType, Integer>(type, cmax);
	}
}
