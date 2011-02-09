package dk.aau.cs.petrinet.colors;

public class ColoredInterval {
	protected char lowerParenthesis;
	protected IntervalBound lower;
	protected IntervalBound upper;
	protected char upperParenthesis;

	public ColoredInterval(String interval) {
		lowerParenthesis = interval.charAt(0);
		upperParenthesis = interval.charAt(interval.length() - 1);

		String[] split = interval.split(",");
		lower = new IntervalBound(split[0].substring(1).trim());
		upper = new IntervalBound(split[1].substring(0, split[1].length() - 1)
				.trim());
	}

	public ColoredInterval() {
		lowerParenthesis = '[';
		lower = new IntervalBound(0, 0);
		upper = new IntervalBound("inf");
		upperParenthesis = ')';
	}

	public ColoredInterval(ColoredInterval other) {
		this.lowerParenthesis = other.lowerParenthesis;
		this.upperParenthesis = other.upperParenthesis;
		this.lower = new IntervalBound(other.lower);
		this.upper = new IntervalBound(other.upper);
	}

	public String convertToTAGuardString(String clockName, String tokenValueName) {
		String lowerOperator = lowerParenthesis == '[' ? ">=" : ">";
		String upperOperator = upperParenthesis == ']' ? "<=" : "<";

		if (goesToInfinity()) {
			if (lower.isZero()) {
				return "";
			} else {
				return String.format("%1$s %2$s %3$s", clockName,
						lowerOperator, lower.toString(tokenValueName));
			}
		} else {
			return String.format("%1$s %2$s %3$s && %1$s %4$s %5$s", clockName,
					lowerOperator, lower.toString(tokenValueName),
					upperOperator, upper.toString(tokenValueName));
		}
	}

	protected boolean goesToInfinity() {
		return upper.goesToInfinity();
	}
}
