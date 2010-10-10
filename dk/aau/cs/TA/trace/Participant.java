/**
 * 
 */
package dk.aau.cs.TA.trace;

import java.math.BigDecimal;
import java.util.HashMap;

public class Participant {
	private String automata;
	private String location;
	private HashMap<String,BigDecimal> clockValue;
	private HashMap<String, Integer> localVariables;

	public Participant(String automata, String location, HashMap<String,BigDecimal> clockValue, HashMap<String, Integer> localVariables){
		this.automata = automata;
		this.location = location;
		this.clockValue = clockValue;
		this.localVariables = localVariables;
	}

	public String location(){
		return location;
	}
	
	public String automata(){
		return automata;
	}

	public BigDecimal clockValue(String clock){
		return clockValue.get(clock);
	}
	
	public int variable(String variable){
		return localVariables.get(variable);
	}
}