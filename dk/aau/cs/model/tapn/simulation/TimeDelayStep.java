package dk.aau.cs.model.tapn.simulation;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import pipe.gui.Pipe;
import dk.aau.cs.model.TapaalTraceStep;
import dk.aau.cs.model.tapn.NetworkMarking;

public class TimeDelayStep implements TapaalTraceStep {
	private BigDecimal delay;
	private static final DecimalFormat df = createDecimalFormat();
	
	public TimeDelayStep(BigDecimal delay){
		this.delay = delay;
	}

	public NetworkMarking performStepFrom(NetworkMarking marking) {
		return marking.delay(delay);
	}
	
	@Override
	public String toString() {
		return "TimeDelay: " + df.format(delay);
	}
	
	private static DecimalFormat createDecimalFormat(){
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(Pipe.AGE_DECIMAL_PRECISION);
		df.setMinimumFractionDigits(Pipe.AGE_DECIMAL_PRECISION);
		return df;
	}
}
