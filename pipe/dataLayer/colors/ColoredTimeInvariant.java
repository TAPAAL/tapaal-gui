package pipe.dataLayer.colors;

import java.util.ArrayList;
import java.util.List;

public class ColoredTimeInvariant {
	public boolean strictLessThan;
	private IntervalBound upper;
	
	public ColoredTimeInvariant(){
		
	}
	
	public ColoredTimeInvariant(String operator, IntervalBound upper){
		strictLessThan = operator.equals("<");
		
		this.upper = upper;
	}
	
	public ColoredTimeInvariant(String invariant) {
		String operator = invariant.contains("<=") ? "<=" : "<";
		
		strictLessThan = operator.equals("<");
		String val = invariant.substring(operator.length()).trim();
		if(!val.equals("inf"))
		{
			upper = new IntervalBound(val);		
		}
	}

	public boolean goesToInfinity(){
		return upper == null;
	}

	public boolean contains(ColoredToken token) {
		if(goesToInfinity()) return true;
		
		return (!strictLessThan && upper.isGreaterThanOrEqual(token)) ||
			(strictLessThan && upper.isGreaterThanOrEqual(token) && !upper.equals(token));
	}
	
	public IntervalBound getUpper(){
		return upper;
	}
	
	
	public String toString() {
		String operator = getOperator();
		String bound = goesToInfinity() ? "inf" : upper.toString();
		return String.format("%1$s%2$s", operator, bound); 
	}

	public String getOperator() {
		String operator = goesToInfinity() || strictLessThan ? "<" : "<=";
		return operator;
	}

	public List<String> getUsedConstantNames() {
		return goesToInfinity() ? new ArrayList<String>() : upper.getUsedConstants();
	}

	public String toStringWithoutConstants() {
		String operator = getOperator();
		String bound = goesToInfinity() ? "inf" : upper.toStringWithoutConstants();
		return String.format("%1$s%2$s", operator, bound); 
	}
}
