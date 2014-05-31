package dk.aau.cs.model.tapn;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import pipe.gui.Pipe;

public class TimedToken {
	private TimedPlace place;
	private BigDecimal age;

	public TimedToken(TimedPlace place) {
		this(place, BigDecimal.ZERO);
	}

	public TimedToken(TimedPlace place, BigDecimal age) {
		this.place = place;
		this.age = age;
	}

	public TimedPlace place() {
		return place;
	}

	public BigDecimal age() {
		return age;
	}
	
	public void setAge(BigDecimal age) {
		this.age = age;		
	}
	
	public TimedToken clone() {
		return new TimedToken(place, age); // age is immutable so ok to pass it
											// to constructor
	}

	public TimedToken delay(BigDecimal delay) {
		return new TimedToken(place, age.add(delay));
	}

	@Override
	public String toString() {
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(Pipe.AGE_DECIMAL_PRECISION);

		StringBuffer buffer = new StringBuffer("(");
		buffer.append(place.toString());
		buffer.append(", ");
		buffer.append(df.format(age));
		buffer.append(')');
		return buffer.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((age == null) ? 0 : age.hashCode());
		result = prime * result + ((place == null) ? 0 : place.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof TimedToken))
			return false;
		TimedToken other = (TimedToken) obj;
		if (age == null) {
			if (other.age != null)
				return false;
		} else if (age.compareTo(other.age) != 0){
			return false;
		}
		if (place == null) {
			if (other.place != null)
				return false;
		} else if (!place.equals(other.place))
			return false;
		return true;
	}
}
