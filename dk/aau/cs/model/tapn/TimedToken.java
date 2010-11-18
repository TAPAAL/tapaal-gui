package dk.aau.cs.model.tapn;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import pipe.gui.Pipe;

public class TimedToken {
	private TimedPlace place;
	private BigDecimal age;
	
	public TimedToken(TimedPlace place, BigDecimal age){
		this.place = place;
		this.age = age;
	}
	
	public TimedPlace place(){
		return place;
	}
	
	public BigDecimal age(){
		return age;
	}
	
	@Override
	public String toString() {
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(Pipe.AGE_DECIMAL_PRECISION);
				
		StringBuffer buffer = new StringBuffer("(");
		buffer.append(place.toString());
		buffer.append(", ");
		buffer.append(df.format(age));
		buffer.append(")");
		return buffer.toString();
	}
}
