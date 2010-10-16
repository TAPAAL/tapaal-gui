package dk.aau.cs.TA.trace;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pipe.gui.Pipe;


public class SymbolicState {
	private static final String AUTOMATA_LOCATION_PATTERN = "([\\w\\(\\)]+)\\.(\\w+)";
	private final HashMap<String, String> automataLocations;
	private final HashMap<String, HashMap<String, BigDecimal>> localClocksAndVariables;
	private final HashMap<String, BigDecimal> globalClocksAndVariables;

	public SymbolicState(
			HashMap<String,String> locations, 
			HashMap<String, HashMap<String, BigDecimal>> localClocksAndVariables, 
			HashMap<String, BigDecimal> globalClocksAndVariables 
	){
		this.automataLocations = locations;
		this.localClocksAndVariables = localClocksAndVariables;
		this.globalClocksAndVariables = globalClocksAndVariables;
	}

	public HashMap<String, BigDecimal> getLocalClocksAndVariablesFor(String automata) {
		return localClocksAndVariables.get(automata);
	}

	public BigDecimal globalClockOrVariableValue(String name){
		return globalClocksAndVariables.get(name);
	}

	public String locationFor(String automata){
		return automataLocations.get(automata);
	}

	public static SymbolicState parse(String state){
		String[] stateLines = state.split("\n");

		HashMap<String,String> locations = parseLocations(stateLines[1]);
		HashMap<String, HashMap<String, BigDecimal>> localClocksAndVariables = parseLocalClocksAndVariables(stateLines[2]);
		HashMap<String, BigDecimal> globalClocksAndVariables = parseGlobalClocksAndVariables(stateLines[2]);
		return new SymbolicState(locations, localClocksAndVariables, globalClocksAndVariables);
	}

	private static HashMap<String, String> parseLocations(String string) {
		String[] split = string.split(" ");
		HashMap<String,String> locations = new HashMap<String, String>(split.length-2);

		Pattern pattern = Pattern.compile(AUTOMATA_LOCATION_PATTERN);
		for(int i = 1; i < split.length-1; i++){
			Matcher matcher = pattern.matcher(split[i]);
			matcher.find();
			String automata = matcher.group(1);
			String location = matcher.group(2);
			locations.put(automata, location);
		}

		return locations;
	}

	private static HashMap<String, HashMap<String, BigDecimal>> parseLocalClocksAndVariables(String string) {
		String[] split = string.split(" ");
		HashMap<String, HashMap<String, BigDecimal>> clocksAndVariables = new HashMap<String, HashMap<String,BigDecimal>>();

		Pattern pattern = Pattern.compile(AUTOMATA_LOCATION_PATTERN + "=(\\d+)");
		for(int i = 0; i < split.length; i++){
			Matcher matcher = pattern.matcher(split[i].trim());

			if(matcher.matches()){			
				String automata = matcher.group(1);
				String clockOrVariable = matcher.group(2);
				double value = Double.parseDouble(matcher.group(3));

				if(!clocksAndVariables.containsKey(automata)){
					clocksAndVariables.put(automata, new HashMap<String, BigDecimal>());
				}

				clocksAndVariables.get(automata).put(clockOrVariable, new BigDecimal(value, new MathContext(Pipe.AGE_DECIMAL_PRECISION)));
			}
		}

		return clocksAndVariables;
	}

	private static HashMap<String, BigDecimal> parseGlobalClocksAndVariables(String string) {
		String[] split = string.split(" ");
		HashMap<String, BigDecimal> global = new HashMap<String, BigDecimal>();

		Pattern pattern = Pattern.compile("(\\w+)=(\\d+)");

		for(String variable : split){
			Matcher matcher = pattern.matcher(variable.trim());
			if(matcher.matches()){
				String name = matcher.group(1);
				double valueAsDouble = Double.parseDouble(matcher.group(2));
				
				global.put(name, new BigDecimal(valueAsDouble, new MathContext(Pipe.AGE_DECIMAL_PRECISION)));
			}
		}


		return global;
	}

	public BigDecimal getLocalClockOrVariable(String automata,
			String colorVariableName) {
		return localClocksAndVariables.get(automata).get(colorVariableName);
	}
}
