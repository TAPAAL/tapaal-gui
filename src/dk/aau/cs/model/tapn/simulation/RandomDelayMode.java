package dk.aau.cs.model.tapn.simulation;

import java.math.BigDecimal;
import java.util.Random;

import dk.aau.cs.model.tapn.Bound;
import dk.aau.cs.model.tapn.IntBound;
import dk.aau.cs.model.tapn.TimeInterval;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.util.IntervalOperations;

public class RandomDelayMode implements DelayMode{
	
	final static int numberOfDecimals = 5;

	@Override
	public BigDecimal GetDelay(TimedTransition transition,
			TimeInterval dInterval, BigDecimal delayGranularity) {
		
		BigDecimal lower = IntervalOperations.getRatBound(dInterval.lowerBound()).getBound();
		BigDecimal upper = IntervalOperations.getRatBound(dInterval.upperBound()).getBound();
		
		Random r = new Random();
		double number = r.nextDouble();
		BigDecimal result;
		
		if(dInterval.upperBound() instanceof Bound.InfBound){
			result = null;
		} else {
			//This is safe as the difference between the bounds is always ints - and constants in the model is ints as well
			int diff = upper.subtract(lower).intValue();
			result = randomBigDecimal(diff, dInterval);
			result = result.add(lower);
		}
		
		return result;
	}
	
	private BigDecimal randomBigDecimal(int maxValue, TimeInterval dInterval){
		TimeInterval range = new TimeInterval(dInterval.IsLowerBoundNonStrict(), new IntBound(0), new IntBound(maxValue), dInterval.IsUpperBoundNonStrict());
		Random r = new Random();
		//We don't want to generate numbers such as X,99 if maxValue is X
		int maxToGenerate = maxValue -1;
		
		BigDecimal result = null;
		
		do{
			int integerPart = 0;
			if(maxToGenerate > 0){
				integerPart = r.nextInt(maxToGenerate);
			}
			//The fractional part is only 5 digits long in the simulator - 
			//if the number 100000 is generated and the integerpart is maxToGenerate
			//if 100000 is generated otherwise the number is considered invalid and a new is generated
			int fractionalPart = r.nextInt(10 * numberOfDecimals + 1);
			
			if(fractionalPart == 10 * numberOfDecimals){
				if(integerPart == maxToGenerate){
					integerPart = maxValue;
					fractionalPart = 0;
				} else {
					//The value is invalid: retry
					continue;
				}
			}
			
			String integerPartAsString = Integer.toString(integerPart);
			String fractionalPartAsString = String.format("%0" + numberOfDecimals + "d", fractionalPart);
			
			String resultAsString = integerPartAsString + "." + fractionalPartAsString;
			result = new BigDecimal(resultAsString);
		} while (!range.isIncluded(result));

		return result;
	}
	

}
