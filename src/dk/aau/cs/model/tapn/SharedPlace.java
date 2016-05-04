package dk.aau.cs.model.tapn;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import pipe.dataLayer.Template;
import pipe.gui.CreateGui;
import dk.aau.cs.model.tapn.event.TimedPlaceEvent;
import dk.aau.cs.model.tapn.event.TimedPlaceListener;
import dk.aau.cs.util.Require;
import dk.aau.cs.util.Tuple;

public class SharedPlace extends TimedPlace{
	private static final Pattern namePattern = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");
	
	private String name;
	private TimeInvariant invariant;
	
	private TimedArcPetriNetNetwork network;
	private TimedMarking currentMarking;
	private Tuple<PlaceType, Integer> extrapolation = new Tuple<TimedPlace.PlaceType, Integer>(PlaceType.Dead, -2);
	
	private List<TimedPlaceListener> listeners = new ArrayList<TimedPlaceListener>();

	public SharedPlace(String name){
		this(name, TimeInvariant.LESS_THAN_INFINITY);
	}
	
	public SharedPlace(String name, TimeInvariant invariant){
		setName(name);
		setInvariant(invariant);
	}
	
	public String name() {
		return name;
	}
	
	public void setName(String newName) {
		Require.that(newName != null && !newName.isEmpty(), "A timed transition must have a name");
		Require.that(isValid(newName) && !newName.toLowerCase().equals("true") && !newName.toLowerCase().equals("false"), "The specified name must conform to the pattern [a-zA-Z_][a-zA-Z0-9_]*");
		name = newName;
		fireNameChanged();
	}
	
	private boolean isValid(String newName) {
		return namePattern.matcher(newName).matches();
	}

	public TimeInvariant invariant(){
		return invariant;
	}
	
	public void setInvariant(TimeInvariant invariant) {
		Require.that(invariant != null, "invariant must not be null");
		this.invariant = invariant;
		fireInvariantChanged();
	}

	public void setNetwork(TimedArcPetriNetNetwork network) {
		this.network = network;		
	}
	
	public TimedArcPetriNetNetwork network(){
		return network;
	}
	
	public void addTimedPlaceListener(TimedPlaceListener listener) {
		Require.that(listener != null, "Listener cannot be null");
		listeners.add(listener);
	}

	public void removeTimedPlaceListener(TimedPlaceListener listener) {
		Require.that(listener != null, "Listener cannot be null");
		listeners.remove(listener);
	}

	public TimedPlace copy() {
		return new SharedPlace(this.name(), this.invariant().copy());
	}

	public boolean isShared() {
		return true;
	}

	public void setCurrentMarking(TimedMarking marking) {
		Require.that(marking != null, "marking cannot be null");
		currentMarking = marking;
		fireMarkingChanged();
	}
	
	public void addToken(TimedToken timedToken) {
		Require.that(timedToken != null, "timedToken cannot be null");
		Require.that(timedToken.place().equals(this), "token is located in a different place");
		
		currentMarking.add(timedToken);
		fireMarkingChanged();
	}
	
	public void addTokens(Iterable<TimedToken> tokens) {
		Require.that(tokens != null, "tokens cannot be null"); // TODO: maybe check that tokens are in this place?
		
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

	public List<TimedToken> tokens() {
		return currentMarking.getTokensFor(this);
	}	

	public int numberOfTokens() {
		return tokens().size();
	}
	
	private void fireMarkingChanged() {
		for(TimedPlaceListener listener : listeners){
			listener.markingChanged(new TimedPlaceEvent(this));
		}
	}
	
	private void fireNameChanged() {
		for(TimedPlaceListener listener : listeners){
			listener.nameChanged(new TimedPlaceEvent(this));
		}
	}

	private void fireInvariantChanged() {
		for(TimedPlaceListener listener : listeners){
			listener.invariantChanged(new TimedPlaceEvent(this));
		}
	}
	
	public ArrayList<String> getComponentsUsingThisPlace(){
		ArrayList<String> components = new ArrayList<String>();
		for(Template t : CreateGui.getCurrentTab().allTemplates()){
			TimedPlace tp = t.model().getPlaceByName(SharedPlace.this.name);
			if(tp != null){
				components.add(t.model().name());
			}
		}
		return components;
	}
	
	@Override
	public String toString() {
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof SharedPlace))
			return false;
		SharedPlace other = (SharedPlace) obj;
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
		
		for(Template t : CreateGui.getCurrentTab().activeTemplates()){
			TimedPlace tp = t.model().getPlaceByName(SharedPlace.this.name);
			if(tp != null){
				cmax = Math.max(cmax, tp.extrapolate().value2());
				if(tp.extrapolate().value1() == PlaceType.Invariant || (type == PlaceType.Dead && tp.extrapolate().value1() == PlaceType.Standard)){
					type = tp.extrapolate().value1();
				}
				extrapolation = new Tuple<TimedPlace.PlaceType, Integer>(type, cmax);
			}
		}
		
		extrapolation = new Tuple<TimedPlace.PlaceType, Integer>(PlaceType.Dead, -2);
		
		return new Tuple<TimedPlace.PlaceType, Integer>(type, cmax);
	}
}
