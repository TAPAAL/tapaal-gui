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
	private final HashMap<String, HashMap<String, BigDecimal>> localVariables;
	private final HashMap<String, BigDecimal> globalVariables;
	
	public SymbolicState(HashMap<String, String> automataLocations, 
			HashMap<String, HashMap<String, BigDecimal>> localVariables, 
			HashMap<String, BigDecimal> globalVariables){
		this.automataLocations = automataLocations;
		this.localVariables = localVariables;
		this.globalVariables = globalVariables;
	}
	
	public BigDecimal getLocalClockOrVariable(String automata,
			String variableName) {
		return localVariables.get(automata).get(variableName);
	}
	
	public BigDecimal globalClockOrVariableValue(String name){
		return globalVariables.get(name);
	}
	
	public static SymbolicState parse(String element) {
		String[] split = element.split("\n");
		
		HashMap<String, String> locations = parseLocations(split[0]);
		HashMap<String, HashMap<String, BigDecimal>> localVariables = parseLocalVariables(split[1]); 
		HashMap<String, BigDecimal> globalVariables = parseGlobalVariables(split[1]);
		
		return new SymbolicState(locations, localVariables, globalVariables);
	}

	private static HashMap<String, HashMap<String, BigDecimal>> parseLocalVariables(
			String string) {
		String[] split = string.replace(",", "").split(" ");
		HashMap<String, HashMap<String, BigDecimal>> locals = new HashMap<String, HashMap<String,BigDecimal>>();

		Pattern pattern = Pattern.compile(AUTOMATA_LOCATION_PATTERN + "=(\\d+)");
		for(int i = 0; i < split.length; i++){
			Matcher matcher = pattern.matcher(split[i].trim());

			if(matcher.matches()){			
				String automata = matcher.group(1);
				String clockOrVariable = matcher.group(2);
				double value = Double.parseDouble(matcher.group(3));

				if(!locals.containsKey(automata)){
					locals.put(automata, new HashMap<String, BigDecimal>());
				}

				locals.get(automata).put(clockOrVariable, new BigDecimal(value, new MathContext(Pipe.AGE_DECIMAL_PRECISION)));
			}
		}

		return locals;
	}

	private static HashMap<String, BigDecimal> parseGlobalVariables(
			String string) {
		String[] split = string.replace(",", "").split(" ");
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

	private static HashMap<String, String> parseLocations(String locationsString) {
		String[] split = locationsString.split(" ");
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

	public String getLocationFor(String automata) {
		return automataLocations.get(automata);
	}
}
