package pipe.dataLayer.colors;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
		if(goesToInfinity()){
			return "inf";
		}else if(!a.isUsingConstant() && a.getValue() == 0){
			return String.valueOf(b);
		}else if(!b.isUsingConstant() && b.getValue() == 0){
			return String.format("%1$s*val", a);
		}
		else{
			return String.format("%1$s*val+%2$s", a, b);
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

	public boolean equals(ColoredToken token){
		return isLessThanOrEqual(token) && isGreaterThanOrEqual(token);
	}

	public List<String> getUsedConstants() {
		List<String> list = new ArrayList<String>();
		if(a.isUsingConstant()){
			list.add(a.getConstantName());
		}

		if(b.isUsingConstant()){
			list.add(b.getConstantName());
		}

		return list;
	}
}
