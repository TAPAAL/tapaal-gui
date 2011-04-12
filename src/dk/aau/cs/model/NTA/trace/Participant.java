/**
 * 
 */
package dk.aau.cs.model.NTA.trace;

import java.util.HashMap;

public class Participant {
	private String automata;
	private String location;
	private HashMap<String, ValueRange> localClocksAndVariables;

	public Participant(String automata, String location,
			HashMap<String, ValueRange> localClocksAndVariables) {
		this.automata = automata;
		this.location = location;
		this.localClocksAndVariables = localClocksAndVariables;
	}

	public String location() {
		return location;
	}

	public String automata() {
		return automata;
	}

	public ValueRange clockOrVariableValue(String name) {
		return localClocksAndVariables.get(name);
	}
}