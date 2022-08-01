package dk.aau.cs.model.NTA.trace;

import java.math.BigDecimal;

import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedToken;

public class TraceToken extends TimedToken {
	
	private final boolean greaterThanOrEqual;
	
	public TraceToken(TimedPlace place, BigDecimal age, boolean greaterThanOrEqual, Color color) {
		super(place, age, color);
		this.greaterThanOrEqual = greaterThanOrEqual;
	}
	
	public boolean isGreaterThanOrEqual(){
		return greaterThanOrEqual;
	}

}
