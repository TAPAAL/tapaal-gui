package dk.aau.cs.petrinet;

import java.math.BigDecimal;

public class Token {
	private TAPNPlace place;
	private BigDecimal age;
	
	public Token(TAPNPlace place){
		this(place, BigDecimal.ZERO);
	}
	
	public Token(TAPNPlace place, BigDecimal age){
		this.place = place;
		this.age = age;
		
	}
	
	public TAPNPlace place(){
		return place;
	}
	
	public BigDecimal age(){
		return age;
	}
}
