package dk.aau.cs.model.tapn.simulation;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import pipe.gui.Pipe;
import dk.aau.cs.model.tapn.NetworkMarking;

public class TAPNNetworkTimeDelayStep implements TAPNNetworkTraceStep {
	private BigDecimal delay;

	public TAPNNetworkTimeDelayStep(BigDecimal delay) {
		this.delay = delay;
	}

	public NetworkMarking performStepFrom(NetworkMarking marking) {
		return marking.delay(delay);
	}

	@Override
	public String toString() {
		return "TimeDelay: " + delay;
	}
}
