package dk.aau.cs.model.tapn;

import dk.aau.cs.util.Require;

public class TimeInvariant {
	public static final TimeInvariant LESS_THAN_INFINITY = new TimeInvariant(false, Bound.Infinity);
	private boolean isUpperIncluded;
	private Bound upper;
	
	public TimeInvariant(boolean isUpperIncluded, Bound upper){
		Require.that(upper != null, "bound cannot be null");
		Require.that(isUpperIncluded || (!isUpperIncluded && upper.value() != 0), "\"< 0\" is and invalid invariant."); 
		Require.that(upper != Bound.Infinity || !isUpperIncluded, "\"<=inf\" is not a valid invariant");
		this.isUpperIncluded = isUpperIncluded;
		this.upper = upper;
	}
	
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(isUpperIncluded ? "<=" : "<");
		buffer.append(" ");
		buffer.append(upper);
		return buffer.toString();
	};
}
