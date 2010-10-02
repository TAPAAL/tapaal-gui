package dk.aau.cs.TA.trace;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pipe.gui.Pipe;


public class SymbolicState {
	private static final String AUTOMATA_LOCATION_PATTERN = "([\\w\\(\\)]+)\\.(\\w+)";
	private HashMap<String, String> automataLocations = new HashMap<String, String>();
	private HashMap<String, HashMap<String, BigDecimal>> automataClockValues = new HashMap<String, HashMap<String,BigDecimal>>();

	public SymbolicState(HashMap<String,String> locations, HashMap<String, HashMap<String, BigDecimal>> clocks){
		this.automataLocations = locations;
		this.automataClockValues = clocks;
	}

	public static SymbolicState parse(String state){
		String[] stateLines = state.split("\n");

		HashMap<String,String> locations = parseLocations(stateLines[1]);
		HashMap<String, HashMap<String, BigDecimal>> clocks = parseAges(stateLines[2]);
		return new SymbolicState(locations, clocks);
	}

	private static HashMap<String, HashMap<String, BigDecimal>> parseAges(String string) {
		String[] split = string.split(" ");
		HashMap<String, HashMap<String, BigDecimal>> clocks = new HashMap<String, HashMap<String,BigDecimal>>(split.length-1);

		Pattern pattern = Pattern.compile(AUTOMATA_LOCATION_PATTERN + "=(\\d+)");
		for(int i = 0; i < split.length-1; i++){
			Matcher matcher = pattern.matcher(split[i]);

			if(matcher.find()){			
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

	public HashMap<String, BigDecimal> getClockValues(String automata) {
		return automataClockValues.get(automata);
	}
}
