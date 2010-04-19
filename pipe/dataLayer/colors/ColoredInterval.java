package pipe.dataLayer.colors;

import java.math.BigDecimal;

public class ColoredInterval {
	private boolean lowerIncluded = true;
	private IntervalBound lower = new IntervalBound(false);
	private IntervalBound upper = new IntervalBound(true);
	private boolean upperIncluded = false;
	
	public ColoredInterval(){
		
	};
	
	public ColoredInterval(String lowerParenthesis, IntervalBound lower, IntervalBound upper, String upperParenthesis){
		lowerIncluded = !lowerParenthesis.equals("(");
		upperIncluded = upperParenthesis.equals("]");
		this.lower = lower;
		this.upper = upper;		
	}
	
	public IntervalBound getLower(){
		return lower;
	}
	
	public IntervalBound getUpper(){
		return upper;
	}
	
	public boolean goesToInfinity(){
		return upper.goesToInfinity();
	}
	
	public String getOpenParenthesis(){
		return lowerIncluded ? "[" : "(";
	}
	
	public String getCloseParenthesis(){
		return upperIncluded ? "]" : ")";
	}
		
	public String toString() {
		String format = "%1$s%2$s,%3$s%4$s";
		
		return String.format(format, getOpenParenthesis(), lower, upper, getCloseParenthesis());
	}

	public boolean contains(ColoredToken token) {
		BigDecimal age = token.getAge();
		if(lower.equals(age) && !lowerIncluded) return false;
		if(upper.equals(age) && !upperIncluded) return false;
		
		return lower.isLessThanOrEqual(token) && upper.isGreaterThanOrEqual(token);
	}
}
