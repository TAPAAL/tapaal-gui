package pipe.dataLayer.colors;

import java.math.BigDecimal;

public class ColoredInterval {
	private boolean lowerIncluded = true;
	private IntervalBound lower = new IntervalBound(false);
	private IntervalBound upper = new IntervalBound(true);
	private boolean upperIncluded = false;
	
	public ColoredInterval(){};
	
	public ColoredInterval(String lowerParenthesis, IntervalBound lower, IntervalBound upper, String upperParenthesis){
		lowerIncluded = !lowerParenthesis.equals("(");
		upperIncluded = upperParenthesis.equals("]");
		this.lower = lower;
		this.upper = upper;		
	}
		
	public String toString() {
		String format = "%1$s%2$s,%3$s%4$s";
		String lowerParenthesis = lowerIncluded ? "[" : "(";
		String upperParenthesis = upperIncluded ? "]" : ")";
		
		return String.format(format, lowerParenthesis, lower, upper, upperParenthesis);
	}

	public boolean contains(ColoredToken token) {
		BigDecimal age = token.getAge();
		if(lower.equals(age) && !lowerIncluded) return false;
		if(upper.equals(age) && !upperIncluded) return false;
		
		return lower.isLessThanOrEqual(token) && upper.isGreaterThanOrEqual(token);
	}
}
