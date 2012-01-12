package dk.aau.cs.util;

import dk.aau.cs.model.tapn.TimeInterval;

public class IntervalOperations {
	
	private enum BoundToCheck {
		lower, upper
	}
	
	public static boolean isEmpty(TimeInterval i){
		if(i == null){
			return true;
		}
		
		if((!i.IsLowerBoundNonStrict() || !i.IsUpperBoundNonStrict()) && i.lowerBound().value() == i.upperBound().value()){
			return true;
		} else {
			return false;
		}
	}
	
	public static TimeInterval union(TimeInterval i1, TimeInterval i2){
		if(i1 == null){
			return i2;
		} else if (i2 == null){
			return i1;
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
		
		if(upper.upperBound().value() > lower.lowerBound().value() || upper.upperBound().value() == lower.lowerBound().value() && lower.IsLowerBoundNonStrict() && upper.IsUpperBoundNonStrict() || (upper.upperBound().value() < 0)){
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
			int b1 = i1.lowerBound().value();
			int b2 = i2.lowerBound().value();
			
			if(b1<b2 || b1 == b2 && i1.IsLowerBoundNonStrict() && !i2.IsLowerBoundNonStrict()){
				return i1;
			} else {
				return i2;
			}
		case upper:
			b1 = i1.upperBound().value();
			b2 = i2.upperBound().value();
			if(b1 >= 0 && b2 >= 0){
				if(b1<b2 || b1 == b2 && !i1.IsUpperBoundNonStrict() && i2.IsUpperBoundNonStrict()){
					return i1;
				} else {
					return i2;
				}
			} else if (b1 < 0){
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
}
