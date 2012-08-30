package dk.aau.cs.util;

import java.math.BigDecimal;
import java.math.MathContext;

import pipe.gui.Pipe;

import dk.aau.cs.model.tapn.Bound;
import dk.aau.cs.model.tapn.RatBound;
import dk.aau.cs.model.tapn.TimeInterval;

public class IntervalOperations {
	
	private enum BoundToCheck {
		lower, upper
	}
	
	public static RatBound getRatBound(Bound b){
		if (b instanceof RatBound){
			return (RatBound)b;
		} else {
			return new RatBound(new BigDecimal(b.value(), new MathContext(Pipe.AGE_PRECISION)));
		}
	}
	
	public static TimeInterval union(TimeInterval i1, TimeInterval i2){
		if(i1 == null){
			return i2;
		} else if (i2 == null){
			return i1;
		}
		
		//The intervals cannot be unioned to a single interval
		if(intersection(i1, i2) == null){
			return null;
		}
		
		TimeInterval lower = min(i1, i2, BoundToCheck.lower);
		TimeInterval upper = max(i1, i2, BoundToCheck.upper);
		
		return new TimeInterval(lower.IsLowerBoundNonStrict(), lower.lowerBound(), upper.upperBound(), upper.IsUpperBoundNonStrict()); 
	}
	
	
	public static TimeInterval intersection(TimeInterval i1, TimeInterval i2){
		if(i1 == null || i2 == null){
			return null;
		}
		
		TimeInterval upper, lower;
		
		upper = min(i1, i2, BoundToCheck.upper);
		lower = max(i1, i2, BoundToCheck.lower);
		BigDecimal iUp = getRatBound(upper.upperBound()).getBound();
		BigDecimal iLow = getRatBound(lower.lowerBound()).getBound();
		
		if(iUp.compareTo(iLow) > 0 || iUp.compareTo(iLow) == 0 && lower.IsLowerBoundNonStrict() && upper.IsUpperBoundNonStrict() || (upper.upperBound().value() < 0)){
			return new TimeInterval(lower.IsLowerBoundNonStrict(), lower.lowerBound(), upper.upperBound(), upper.IsUpperBoundNonStrict());
		} else {
			return null;
		}
	}
	
	public static TimeInterval min(TimeInterval i1, TimeInterval i2, BoundToCheck b){
		if(i1 == null){
			return i2;
		} else if (i2 == null){
			return i1;
		}
		
		switch (b) {
		case lower:
			BigDecimal b1 = getRatBound(i1.lowerBound()).getBound();
			BigDecimal b2 = getRatBound(i2.lowerBound()).getBound();
			
			if(b1.compareTo(b2) < 0 || b1.compareTo(b2) == 0 && i1.IsLowerBoundNonStrict() && !i2.IsLowerBoundNonStrict()){
				return i1;
			} else {
				return i2;
			}
		case upper:
			b1 = getRatBound(i1.upperBound()).getBound();
			b2 = getRatBound(i2.upperBound()).getBound();
			if(b1.compareTo(BigDecimal.ZERO) >= 0 && b2.compareTo(BigDecimal.ZERO) >= 0){
				if(b1.compareTo(b2) < 0 || b1.compareTo(b2) == 0 && !i1.IsUpperBoundNonStrict() && i2.IsUpperBoundNonStrict()){
					return i1;
				} else {
					return i2;
				}
			} else if (b1.compareTo(BigDecimal.ZERO) < 0){
				return i2;
			} else {
				return i1;
			}
		default:
			return null;
		}
	}
	
	public static TimeInterval max(TimeInterval i1, TimeInterval i2, BoundToCheck b){
		if(i1 == null){
			return i2;
		} else if (i2 == null){
			return i1;
		}
		
		TimeInterval min = min(i1, i2, b);
		if(min.equals(i1)){
			return i2;
		} else {
			return i1;
		}
	}
	
	public static Bound getMaxNoInfBound(TimeInterval interval){
		if(interval.upperBound() instanceof Bound.InfBound){
			return interval.lowerBound();
		} else {
			return interval.upperBound();
		}
	}
}
