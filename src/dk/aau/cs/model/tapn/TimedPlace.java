package dk.aau.cs.model.tapn;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import dk.aau.cs.model.tapn.event.TimedPlaceEvent;
import dk.aau.cs.model.tapn.event.TimedPlaceListener;
import dk.aau.cs.util.Require;
import dk.aau.cs.util.Tuple;

public abstract class TimedPlace {

    protected static final Pattern namePattern = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");
    protected final List<TimedPlaceListener> listeners = new ArrayList<TimedPlaceListener>();
    protected Tuple<PlaceType, Integer> extrapolation = new Tuple<PlaceType, Integer>(PlaceType.Dead, -2);
    protected String name;
    protected TimeInvariant invariant;
    protected TimedMarking currentMarking;

    private final List<TimedOutputArc> postset = new ArrayList<TimedOutputArc>();
    private final List<TimedInputArc> preset = new ArrayList<TimedInputArc>();
    private final List<TransportArc> transportArcs = new ArrayList<TransportArc>();
    private final List<TimedInhibitorArc> inhibitorArcs = new ArrayList<TimedInhibitorArc>();

    public enum PlaceType{
		Standard, Invariant, Dead
	}

	public abstract boolean isShared();

    public String name() {
        return name;
    }

    public void setName(String newName) {
		Require.that(newName != null && !newName.isEmpty(), "A timed place must have a name");
		Require.that(isValid(newName) && !newName.toLowerCase().equals("true") && !newName.toLowerCase().equals("false"), "The specified name must conform to the pattern [a-zA-Z_][a-zA-Z0-9_]*");
        List<TimedToken> tokens = null;

		if (currentMarking != null) {
            if (currentMarking.getTokensFor(this).size() != 0) {
                tokens = tokens();
                currentMarking.removePlaceFromMarking(this);
            }
            this.name = newName;
            if (tokens != null) addTokens(tokens);
            fireNameChanged();
        } else {
            this.name = newName;
            fireNameChanged();
        }
    }

    public TimeInvariant invariant(){
        return invariant;
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

    public void addTokens(int numberOfTokensToAdd) {
        for (int i = 0; i < numberOfTokensToAdd; i++) {
            addToken(new TimedToken(this, BigDecimal.ZERO));
        }
    }

    public void removeTokens(int numberOfTokensToRemove) {
        for (int i = 0; i < numberOfTokensToRemove; i++) {
            removeToken();
        }
    }

    public void removeToken() {
        if (numberOfTokens() > 0) {
            currentMarking.remove(tokens().get(0));
            fireMarkingChanged();
        }
    }
	
	public abstract Tuple<PlaceType, Integer> extrapolate();
	
	public abstract TimedPlace copy();
	
	/**
	 * Returns the tokens in the place, sorted decreasing
	 */
	public List<TimedToken> sortedTokens(){
		List<TimedToken> copy = new ArrayList<TimedToken>(tokens());
		copy.sort((o1, o2) -> {
			//Order reverse
			return o1.age().compareTo(o2.age()) * -1;
		});
		
		return copy;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj)	return true;
		if(!(obj instanceof TimedPlace))	return false;
		TimedPlace other = (TimedPlace) obj;
		return name() == other.name();
	}


    protected void fireMarkingChanged() {
        for(TimedPlaceListener listener : listeners){
            listener.markingChanged(new TimedPlaceEvent(this));
        }
    }

    protected void fireNameChanged() {
        for(TimedPlaceListener listener : listeners){
            listener.nameChanged(new TimedPlaceEvent(this));
        }
    }

    protected void fireInvariantChanged() {
        for(TimedPlaceListener listener : listeners){
            listener.invariantChanged(new TimedPlaceEvent(this));
        }
    }

    protected boolean isValid(String newName) {
        return namePattern.matcher(newName).matches();
    }

    public void setInvariant(TimeInvariant invariant) {
        Require.that(invariant != null, "invariant must not be null");
        this.invariant = invariant;
        fireInvariantChanged();
    }

    public void addTimedPlaceListener(TimedPlaceListener listener) {
        Require.that(listener != null, "Listener cannot be null");
        listeners.add(listener);
    }

    public void removeTimedPlaceListener(TimedPlaceListener listener) {
        Require.that(listener != null, "Listener cannot be null");
        listeners.remove(listener);
    }

    public void setCurrentMarking(TimedMarking marking) {
        Require.that(marking != null, "marking cannot be null");
        currentMarking = marking;
        fireMarkingChanged();
    }

    public boolean isOrphan() {
        return presetSize() == 0 && postsetSize() == 0;
    }

    public void addInputArc(TimedInputArc arc) {
        Require.that(arc != null, "Cannot add null to preset");
        preset.add(arc);
    }

    public void addOutputArc(TimedOutputArc arc) {
        Require.that(arc != null, "Cannot add null to postset");
        postset.add(arc);
    }

    public void removeInputArc(TimedInputArc arc) {
        preset.remove(arc);
    }

    public void removeOutputArc(TimedOutputArc arc) {
        postset.remove(arc);
    }

    public void addTransportArc(TransportArc arc) {
        Require.that(arc != null, "Cannot add null to preset");
        transportArcs.add(arc);
    }

    public void removeTransportArc(TransportArc arc) {
        transportArcs.remove(arc);
    }

    public void addInhibitorArc(TimedInhibitorArc arc) {
        inhibitorArcs.add(arc);
    }

    public void removeInhibitorArc(TimedInhibitorArc arc) {
        inhibitorArcs.remove(arc);
    }

    public int presetSize() {
        return preset.size() + transportArcs.size();
    }

    public int postsetSize() {
        return postset.size() + transportArcs.size() + inhibitorArcs.size();
    }
}