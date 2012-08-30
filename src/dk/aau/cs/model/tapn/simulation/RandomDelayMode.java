package dk.aau.cs.model.tapn.simulation;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Random;

import dk.aau.cs.model.tapn.Bound;
import dk.aau.cs.model.tapn.IntBound;
import dk.aau.cs.model.tapn.TimeInterval;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.util.IntervalOperations;

public class RandomDelayMode implements DelayMode{
	
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
			TimeInterval range = new TimeInterval(dInterval.IsLowerBoundNonStrict(), new IntBound(0), new IntBound(upper.subtract(lower).intValue()), dInterval.IsUpperBoundNonStrict());
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
		int maxValue = range.upperBound().value();
		Random r = new Random();
		
		BigDecimal result = null;
		
		boolean validValue;
		
		do{
			validValue=true;
			int integerPart = r.nextInt(maxValue);
			//The fractional part is only 5 digits long in the simulator - 
			//if the number 100000 is generated and the integerpart is maxToGenerate
			//if 100000 is generated otherwise the number is considered invalid and a new is generated
			int fractionalPart = r.nextInt((int)Math.pow(10, numberOfDecimals) + 1);
			if(fractionalPart == (int)Math.pow(10, numberOfDecimals)){
				if(integerPart == maxValue -1){
					integerPart = maxValue;
					fractionalPart = 0;
				} else {
					validValue = false;
					continue;
				}
			}
			
			String integerPartAsString = Integer.toString(integerPart);
			String fractionalPartAsString = numberOfDecimals == 0 ? "" : String.format("%0" + numberOfDecimals + "d", fractionalPart);
			
			String resultAsString = integerPartAsString + (numberOfDecimals == 0 ? "" : "." + fractionalPartAsString);
			result = new BigDecimal(resultAsString);
		} while (!validValue || !range.isIncluded(result));

		return result;
	}
	
	private BigDecimal exponentialDistribution(TimeInterval range, TimedTransition transition){
		BigDecimal result;
		
		Random r = new Random();
		
		TimedArcPetriNetNetwork network = transition.model().parentNetwork();
		int biggestConstant = network.biggestConstantInActiveNet();
		int biggestConstantEnabled = network.biggestContantInActiveNetEnabledTransitions();
		double factor = 1d/(biggestConstantEnabled * 0.6d + biggestConstant * 0.4d);
		
		do{
			double uniformDistribution = r.nextDouble();
			
			double number = Math.log(1 - uniformDistribution)/(factor * -1);
			result = new BigDecimal(number);
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