package dk.aau.cs.model.tapn.simulation;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import pipe.gui.Constants;

public class TimeDelayStep implements PetriNetStep {
	private BigDecimal delay;
	private static final DecimalFormat df = createDecimalFormat();

	public TimeDelayStep(BigDecimal delay) {
		this.delay = delay;
	}
	
	public BigDecimal delay() {
		return delay;
	}
	
	public void setDelay(BigDecimal delay) {
		this.delay = delay;
	}

	@Override
	public String toString() {
		return "TimeDelay: " + df.format(delay);
	}

	private static DecimalFormat createDecimalFormat() {
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(Constants.AGE_DECIMAL_PRECISION);
		df.setMinimumFractionDigits(Constants.AGE_DECIMAL_PRECISION);
		return df;
	}
}
