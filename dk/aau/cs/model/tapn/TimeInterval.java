package dk.aau.cs.model.tapn;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import pipe.gui.CreateGui;
import dk.aau.cs.model.tapn.Bound.InfBound;
import dk.aau.cs.util.Require;

public class TimeInterval {
	public static final TimeInterval ZERO_INF = new TimeInterval(true, new IntBound(0), Bound.Infinity, false);
	private boolean isLowerIncluded;
	private Bound lower;
	private Bound upper;
	private boolean isUpperIncluded;
	
	public TimeInterval(boolean isLowerIncluded, Bound lower, Bound upper, boolean isUpperIncluded){
		Require.that(lower != null, "Lower bound cannot be null");
		Require.that(upper != null, "Upper bound cannot be null");
		
		this.isLowerIncluded = isLowerIncluded;
		this.lower = lower;
		this.upper = upper;
		this.isUpperIncluded = isUpperIncluded;
		Require.that(isValidInterval(), "The constructed interval " + toString() + " is empty.");
	}
	
	public TimeInterval(TimeInterval interval) {
		Require.that(interval != null, "Interval cannot be null");
		
		this.isLowerIncluded = interval.isLowerIncluded;
		this.isUpperIncluded = interval.isUpperIncluded;
		this.lower = interval.lower.copy();
		this.upper = interval.upper.copy();
	}
	
	private boolean isValidInterval() {
		boolean canBoundsBeEqual = isLowerIncluded && isUpperIncluded;
		boolean upperIsInfinity = upper == Bound.Infinity;
		boolean equalBounds = !upperIsInfinity && lower.value() == upper.value();
		boolean lowerIsNotInfinity =  lower != Bound.Infinity;
		boolean lowerSmallerThanUpper = lower.value() < upper.value();
		
		return lowerIsNotInfinity && 
			   ((upperIsInfinity && !isUpperIncluded) || lowerSmallerThanUpper || (canBoundsBeEqual && equalBounds));
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(isLowerIncluded ? "[" : "(");
		buffer.append(lower);
		buffer.append(",");
		buffer.append(upper);
		buffer.append(isUpperIncluded ? "]" : ")");
		return buffer.toString();
	}
	
	public String toString(boolean displayConstantNames) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(isLowerIncluded ? "[" : "(");
		buffer.append(displayConstantNames ? lower : lower.value());
		buffer.append(",");
		buffer.append(displayConstantNames || upper instanceof InfBound ? upper : upper.value());
		buffer.append(isUpperIncluded ? "]" : ")");
		return buffer.toString();
	}

	public boolean isIncluded(BigDecimal age) {
		return satisfiesLowerBound(age) && satisfiesUpperBound(age);
	}
	
	private boolean satisfiesLowerBound(BigDecimal value){
		int compare = value.compareTo(new BigDecimal(lower.value()));
		return isLowerIncluded ? (compare >= 0) : (compare > 0);
	}
	
	private boolean satisfiesUpperBound(BigDecimal value){
		if(upper instanceof InfBound) return true;
		
		int compare = value.compareTo(new BigDecimal(upper.value()));
		return isUpperIncluded ? (compare <= 0) : (compare < 0);
	}

	public Bound lowerBound() {
		return lower;
	}
	
	public Bound upperBound() {
		return upper;
	}
	
	public boolean IsLowerBoundNonStrict() {
		return isLowerIncluded;
	}
	
	public boolean IsUpperBoundNonStrict() {
		return isUpperIncluded;
	}

	public static TimeInterval parse(String interval) {
		Pattern pattern = Pattern.compile("^(\\[|\\()\\s*(\\w+)\\s*,\\s*(\\w+)(\\]|\\))$");
		Matcher matcher = pattern.matcher(interval);
		matcher.find();
		
		String leftBracket = matcher.group(1);
		String lowerBoundAsString = matcher.group(2);
		String upperBoundAsString = matcher.group(3);
		String rightBracket = matcher.group(4);
		
		if(!(leftBracket.equals("[") || leftBracket.equals("("))) return null;
		if(!(rightBracket.equals("]") || rightBracket.equals(")"))) return null;
		
		Bound lowerBound = null;
		try{
			int intLower = Integer.parseInt(lowerBoundAsString);
			lowerBound = new IntBound(intLower);
		} catch(NumberFormatException e) {
			JOptionPane.showMessageDialog(CreateGui.getApp(), "Parsing time interval from string containing constants not yet implemented");
		}
		
		Bound upperBound = null;
		if(upperBoundAsString.equals("inf")) upperBound = Bound.Infinity;
		else{
			try{
				int intBound = Integer.parseInt(upperBoundAsString);
				upperBound = new IntBound(intBound);
			}catch(NumberFormatException e){
				JOptionPane.showMessageDialog(CreateGui.getApp(), "Parsing time interval from string containing constants not yet implemented");
			}
		}
		
		return new TimeInterval(leftBracket.equals("[") ? true : false, lowerBound, upperBound, rightBracket.equals("]") ? true : false);	
	}

	
}
