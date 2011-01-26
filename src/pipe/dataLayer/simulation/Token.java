package pipe.dataLayer.simulation;

import java.math.BigDecimal;

import pipe.dataLayer.TimedPlace;

public class Token {
	private TimedPlace place;
	private BigDecimal age;
	
	public Token(TimedPlace place, double age){
		this(place, new BigDecimal(age));
	}
	
	public Token(TimedPlace place, BigDecimal age){
		this.place = place;
		this.age = age;
	}
	
	public TimedPlace place(){
		return place;
	}
	
	public BigDecimal age(){
		return age;
	}
}
