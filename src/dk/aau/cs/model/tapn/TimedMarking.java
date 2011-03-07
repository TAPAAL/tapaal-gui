package dk.aau.cs.model.tapn;

import java.math.BigDecimal;
import java.util.List;

import dk.aau.cs.model.tapn.simulation.FiringMode;

public interface TimedMarking {
	public abstract void add(TimedToken token);
	public abstract void remove(TimedToken token);

//	public abstract void removePlaceFromMarking(TimedPlace place);
//	public abstract void removePlaceFromMarking(SharedPlace place);
	public abstract void removePlaceFromMarking(TimedPlace place);
	
	public abstract List<TimedToken> getTokensFor(TimedPlace place);
//	public abstract List<TimedToken> getTokensFor(TimedPlace place);
//	public abstract List<TimedToken> getTokensFor(SharedPlace place);
	
	public abstract TimedMarking clone();

	public abstract boolean isDelayPossible(BigDecimal delay);
	public abstract TimedMarking delay(BigDecimal amount);

	public abstract TimedMarking fireTransition(TimedTransition transition, List<TimedToken> tokensToConsume);
	public abstract TimedMarking fireTransition(TimedTransition transition, FiringMode firingMode);

	public abstract int size();
}