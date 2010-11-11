package dk.aau.cs.model.tapn;

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
}
