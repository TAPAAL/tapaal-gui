package dk.aau.cs.petrinet.colors;

import dk.aau.cs.petrinet.TAPNPlace;
import dk.aau.cs.petrinet.Token;

public class ColoredToken extends Token {
	private int color;
	
	public ColoredToken(TAPNPlace place){
		this(place, 0);
	}
	
	public ColoredToken(TAPNPlace place, int color){
		super(place);
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
