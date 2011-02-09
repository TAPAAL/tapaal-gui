package dk.aau.cs.model.tapn;

import java.math.BigDecimal;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dk.aau.cs.model.tapn.Bound.InfBound;
import dk.aau.cs.util.Require;

public class TimeInvariant {
	public static final TimeInvariant LESS_THAN_INFINITY = new TimeInvariant(
			false, Bound.Infinity);
	private boolean isUpperIncluded;
	private Bound upper;

	public TimeInvariant(boolean isUpperIncluded, Bound upper) {
		Require.that(upper != null, "bound cannot be null");
		Require.that(isUpperIncluded
				|| (!isUpperIncluded && upper.value() != 0),
				"\"< 0\" is and invalid invariant.");
		Require.that(upper != Bound.Infinity || !isUpperIncluded,
				"\"<=inf\" is not a valid invariant");
		this.isUpperIncluded = isUpperIncluded;
		this.upper = upper;
	}

	public Bound upperBound() {
		return upper;
	}

	public boolean isUpperNonstrict() {
		return isUpperIncluded;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(isUpperIncluded ? "<=" : "<");
		buffer.append(" ");
		buffer.append(upper);
		return buffer.toString();
	};

	public String toString(boolean displayConstantNames) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(isUpperIncluded ? "<=" : "<");
		buffer.append(" ");
		buffer.append(displayConstantNames || upper instanceof InfBound ? upper
				: upper.value());
		return buffer.toString();
	}

	public boolean isSatisfied(BigDecimal age) {
		if (upper instanceof InfBound)
			return true;
		int comparison = age.compareTo(new BigDecimal(upper.value()));

		return isUpperIncluded ? comparison <= 0 : comparison < 0;
	}

	public static TimeInvariant parse(String invariant,
			TreeMap<String, Constant> constants) {
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
				if (constants.containsKey(boundAsString)) {
					bound = new ConstantBound(constants.get(boundAsString));
				} else
					throw new RuntimeException(
							"A constant which was not declared was used in an invariant.");
			}
		}

		return new TimeInvariant(operator.equals("<="), bound);
	}

	public static TimeInvariant parseInvariantWithoutConstant(String invariant) {
		return TimeInvariant.parse(invariant, new TreeMap<String, Constant>());
	}

}
