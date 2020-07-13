package dk.aau.cs.model.NTA.trace;

import java.util.HashMap;

public class Participant {
	private final String automata;
	private final String location;
	private final HashMap<String, ValueRange> localClocksAndVariables;

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