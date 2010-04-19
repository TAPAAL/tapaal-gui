package pipe.dataLayer.colors;

import java.math.BigDecimal;

public class IntervalBound {
	private IntOrConstant a;
	private IntOrConstant b;
	
	public IntervalBound(IntOrConstant a, IntOrConstant b){
		this.a = a;
		this.b = b;
	}
	
	public IntervalBound(boolean infinity){
		this.a = new IntOrConstant();
		
		if(infinity){
			this.b = new IntOrConstant(-1);
		}else{
			this.b = new IntOrConstant();
		}
	}
	
	public String toString() {
		int valueOfa = a.isUsingConstant() ? a.getConstantValue() : a.getIntegerValue();
		
		if(goesToInfinity()){
			return "inf";
		}else if(valueOfa == 0){
			return String.valueOf(b);
		}else{
			return String.format("%1$d*val+%2$s", valueOfa, b);
		}
	}

	public boolean isLessThanOrEqual(ColoredToken token) {
		int lower = token.getColor().getValue() * a.getValue() + b.getValue();
		
		return new BigDecimal(lower).compareTo(token.getAge()) < 0;
	}

	public boolean isGreaterThanOrEqual(ColoredToken token) {
		int upper = token.getColor().getValue() * a.getValue() + b.getValue();
		
		return new BigDecimal(upper).compareTo(token.getAge()) >= 0;
	}

	public boolean goesToInfinity() {
		return !b.isUsingConstant() && b.getIntegerValue() == -1;
	}

	public IntOrConstant getScale() {
		return a;
	}
	
	public IntOrConstant getOffset(){
		return b;
	}
}
