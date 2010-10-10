package dk.aau.cs.petrinet.colors;

import java.math.BigDecimal;

import dk.aau.cs.petrinet.TAPNPlace;
import dk.aau.cs.petrinet.Token;

public class ColoredToken extends Token {
	private int color;
	
	public ColoredToken(TAPNPlace place, BigDecimal age){
		this(place, age, 0);
	}
	
	public ColoredToken(TAPNPlace place, BigDecimal age, int color){
		super(place, age);
		this.color = color;
	}

	public ColoredToken(ColoredToken token) {
		super(token.place());
		this.color = token.color;
	}

	public int getColor() {
		return color;
	}
}
