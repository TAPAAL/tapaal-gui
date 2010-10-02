package dk.aau.cs.petrinet.colors;


public class ColoredTimeInvariant {
	public boolean strictLessThan;
	private IntervalBound upper;
	
	public ColoredTimeInvariant(){
		this("<inf");
	}
	
	public ColoredTimeInvariant(String operator, IntervalBound upper){
		strictLessThan = operator.equals("<");
		
		this.upper = upper;
	}
	
	public ColoredTimeInvariant(String invariant) {
		String operator = invariant.contains("<=") ? "<=" : "<";
		
		strictLessThan = operator.equals("<");
		upper = new IntervalBound(invariant.substring(operator.length()).trim());		
	}

	public ColoredTimeInvariant(ColoredTimeInvariant timeInvariant) {
		this.strictLessThan = timeInvariant.strictLessThan;
		this.upper = new IntervalBound(timeInvariant.upper);
	}

	public String convertToTAInvariantString(String tokenClockName,
			String valueVarName) {
		if(upper.goesToInfinity()) return "";
		
		String operator = strictLessThan ? "<" : "<=";
		return String.format("%1$s %2$s %3$s", tokenClockName, operator , upper.toString(valueVarName));
	}

	public static ColoredTimeInvariant getZeroInvariant() {
		return new ColoredTimeInvariant("<=", new IntervalBound(0,0));
	}
}
