package dk.aau.cs.model.tapn;

import java.math.BigDecimal;
import java.math.MathContext;

import pipe.gui.Pipe;

import dk.aau.cs.util.Require;

public class RatBound implements Bound {
	private BigDecimal bound;

	public RatBound(BigDecimal bound) {
		Require.that(bound.compareTo(new BigDecimal(-1, new MathContext(Pipe.AGE_PRECISION))) >= 0, "Rational bounds must be non-negative or -1.");
		this.bound = bound;
	}

	public RatBound(RatBound bound) {
		Require.that(bound != null, "Rational bound cannot be null");

		this.bound = bound.bound;
	}
	
	public int value() {
		return bound.intValue();
	}
	
	public BigDecimal getBound(){
		return bound;
	}

	@Override
	public String toString() {
		return bound.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + bound.intValue();
		return result;
	}
	
	/*
	 * Comparison of bounds, resulting in -1 if this is lesser, 0 if they are equal and 1 if this is greater.
	 */
	public int compareTo(RatBound b){
		if(this.getBound().compareTo(BigDecimal.ZERO) == -1){
			if(this.getBound().compareTo(b.getBound()) == 0){
				return 0;
			} else {
				return 1;
			}
		} else if(b.getBound().compareTo(BigDecimal.ZERO) == -1){
			return -1;
		} else {
			return this.getBound().compareTo(b.getBound());
		}
	}

	public RatBound copy() {
		return new RatBound(this);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof RatBound))
			return false;
		RatBound other = (RatBound) obj;
		if (bound != other.bound)
			return false;
		return true;
	}
}
