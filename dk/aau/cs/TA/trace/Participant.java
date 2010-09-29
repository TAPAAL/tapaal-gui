/**
 * 
 */
package dk.aau.cs.TA.trace;

import java.math.BigDecimal;
import java.util.HashMap;

public class Participant {
	private String location;
	private HashMap<String,BigDecimal> clockValue;

	public Participant(String location, HashMap<String,BigDecimal> clockValue){
		this.location = location;
		this.clockValue = clockValue;
	}

	public String location(){
		return location;
	}

	public BigDecimal clockValue(String clock){
		return clockValue.get(clock);
	}
}