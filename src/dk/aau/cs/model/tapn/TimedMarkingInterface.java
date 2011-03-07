package dk.aau.cs.model.tapn;

import java.math.BigDecimal;
import java.util.List;

import dk.aau.cs.model.tapn.simulation.FiringMode;

public interface TimedMarkingInterface {
	public abstract void add(TimedToken token);
	public abstract void remove(TimedToken token);

//	public abstract void removePlaceFromMarking(TimedPlace place);
//	public abstract void removePlaceFromMarking(SharedPlace place);
	public abstract void removePlaceFromMarking(TimedPlaceInterface place);
	
	public abstract List<TimedToken> getTokensFor(TimedPlaceInterface place);
//	public abstract List<TimedToken> getTokensFor(TimedPlace place);
//	public abstract List<TimedToken> getTokensFor(SharedPlace place);
	
	public abstract TimedMarkingInterface clone();

	public abstract boolean isDelayPossible(BigDecimal delay);
	public abstract TimedMarkingInterface delay(BigDecimal amount);

	public abstract TimedMarkingInterface fireTransition(TimedTransition transition, List<TimedToken> tokensToConsume);
	public abstract TimedMarkingInterface fireTransition(TimedTransition transition, FiringMode firingMode);

	public abstract int size();
}