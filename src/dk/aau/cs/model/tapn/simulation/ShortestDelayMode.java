package dk.aau.cs.model.tapn.simulation;

import java.math.BigDecimal;
import java.math.MathContext;

import pipe.gui.CreateGui;
import pipe.gui.Pipe;

import dk.aau.cs.model.tapn.TimeInterval;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.util.IntervalOperations;

public class ShortestDelayMode implements DelayMode{

	@Override
	public BigDecimal GetDelay(TimedTransition transition,
			TimeInterval dInterval, BigDecimal delayGranularity) {
		
		if(dInterval.IsLowerBoundNonStrict()){
			return IntervalOperations.getRatBound(dInterval.lowerBound()).getBound();
		} else {
			return IntervalOperations.getRatBound(dInterval.lowerBound()).getBound().add(delayGranularity, new MathContext(Pipe.AGE_PRECISION));
		}
	}
	
	public String toString() {
		return name();
	}
	
	public static String name(){
		return "Shortest delay";
	}

}
