package dk.aau.cs.model.tapn;

import dk.aau.cs.util.Require;

public class ConstantBound implements Bound {
	private Constant constant;

	public ConstantBound(Constant constant) {
		Require.that(constant != null, "Argument must be a non-null constant");
		this.constant = constant;
	}

	public int value() {
		return constant.value();
	}

	public String name() {
		return constant.name();
	}

	public ConstantBound copy() {
		return new ConstantBound(constant.copy());
	}


	public Constant constant() {
		return constant;
	}
	
	public void setConstant(Constant newConstant) {
		Require.that(newConstant != null, "Constant cannot be null");

		constant = newConstant;
	}

	public String toString() {
		return constant.name();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((constant == null) ? 0 : constant.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ConstantBound))
			return false;
		ConstantBound other = (ConstantBound) obj;
		if (constant == null) {
			if (other.constant != null)
				return false;
		} else if (!constant.equals(other.constant))
			return false;
		return true;
	}
}
