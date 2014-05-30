package dk.aau.cs.model.tapn.simulation;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import pipe.gui.Pipe;
import dk.aau.cs.model.tapn.LocalTimedMarking;

public class TimeDelayStep implements TimedArcPetriNetStep {
	private BigDecimal delay;
	private static final DecimalFormat df = createDecimalFormat();

	public TimeDelayStep(BigDecimal delay) {
		this.delay = delay;
	}

	public LocalTimedMarking performStepFrom(LocalTimedMarking marking) {
		return marking.delay(delay);
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
		df.setMaximumFractionDigits(Pipe.AGE_DECIMAL_PRECISION);
		df.setMinimumFractionDigits(Pipe.AGE_DECIMAL_PRECISION);
		return df;
	}
}
