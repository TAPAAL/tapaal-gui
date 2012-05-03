package dk.aau.cs.model.NTA.trace;

import java.math.BigDecimal;

import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedToken;

public class TraceToken extends TimedToken {
	
	private boolean greaterThanOrEqual = false;

	public TraceToken(TimedPlace place) {
		super(place);
	}

	public TraceToken(TimedPlace place, BigDecimal age) {
		super(place, age);
	}
	
	public TraceToken(TimedPlace place, BigDecimal age, boolean greaterThanOrEqual) {
		super(place, age);
		this.greaterThanOrEqual = greaterThanOrEqual;
	}
	
	public boolean isGreaterThanOrEqual(){
		return greaterThanOrEqual;
	}

}
