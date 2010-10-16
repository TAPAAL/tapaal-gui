/**
 * 
 */
package dk.aau.cs.TA.trace;

import java.math.BigDecimal;
import java.util.HashMap;

public class Participant {
	private String automata;
	private String location;
	private HashMap<String,BigDecimal> localClocksAndVariables;

	public Participant(String automata, String location, HashMap<String,BigDecimal> localClocksAndVariables){
		this.automata = automata;
		this.location = location;
		this.localClocksAndVariables = localClocksAndVariables;
	}

	public String location(){
		return location;
	}
	
	public String automata(){
		return automata;
	}

	public BigDecimal clockOrVariableValue(String name){
		return localClocksAndVariables.get(name);
	}
}