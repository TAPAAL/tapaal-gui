package dk.aau.cs.model.tapn;

import dk.aau.cs.util.Require;

public class IntBound implements Bound {
	private int bound;

	public IntBound(int bound){
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
	
	@Override
	public String toString() {
		return Integer.toString(bound);
	}

	@Override
	public IntBound copy() {
		return new IntBound(this);
	}
}
