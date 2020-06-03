package dk.aau.cs.model.tapn;

import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.model.tapn.event.TimedPlaceListener;
import dk.aau.cs.util.Require;
import dk.aau.cs.util.Tuple;

public abstract class TimedPlace {
    private SharedPlace sharedPlace;
    private List<TimedOutputArc> postset = new ArrayList<TimedOutputArc>();
    private List<TimedInputArc> preset = new ArrayList<TimedInputArc>();
    private List<TransportArc> transportArcs = new ArrayList<TransportArc>();
    private List<TimedInhibitorArc> inhibitorArcs = new ArrayList<TimedInhibitorArc>();

    public enum PlaceType{
		Standard, Invariant, Dead
	}
	
	public abstract void addTimedPlaceListener(TimedPlaceListener listener);
	public abstract void removeTimedPlaceListener(TimedPlaceListener listener);

	public abstract boolean isShared();

	public abstract String name();
	public abstract void setName(String newName);

	public abstract TimeInvariant invariant();
	public abstract void setInvariant(TimeInvariant invariant);

	public abstract List<TimedToken> tokens();
	public abstract int numberOfTokens();

	public abstract void setCurrentMarking(TimedMarking marking);
	
	public abstract void addToken(TimedToken timedToken);
	public abstract void addTokens(Iterable<TimedToken> tokens);

	public abstract void removeToken(TimedToken timedToken);
	public abstract void removeToken();
	
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
        return preset.size() + transportArcs.size() + inhibitorArcs.size();
    }

    public int postsetSize() {
        return postset.size() + transportArcs.size() + inhibitorArcs.size();
    }

//	public abstract void addInhibitorArc(TimedInhibitorArc arc);
//	public abstract void addToPreset(TransportArc arc);
//	public abstract void addToPreset(TimedOutputArc arc);
//	public abstract void addToPostset(TransportArc arc);
//	public abstract void addToPostset(TimedInputArc arc);
//
//	public abstract void removeFromPostset(TimedInputArc arc);
//	public abstract void removeFromPostset(TransportArc arc);
//	public abstract void removeFromPreset(TransportArc arc);
//	public abstract void removeFromPreset(TimedOutputArc arc);
//	public abstract void removeInhibitorArc(TimedInhibitorArc arc);

}