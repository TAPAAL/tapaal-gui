package dk.aau.cs.model.tapn;

import java.math.BigDecimal;

import dk.aau.cs.util.Require;

public class TimeInterval {
	private boolean isLowerIncluded;
	private Bound lower;
	private Bound upper;
	private boolean isUpperIncluded;
	
	public TimeInterval(boolean isLowerIncluded, Bound lower, Bound upper, boolean isUpperIncluded){
		Require.that(lower != null, "Lower bound cannot be null");
		Require.that(upper != null, "Upper bound cannot be null");
		
		this.isLowerIncluded = isLowerIncluded;
		this.lower = lower;
		this.upper = upper;
		this.isUpperIncluded = isUpperIncluded;
		Require.that(isValidInterval(), "The constructed interval " + toString() + " is empty.");
	}
	
	public TimeInterval(TimeInterval interval) {
		Require.that(interval != null, "Interval cannot be null");
		
		this.isLowerIncluded = interval.isLowerIncluded;
		this.isUpperIncluded = interval.isUpperIncluded;
		this.lower = interval.lower.copy();
		this.upper = interval.upper.copy();
	}
	
	private boolean isValidInterval() {
		boolean canBoundsBeEqual = isLowerIncluded && isUpperIncluded;
		boolean upperIsInfinity = upper == Bound.Infinity;
		boolean equalBounds = !upperIsInfinity && lower.value() == upper.value();
		boolean lowerIsNotInfinity =  lower != Bound.Infinity;
		boolean lowerSmallerThanUpper = lower.value() < upper.value();
		
		return lowerIsNotInfinity && 
			   ((upperIsInfinity && !isUpperIncluded) || lowerSmallerThanUpper || (canBoundsBeEqual && equalBounds));
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(isLowerIncluded ? "[" : "(");
		buffer.append(lower);
		buffer.append(",");
		buffer.append(upper);
		buffer.append(isUpperIncluded ? "]" : ")");
		return buffer.toString();
	}

	public boolean isIncluded(BigDecimal age) {
		BigDecimal lowerBound = new BigDecimal(lower.value());
		BigDecimal upperBound = new BigDecimal(upper.value());
		
		int lowerCompare = age.compareTo(lowerBound);
		int upperCompare = age.compareTo(upperBound);
		
		return (isLowerIncluded ? (lowerCompare >= 0) : (lowerCompare > 0)) && (isUpperIncluded ? (upperCompare <= 0) : (upperCompare < 0));
	}

	public Bound lowerBound() {
		return lower;
	}
	
	public Bound upperBound() {
		return upper;
	}
	
	public boolean IsLowerBoundNonStrict() {
		return isLowerIncluded;
	}
	
	public boolean IsUpperBoundNonStrict() {
		return isUpperIncluded;
	}
}
