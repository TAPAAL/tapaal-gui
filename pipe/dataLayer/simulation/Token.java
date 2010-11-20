package pipe.dataLayer.simulation;

import java.math.BigDecimal;

import pipe.dataLayer.TimedPlaceComponent;

public class Token {
	private TimedPlaceComponent place;
	private BigDecimal age;
	
	public Token(TimedPlaceComponent place, double age){
		this(place, new BigDecimal(age));
	}
	
	public Token(TimedPlaceComponent place, BigDecimal age){
		this.place = place;
		this.age = age;
	}
	
	public TimedPlaceComponent place(){
		return place;
	}
	
	public BigDecimal age(){
		return age;
	}
}
