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
	private final HashMap<String, HashMap<String, BigDecimal>> automataClockValues;
	private final HashMap<String, Integer> globalVariables;
	private final HashMap<String, HashMap<String, Integer>> localVariables;

	public SymbolicState(
			HashMap<String,String> locations, 
			HashMap<String, HashMap<String, BigDecimal>> clocks, 
			HashMap<String, Integer> globalVariables, 
			HashMap<String, HashMap<String, Integer>> localVariables
		){
		this.automataLocations = locations;
		this.automataClockValues = clocks;
		this.globalVariables = globalVariables;
		this.localVariables = localVariables;
	}
	
	public HashMap<String, BigDecimal> getClockValues(String automata) {
		return automataClockValues.get(automata);
	}
	
	public HashMap<String, Integer> localVariablesFor(String automata) {
		return localVariables.get(automata);
	}
	
	public int globalVariableValue(String variable){
		return globalVariables.get(variable);
	}
	
	public String locationFor(String automata){
		return automataLocations.get(automata);
	}

	public static SymbolicState parse(String state){
		String[] stateLines = state.split("\n");

		HashMap<String,String> locations = parseLocations(stateLines[1]);
		HashMap<String, HashMap<String, BigDecimal>> clocks = parseAges(stateLines[2]);
		HashMap<String, Integer> globalVariables = parseGlobalVariables(stateLines[2]);
		HashMap<String, HashMap<String, Integer>> localVariables = parseLocalVariables(stateLines[2]);
		return new SymbolicState(locations, clocks, globalVariables, localVariables);
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
	
	private static HashMap<String, HashMap<String, BigDecimal>> parseAges(String string) {
		String clockValues = extractClockPart(string);
		String[] split = clockValues.split(" ");
		HashMap<String, HashMap<String, BigDecimal>> clocks = new HashMap<String, HashMap<String,BigDecimal>>(split.length-1);

		Pattern pattern = Pattern.compile(AUTOMATA_LOCATION_PATTERN + "=(\\d+)");
		for(int i = 0; i < split.length-1; i++){
			Matcher matcher = pattern.matcher(split[i]);

			if(matcher.matches()){			
				String automata = matcher.group(1);
				String clock = matcher.group(2);
				double value = Double.parseDouble(matcher.group(3));

				if(!clocks.containsKey(automata)){
					clocks.put(automata, new HashMap<String, BigDecimal>());
				}

				clocks.get(automata).put(clock, new BigDecimal(value, new MathContext(Pipe.AGE_DECIMAL_PRECISION)));
			}
		}

		return clocks;
	}
	
	private static HashMap<String, Integer> parseGlobalVariables(String string) {
		String variables = extractVariablePart(string);
		HashMap<String, Integer> global = new HashMap<String, Integer>();
		
		if(variables != null && !variables.isEmpty()){
			String[] split = variables.split(" ");
			Pattern variablePattern = Pattern.compile("\\s*(\\w+)=(\\d+)\\s*");
			
			for(String variable : split){
				Matcher variableMatcher = variablePattern.matcher(variable);
				if(variableMatcher.matches()){
					String name = variableMatcher.group(1);
					Integer value = Integer.parseInt(variableMatcher.group(2));
					global.put(name, value);
				}
			}
		}
		
		return global;
	}
	

	private static HashMap<String, HashMap<String, Integer>> parseLocalVariables(
			String string) {
		String variables = extractVariablePart(string);
		HashMap<String, HashMap<String, Integer>> local = new HashMap<String, HashMap<String, Integer>>();
		
		if(variables != null && !variables.isEmpty()){
			String[] split = variables.split(" ");
			Pattern variablePattern = Pattern.compile("\\s*" + AUTOMATA_LOCATION_PATTERN + "=(\\d+)\\s*");
			
			for(String variable : split){
				Matcher variableMatcher = variablePattern.matcher(variable);
				if(variableMatcher.matches()){
					String automata = variableMatcher.group(1);
					String name = variableMatcher.group(2);
					Integer value = Integer.parseInt(variableMatcher.group(3));
					
					if(!local.containsKey(automata)){
						local.put(automata, new HashMap<String, Integer>());
					}
					local.get(automata).put(name, value);
				}
			}
		}
		return local;
	}

	private static String extractClockPart(String string) {
		Pattern pattern = Pattern.compile("^(.*)#tau=\\d+(?:.*)?$");

		Matcher matcher = pattern.matcher(string);
		matcher.find();
		
		String variables = matcher.group(1);
		return variables.trim();
	}
	
	private static String extractVariablePart(String string) {
		Pattern pattern = Pattern.compile("^.* #tau=\\d+(.*)?$");

		Matcher matcher = pattern.matcher(string);
		matcher.find();
		
		String variables = matcher.group(1);
		return variables.trim();
	}
}
