package dk.aau.cs.model.tapn;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(isUpperIncluded ? "<=" : "<");
		buffer.append(" ");
		buffer.append(upper);
		return buffer.toString();
	};
	
	public static TimeInvariant parse(String invariant){
		Pattern pattern = Pattern.compile("^(<|<=)\\s*(\\d+|inf)$");
		Matcher matcher = pattern.matcher(invariant);
		matcher.find();
		
		String operator = matcher.group(1);
		String boundAsString = matcher.group(2);
		
		if(operator.equals("<") && boundAsString.equals("0")) return null;
		if(operator.equals("<=") && boundAsString .equals("inf")) return null;
		
		
		Bound bound = null;
		if(boundAsString.equals("inf")) bound = Bound.Infinity;
		else{
			try{
				int intBound = Integer.parseInt(boundAsString);
				bound = new IntBound(intBound);
			}catch(NumberFormatException e){
				//bound = new ConstantBound()
			}
		}
		
		return new TimeInvariant(operator.equals("<="), bound); 	
	}
}
