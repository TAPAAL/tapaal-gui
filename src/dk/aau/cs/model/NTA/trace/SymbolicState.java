package dk.aau.cs.model.NTA.trace;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pipe.gui.Pipe;

public class SymbolicState {
	private static final String AUTOMATA_LOCATION_PATTERN = "([\\w\\(\\)]+)\\.(\\w+)";
	private final HashMap<String, String> automataLocations;
	private final HashMap<String, HashMap<String, ValueRange>> localClocksAndVariables;
	private final HashMap<String, ValueRange> globalClocksAndVariables;

	public SymbolicState(
			HashMap<String, String> locations,
			HashMap<String, HashMap<String, ValueRange>> localClocksAndVariables,
			HashMap<String, ValueRange> globalClocksAndVariables) {
		automataLocations = locations;
		this.localClocksAndVariables = localClocksAndVariables;
		this.globalClocksAndVariables = globalClocksAndVariables;
	}

	public HashMap<String, ValueRange> getLocalClocksAndVariablesFor(
			String automata) {
		return localClocksAndVariables.get(automata);
	}

	public ValueRange getLocalClockOrVariable(String automata,
			String colorVariableName) {
		return localClocksAndVariables.get(automata).get(colorVariableName);
	}

	public ValueRange globalClockOrVariableValue(String name) {
		return globalClocksAndVariables.get(name);
	}

	public String locationFor(String automata) {
		return automataLocations.get(automata);
	}

	public boolean isConcreteState() {
		for (ValueRange range : globalClocksAndVariables.values()) {
			if (!range.hasExactValue())
				return false;
		}

		for (HashMap<String, ValueRange> locals : localClocksAndVariables
				.values()) {
			for (ValueRange range : locals.values()) {
				if (!range.hasExactValue())
					return false;
			}
		}

		return true;
	}

	public static SymbolicState parse(String state) {
		String[] stateLines = state.split("\n");

		HashMap<String, String> locations = parseLocations(stateLines[1]);
		HashMap<String, HashMap<String, ValueRange>> localClocksAndVariables = parseLocalClocksAndVariables(stateLines[2]);
		HashMap<String, ValueRange> globalClocksAndVariables = parseGlobalClocksAndVariables(stateLines[2]);
		return new SymbolicState(locations, localClocksAndVariables,
				globalClocksAndVariables);
	}

	private static HashMap<String, String> parseLocations(String string) {
		String[] split = string.split(" ");
		HashMap<String, String> locations = new HashMap<String, String>(
				split.length - 2);

		Pattern pattern = Pattern.compile(AUTOMATA_LOCATION_PATTERN);
		for (int i = 1; i < split.length - 1; i++) {
			Matcher matcher = pattern.matcher(split[i]);
			matcher.find();
			String automata = matcher.group(1);
			String location = matcher.group(2);
			locations.put(automata, location);
		}

		return locations;
	}

	private static HashMap<String, HashMap<String, ValueRange>> parseLocalClocksAndVariables(
			String string) {
		String[] split = string.split(" ");
		HashMap<String, HashMap<String, ValueRange>> clocksAndVariables = new HashMap<String, HashMap<String, ValueRange>>();

		Pattern pattern = Pattern.compile(AUTOMATA_LOCATION_PATTERN
				+ "(<|<=|=|>=|>)(\\d+(?:\\.\\d+)?)");
		for (int i = 0; i < split.length; i++) {
			Matcher matcher = pattern.matcher(split[i].replace(",", "").trim());

			if (matcher.matches()) {
				String automata = matcher.group(1);
				String clockOrVariable = matcher.group(2);
				String operator = matcher.group(3);
				double value = Double.parseDouble(matcher.group(4));

				boolean isLower = operator.equals("<") || operator.equals("<=")
						|| operator.equals("=");
				boolean isUpper = operator.equals(">") || operator.equals(">=")
						|| operator.equals("=");

				if (!clocksAndVariables.containsKey(automata)) {
					clocksAndVariables.put(automata,
							new HashMap<String, ValueRange>());
				}

				HashMap<String, ValueRange> locals = clocksAndVariables
						.get(automata);

				ValueRange range = locals.containsKey(clockOrVariable) ? locals
						.get(clockOrVariable) : new ValueRange();
				if (isLower) {
					range.setLower(new BigDecimal(value, new MathContext(
							Pipe.AGE_DECIMAL_PRECISION)));
					range.setLowerIncluded(operator.equals("<=")
							|| operator.equals("="));
				}

				if (isUpper) {
					range.setUpper(new BigDecimal(value, new MathContext(
							Pipe.AGE_DECIMAL_PRECISION)));
					range.setUpperIncluded(operator.equals(">=")
							|| operator.equals("="));
				}

				if (!locals.containsKey(clockOrVariable)) {
					locals.put(clockOrVariable, range);
				}
			}
		}

		return clocksAndVariables;
	}

	private static HashMap<String, ValueRange> parseGlobalClocksAndVariables(
			String string) {
		String[] split = string.split(" ");
		HashMap<String, ValueRange> global = new HashMap<String, ValueRange>();

		Pattern pattern = Pattern
				.compile("(\\w+)(<|<=|=|>=|>)(\\d+(?:\\.\\d+)?)");

		for (String variable : split) {
			Matcher matcher = pattern.matcher(variable.trim());
			if (matcher.matches()) {
				String name = matcher.group(1);
				String operator = matcher.group(2);
				double valueAsDouble = Double.parseDouble(matcher.group(3));

				boolean isLower = operator.equals("<") || operator.equals("<=")
						|| operator.equals("=");
				boolean isUpper = operator.equals(">") || operator.equals(">=")
						|| operator.equals("=");

				ValueRange range = global.containsKey(name) ? global.get(name)
						: new ValueRange();
				if (isLower) {
					range.setLower(new BigDecimal(valueAsDouble,
							new MathContext(Pipe.AGE_DECIMAL_PRECISION)));
					range.setLowerIncluded(operator.equals("<=")
							|| operator.equals("="));
				}

				if (isUpper) {
					range.setUpper(new BigDecimal(valueAsDouble,
							new MathContext(Pipe.AGE_DECIMAL_PRECISION)));
					range.setUpperIncluded(operator.equals(">=")
							|| operator.equals("="));
				}

				if (!global.containsKey(name)) {
					global.put(name, range);
				}
			}
		}

		return global;
	}
}
