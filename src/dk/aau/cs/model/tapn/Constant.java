package dk.aau.cs.model.tapn;

import dk.aau.cs.util.Require;

public class Constant {
	private String name;
	private int value;
	private int lowerBound;
	private int upperBound;
	private boolean isUsed;

	public Constant(String name, int value) {
		setName(name);
		setValue(value);
		setIsUsed(false);
		reset();
	}

	public Constant(Constant constant) {
		Require.that(constant != null, "Constant cannot be null");

		this.name = constant.name;
		this.value = constant.value;
		this.lowerBound = constant.lowerBound;
		this.upperBound = constant.upperBound;
		this.isUsed = constant.isUsed;
	}

	public void setName(String name) {
		Require.that(name != null && !name.isEmpty(),
				"A constant must have a name");
		this.name = name;
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
