package dk.aau.cs.model.tapn;

import dk.aau.cs.util.Require;

public class IntBound implements Bound {
	private int bound;

	public IntBound(int bound) {
		Require.that(bound >= 0, "Integer bounds must be non-negative.");
		this.bound = bound;
	}

	public IntBound(IntBound bound) {
		Require.that(bound != null, "Integer bound cannot be null");

		this.bound = bound.bound;
	}

	public int value() {
		return bound;
	}


	public IntBound copy() {
		return new IntBound(this);
	}

	@Override
	public String toString() {
		return Integer.toString(bound);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + bound;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof IntBound))
			return false;
		IntBound other = (IntBound) obj;
		if (bound != other.bound)
			return false;
		return true;
	}
}
