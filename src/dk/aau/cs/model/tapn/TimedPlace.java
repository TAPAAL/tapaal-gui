package dk.aau.cs.model.tapn;

import java.util.List;

import dk.aau.cs.model.tapn.event.TimedPlaceListener;

public interface TimedPlace {
	void addTimedPlaceListener(TimedPlaceListener listener);
	void removeTimedPlaceListener(TimedPlaceListener listener);

	boolean isShared();

	String name();
	void setName(String newName);

	TimeInvariant invariant();
	void setInvariant(TimeInvariant invariant);

	List<TimedToken> tokens();
	int numberOfTokens();

	void setCurrentMarking(TimedMarking marking);
	
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