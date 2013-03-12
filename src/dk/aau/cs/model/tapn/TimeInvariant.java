package dk.aau.cs.model.tapn;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pipe.gui.Pipe;

import dk.aau.cs.model.tapn.Bound.InfBound;
import dk.aau.cs.util.IntervalOperations;
import dk.aau.cs.util.Require;

public class TimeInvariant {
	public static final TimeInvariant LESS_THAN_INFINITY = new TimeInvariant(false, Bound.Infinity);
	private boolean isUpperIncluded;
	private Bound upper;

	public TimeInvariant(boolean isUpperIncluded, Bound upper) {
		Require.that(upper != null, "bound cannot be null");
		Require.that(isUpperIncluded || (!isUpperIncluded && upper.value() != 0), "\"< 0\" is and invalid invariant.");
		Require.that(upper != Bound.Infinity || !isUpperIncluded, "\"<=inf\" is not a valid invariant");
		
		this.isUpperIncluded = isUpperIncluded;
		this.upper = upper;
	}

	public Bound upperBound() {
		return upper;
	}

	public boolean isUpperNonstrict() {
		return isUpperIncluded;
	}


	public boolean isSatisfied(BigDecimal age) {
		if (upper instanceof InfBound)
			return true;
		int comparison = age.compareTo(new BigDecimal(upper.value()));

		return isUpperIncluded ? comparison <= 0 : comparison < 0;
	}

	public static TimeInvariant parse(String invariant, ConstantStore constants) {
		Pattern pattern = Pattern.compile("^(<|<=)\\s*(\\w+)$");
		Matcher matcher = pattern.matcher(invariant);
		matcher.find();

		String operator = matcher.group(1);
		String boundAsString = matcher.group(2);

		if (operator.equals("<") && boundAsString.equals("0"))
			return null;
		if (operator.equals("<=") && boundAsString.equals("inf"))
			return null;

		Bound bound = null;
		if (boundAsString.equals("inf"))
			bound = Bound.Infinity;
		else {
			try {
				int intBound = Integer.parseInt(boundAsString);
				bound = new IntBound(intBound);
			} catch (NumberFormatException e) {
				if (constants.containsConstantByName(boundAsString)) {
					bound = new ConstantBound(constants.getConstantByName(boundAsString));
				} else
					throw new RuntimeException("A constant which was not declared was used in an invariant.");
			}
		}

		return new TimeInvariant(operator.equals("<="), bound);
	}
	
	public TimeInvariant copy() {
		return new TimeInvariant(isUpperIncluded, upper.copy());
	}
	
	public TimeInterval subtractToken(BigDecimal age){
		BigDecimal iUp = IntervalOperations.getRatBound(upperBound()).getBound();
		
		if(iUp.compareTo(BigDecimal.ZERO) < 0){
			return new TimeInterval(true, new IntBound(0), Bound.Infinity, false);
		} else{
			BigDecimal newUpper = iUp.subtract(age, new MathContext(Pipe.AGE_PRECISION));
			return new TimeInterval(true, new IntBound(0), new RatBound(newUpper), isUpperNonstrict());
		}
	}
	
	public TimeInterval asIterval(){
		return new TimeInterval(true, new IntBound(0), upperBound(), isUpperNonstrict());
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(isUpperIncluded ? "<=" : "<");
		buffer.append(' ');
		buffer.append(upper);
		return buffer.toString();
	};
	
	public String toString(boolean displayConstantNames) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(isUpperIncluded ? "<=" : "<");
		buffer.append(' ');
		buffer.append((displayConstantNames || upper instanceof InfBound) ? upper	: upper.value());
		return buffer.toString();
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isUpperIncluded ? 1231 : 1237);
		result = prime * result + ((upper == null) ? 0 : upper.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof TimeInvariant))
			return false;
		TimeInvariant other = (TimeInvariant) obj;
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
