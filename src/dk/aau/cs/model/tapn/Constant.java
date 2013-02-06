package dk.aau.cs.model.tapn;

import java.util.regex.Pattern;

import dk.aau.cs.util.Require;

public class Constant {
	private static final Pattern namePattern = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");
	
	private String name;
	private int value;
	private int lowerBound;
	private int upperBound;
	private boolean isUsed;
	private boolean isFocused;

	public Constant(String name, int value) {
		setName(name);
		setValue(value);
		setIsUsed(false);
		setFocused(false);
		reset();
	}

	public Constant(Constant constant) {
		Require.that(constant != null, "Constant cannot be null");

		name = constant.name;
		value = constant.value;
		lowerBound = constant.lowerBound;
		upperBound = constant.upperBound;
		isUsed = constant.isUsed;
		isFocused = constant.isFocused;
	}

	public void setName(String newName) {
		Require.that(newName != null && !newName.isEmpty(), "A constant must have a name");
		Require.that(isValid(newName), "name must match regular expression [a-zA-Z_][a-zA-Z0-9_]*");
		name = newName;
	}

	private boolean isValid(String newName) {
		return namePattern.matcher(newName).matches();
	}
	
	public boolean hasFocus(){
		return isFocused;
	}
	
	public void setFocused(boolean focused){
		isFocused = focused;
	}

	public String name() {
		return name;
	}

	public void setValue(int value) {
		Require.that(value >= 0, "value must be non-negative");
		this.value = value;
	}

	public int value() {
		return value;
	}


	public int lowerBound() {
		return lowerBound;
	}

	public void setLowerBound(int value) {
		if (value > lowerBound) {
			lowerBound = value;
		}
	}

	public int upperBound() {
		return upperBound;
	}

	public void setUpperBound(int value) {
		if (value >= 0 && value < upperBound) {
			upperBound = value;
		}
	}

	public boolean isUsed() {
		return isUsed;
	}

	public void setIsUsed(boolean isUsed) {
		this.isUsed = isUsed;

	}

	public void reset() {
		lowerBound = 0;
		upperBound = Integer.MAX_VALUE;
		isUsed = false;
	}

	public Constant copy() {
		return new Constant(this);
	}

	@Override
	public String toString() {
		return name + " = " + value;
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
		if (obj == null)
			return false;
		if (!(obj instanceof Constant))
			return false;
		Constant other = (Constant) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
