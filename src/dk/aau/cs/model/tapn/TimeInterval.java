package dk.aau.cs.model.tapn;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dk.aau.cs.model.tapn.Bound.InfBound;
import dk.aau.cs.util.IntervalOperations;
import dk.aau.cs.util.Require;

public class TimeInterval {
	public static final TimeInterval ZERO_INF = new TimeInterval(true,
			new IntBound(0), Bound.Infinity, false);
	private boolean isLowerIncluded;
	private Bound lower;
	private Bound upper;
	private boolean isUpperIncluded;

	public TimeInterval(boolean isLowerIncluded, Bound lower, Bound upper,
			boolean isUpperIncluded) {
		Require.that(lower != null, "Lower bound cannot be null");
		Require.that(upper != null, "Upper bound cannot be null");

		this.isLowerIncluded = isLowerIncluded;
		this.lower = lower;
		this.upper = upper;
		this.isUpperIncluded = isUpperIncluded;
		
		Require.that(isValidInterval(), "The constructed interval "
				+ toString() + " is empty.");
	}

	public TimeInterval(TimeInterval interval) {
		Require.that(interval != null, "Interval cannot be null");

		isLowerIncluded = interval.isLowerIncluded;
		isUpperIncluded = interval.isUpperIncluded;
		lower = interval.lower.copy();
		upper = interval.upper.copy();
	}

	private boolean isValidInterval() {
		boolean canBoundsBeEqual = isLowerIncluded && isUpperIncluded;
		boolean upperIsInfinity = (upper instanceof InfBound);
		boolean equalBounds = !upperIsInfinity
				&& IntervalOperations.getRatBound(lower).getBound().compareTo(IntervalOperations.getRatBound(upper).getBound()) == 0;
		boolean lowerIsNotInfinity = lower != Bound.Infinity;
		boolean lowerSmallerThanUpper = IntervalOperations.getRatBound(lower).getBound().compareTo(IntervalOperations.getRatBound(upper).getBound()) < 0;;

		return lowerIsNotInfinity
				&& ((upperIsInfinity && !isUpperIncluded)
						|| lowerSmallerThanUpper || (canBoundsBeEqual && equalBounds));
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(isLowerIncluded ? "[" : "(");
		buffer.append(lower);
		buffer.append(',');
		buffer.append(upper);
		buffer.append(isUpperIncluded ? "]" : ")");
		return buffer.toString();
	}

	public String toString(boolean displayConstantNames) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(isLowerIncluded ? "[" : "(");
		buffer.append(displayConstantNames ? lower : lower.value());
		buffer.append(',');
		buffer.append(displayConstantNames || upper instanceof InfBound ? upper : upper.value());
		buffer.append(isUpperIncluded ? "]" : ")");
		return buffer.toString();
	}

	public boolean isIncluded(BigDecimal age) {
		return satisfiesLowerBound(age) && satisfiesUpperBound(age);
	}

	private boolean satisfiesLowerBound(BigDecimal value) {
		BigDecimal lowerAsBigDec = IntervalOperations.getRatBound(lower).getBound();
		int compare = value.compareTo(lowerAsBigDec);
		return isLowerIncluded ? (compare >= 0) : (compare > 0);
	}

	private boolean satisfiesUpperBound(BigDecimal value) {
		if (upper instanceof InfBound)
			return true;
		
		BigDecimal upperAsBigDec = IntervalOperations.getRatBound(upper).getBound();
		int compare = value.compareTo(upperAsBigDec);
		return isUpperIncluded ? (compare <= 0) : (compare < 0);
	}

	public Bound lowerBound() {
		return lower;
	}
	
	public void setLowerBound(Bound lower) {
		this.lower = lower;
	}

	public Bound upperBound() {
		return upper;
	}
	
	public void setUpperBound(Bound upper) {
		this.upper = upper;
	}

	public boolean IsLowerBoundNonStrict() {
		return isLowerIncluded;
	}

	public boolean IsUpperBoundNonStrict() {
		return isUpperIncluded;
	}

	public static TimeInterval parse(String interval, ConstantStore constants) {
		Pattern pattern = Pattern.compile("^(\\[|\\()\\s*(\\w+)\\s*,\\s*(\\w+)(\\]|\\))$");
		Matcher matcher = pattern.matcher(interval);
		matcher.find();

		String leftBracket = matcher.group(1);
		String lowerBoundAsString = matcher.group(2);
		String upperBoundAsString = matcher.group(3);
		String rightBracket = matcher.group(4);

		if (!(leftBracket.equals("[") || leftBracket.equals("(")))
			return null;
		if (!(rightBracket.equals("]") || rightBracket.equals(")")))
			return null;

		Bound lowerBound = null;
		try {
			int intLower = Integer.parseInt(lowerBoundAsString);
			lowerBound = new IntBound(intLower);
		} catch (NumberFormatException e) {
			if (constants.containsConstantByName(lowerBoundAsString)) {
				lowerBound = new ConstantBound(constants.getConstantByName(lowerBoundAsString));
			} else
				throw new RuntimeException("A constant which was not declared was used in an time interval of an arc.");
		}
		
		Bound upperBound = null;
		if (upperBoundAsString.equals("inf"))
			upperBound = Bound.Infinity;
		else {
			try {
				int intBound = Integer.parseInt(upperBoundAsString);
				upperBound = new IntBound(intBound);
			} catch (NumberFormatException e) {
				if (constants.containsConstantByName(upperBoundAsString)) {
					upperBound = new ConstantBound(constants.getConstantByName(upperBoundAsString));
				} else
					throw new RuntimeException(
							"A constant which was not declared was used in an time interval of an arc.");
			}
		}

		return new TimeInterval(leftBracket.equals("[") ? true : false,
				lowerBound, upperBound, rightBracket.equals("]") ? true : false);
	}

	public TimeInterval copy() {
		return new TimeInterval(this);
	}

	public TimeInterval intersect(TimeInvariant invariant) {
		if(invariant.equals(TimeInvariant.LESS_THAN_INFINITY))
			return this.copy();
	
		int invariantUpper = invariant.upperBound().value();
		
		if (invariantUpper < lower.value() || (invariantUpper == lower.value() && !isLowerIncluded) || (invariantUpper == lower.value() && !invariant.isUpperNonstrict())) {
			return new TimeInterval(false, new IntBound(0), new IntBound(0), false); // intersection is empty, so return an empty interval
		} else if (invariantUpper > upper.value() && !upper.equals(Bound.Infinity) || invariantUpper == upper.value() && !upper.equals(Bound.Infinity) && !isUpperIncluded ) {
			return this.copy();
		} else {
			return new TimeInterval(isLowerIncluded, lower.copy(), invariant.upperBound().copy(), invariant.isUpperNonstrict());
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof TimeInterval))
			return false;
		TimeInterval other = (TimeInterval) obj;
		if(isLowerIncluded != other.isLowerIncluded)
			return false;
		if(lower == null) {
			if(other.lower != null)
				return false;
		} else if(!lower.equals(other.lower))
			return false;
		if (isUpperIncluded != other.isUpperIncluded)
			return false;
		if (upper == null) {
			if (other.upper != null)
				return false;
		} else if (!upper.equals(other.upper))
			return false;
		return true;
	}

}
