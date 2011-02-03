package dk.aau.cs.model.tapn.simulation;

import java.math.BigDecimal;

import dk.aau.cs.model.TapaalTraceStep;

public class TimeDelayStep implements TapaalTraceStep {
	private BigDecimal delay;
	
	public TimeDelayStep(BigDecimal delay){
		this.delay = delay;
	}
}
