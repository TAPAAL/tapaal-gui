package dk.aau.cs.model.tapn;

import java.util.LinkedHashSet;
import java.util.regex.Pattern;

import dk.aau.cs.util.Require;

public class RealConstant implements SMCParameterConstant {
	private static final Pattern namePattern = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");

	private String name;
	private LinkedHashSet<Double> values;
	private boolean isUsed;

	public RealConstant(String name, double value) {
		setName(name);
		setValue(value);
		setIsUsed(false);
	}

	public RealConstant(String name, LinkedHashSet<Double> values) {
		setName(name);
		setValues(values);
		setIsUsed(false);
	}

	public void setName(String newName) {
		Require.that(newName != null && !newName.isEmpty(), "A constant must have a name");
		Require.that(namePattern.matcher(newName).matches(), "name must match regular expression [a-zA-Z_][a-zA-Z0-9_]*");
		name = newName;
	}

	public String name() {
		return name;
	}

	public void setValue(double value) {
		var singleValue = new LinkedHashSet<Double>();
		singleValue.add(value);
		setValues(singleValue);
	}

	public void setValues(LinkedHashSet<Double> values) {
		Require.that(values != null && !values.isEmpty(), "A real constant must have at least one value");
		this.values = values;
	}

	public boolean hasMultipleValues() {
		return values != null && values.size() > 1;
	}

	public double value() {
		if (values == null || values.isEmpty()) {
			throw new IllegalStateException("Constant has no values");
		}

		return values.iterator().next();
	}

	@Override
	public double paramValue() {
		return value();
	}

	public LinkedHashSet<Double> values() {
		if (values == null || values.isEmpty()) {
			throw new IllegalStateException("Constant has no values");
		}

		return values;
	}

	public boolean isUsed() {
		return isUsed;
	}

	public void setIsUsed(boolean isUsed) {
		this.isUsed = isUsed;
	}

	@Override
	public String toString() {
		if (values == null || values.isEmpty()) {
			return name + " = <no value>";
		}

		if (values.size() == 1) {
			return name + " = " + values.iterator().next();
		}

		var sb = new StringBuilder();
		sb.append(name).append(" = {");
		for (Double value : values) {
			sb.append(value).append(", ");
		}

		sb.delete(sb.length() - 2, sb.length());
		sb.append("}");

		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof RealConstant))
			return false;
		RealConstant other = (RealConstant) obj;
		return name != null && name.equals(other.name);
	}
}
