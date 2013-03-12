package dk.aau.cs.model.tapn.simulation;

import java.math.BigDecimal;

import dk.aau.cs.model.tapn.TimeInterval;
import dk.aau.cs.model.tapn.TimedTransition;

public interface DelayMode {
	BigDecimal GetDelay(TimedTransition transition, TimeInterval dInterval, BigDecimal delayGranularity);
}
