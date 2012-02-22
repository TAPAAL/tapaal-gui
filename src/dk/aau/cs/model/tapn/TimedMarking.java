package dk.aau.cs.model.tapn;

import java.math.BigDecimal;
import java.util.List;

import dk.aau.cs.model.tapn.simulation.FiringMode;

public interface TimedMarking {
	void add(TimedToken token);
	void remove(TimedToken token);

//	public abstract void removePlaceFromMarking(TimedPlace place);
//	public abstract void removePlaceFromMarking(SharedPlace place);
	void removePlaceFromMarking(TimedPlace place);
	
	List<TimedToken> getTokensFor(TimedPlace place);
//	public abstract List<TimedToken> getTokensFor(TimedPlace place);
//	public abstract List<TimedToken> getTokensFor(SharedPlace place);
	
	TimedMarking clone();

	boolean isDelayPossible(BigDecimal delay);
	TimedMarking delay(BigDecimal amount);

	TimedMarking fireTransition(TimedTransition transition, List<TimedToken> tokensToConsume);
	TimedMarking fireTransition(TimedTransition transition, FiringMode firingMode);

	int size();
}