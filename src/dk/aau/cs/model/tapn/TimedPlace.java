package dk.aau.cs.model.tapn;

import java.util.List;

import dk.aau.cs.model.tapn.event.TimedPlaceListener;

public interface TimedPlace {
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
	
	public abstract TimedPlace copy();
	
	
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