package dk.aau.cs.model.tapn.simulation;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Random;

import dk.aau.cs.model.tapn.Bound;
import dk.aau.cs.model.tapn.IntBound;
import dk.aau.cs.model.tapn.RatBound;
import dk.aau.cs.model.tapn.TimeInterval;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.util.IntervalOperations;

public class RandomDelayMode implements DelayMode{
	
	private static RandomDelayMode instance;
	
	public static RandomDelayMode getInstance(){
		if(instance == null){
			instance = new RandomDelayMode();
		}
		return instance;
	}
	
	private RandomDelayMode(){};
	
	int numberOfDecimals = -1;

	@Override
	public BigDecimal GetDelay(TimedTransition transition,
			TimeInterval dInterval, BigDecimal delayGranularity) {
		
		setNumberOfDecimalsAccordingToGranularity(delayGranularity);
		
		BigDecimal lower = IntervalOperations.getRatBound(dInterval.lowerBound()).getBound();
		BigDecimal upper = IntervalOperations.getRatBound(dInterval.upperBound()).getBound();
		
		BigDecimal result;
		
		if(dInterval.upperBound() instanceof Bound.InfBound){
			TimeInterval range = new TimeInterval(dInterval.IsLowerBoundNonStrict(), new IntBound(0), Bound.Infinity, dInterval.IsUpperBoundNonStrict());
			result = exponentialDistribution(range, transition);
		} else {
			//This is safe as the difference between the bounds is always ints - and constants in the model is ints as well
			TimeInterval range = new TimeInterval(dInterval.IsLowerBoundNonStrict(), new IntBound(0), new RatBound(upper.subtract(lower)), dInterval.IsUpperBoundNonStrict());
			result = randomBigDecimal(range);
		}
		
		result = result.add(lower);
		return result;
	}
	
	private void setNumberOfDecimalsAccordingToGranularity(BigDecimal granularity){
		granularity = granularity.stripTrailingZeros();
		int scale = granularity.scale();
		if(scale >= 0){
			numberOfDecimals = scale;
		} else {
			throw new IllegalArgumentException("The granularity supplied has a negative scale");
		}
	}
	
	private BigDecimal randomBigDecimal(TimeInterval range){
		BigDecimal maxValue = ((RatBound)range.upperBound()).getBound();
		if(maxValue.equals(BigDecimal.ZERO)){
			return BigDecimal.ZERO;
		}
		
		BigDecimal result;
		do{
			BigDecimal randFromDouble = new BigDecimal(Math.random());
			result = randFromDouble.multiply(maxValue);
		} while (!range.isIncluded(result));
		
		return result.setScale(numberOfDecimals, RoundingMode.DOWN);
	}
	
	private BigDecimal exponentialDistribution(TimeInterval range, TimedTransition transition){
		BigDecimal result;
		
		Random r = new Random();
		
		TimedArcPetriNetNetwork network = transition.model().parentNetwork();
		double biggestConstant = network.biggestConstantInActiveNet();
		double biggestConstantEnabled = network.biggestContantInActiveNetEnabledTransitions();
		double factor = 1d/(biggestConstantEnabled * 0.6d + biggestConstant * 0.4d);
		
		//If both the biggest constant enabled and the biggest constant in the net are 0, handle the division by zero. 
		if(factor == Double.POSITIVE_INFINITY){
			factor = 1/0.6;
		}
		
		do{
			double uniformDistribution = r.nextDouble();
			
			double number = Math.log(1 - uniformDistribution)/(factor * -1);
			result = new BigDecimal(number);
			result = result.setScale(numberOfDecimals, RoundingMode.DOWN);
		//If the lower bound of the interval is noninclusive and the number generated is 0 - a new number must be found
		} while (!range.isIncluded(result));
		return result;
	}
	
	public String toString() {
		return name();
	}
	
	public static String name(){
		return "Random delay";
	}
}