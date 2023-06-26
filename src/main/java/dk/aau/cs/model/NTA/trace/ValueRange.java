package dk.aau.cs.model.NTA.trace;

import java.math.BigDecimal;

public class ValueRange {
	private boolean lowerStrict = false;
	private BigDecimal lower = BigDecimal.ZERO;
	private BigDecimal upper = new BigDecimal(Long.MAX_VALUE);
	private boolean upperStrict = false;

	public ValueRange() {

	}

	public ValueRange(boolean lowerStrict, BigDecimal lower, BigDecimal upper,
			boolean upperStrict) {
		this.lowerStrict = lowerStrict;
		this.lower = lower;
		this.upper = upper;
		this.upperStrict = upperStrict;
	}

	public boolean hasExactValue() {
		return lower.equals(upper);
	}

	public boolean lowerIncluded() {
		return !lowerStrict;
	}

	public BigDecimal lower() {
		return lower;
	}

	public BigDecimal upper() {
		return upper;
	}

	public boolean upperIncluded() {
		return !upperStrict;
	}

	public void setLowerIncluded(boolean included) {
		lowerStrict = !included;
	}

	public void setLower(BigDecimal lower) {
		this.lower = lower;
	}

	public void setUpper(BigDecimal upper) {
		this.upper = upper;
	}

	public void setUpperIncluded(boolean included) {
		upperStrict = !included;
	}
}
