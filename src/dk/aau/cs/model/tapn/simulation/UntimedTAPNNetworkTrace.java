package dk.aau.cs.model.tapn.simulation;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class UntimedTAPNNetworkTrace implements TAPNNetworkTrace {
	private final List<TAPNNetworkTraceStep> steps = new ArrayList<TAPNNetworkTraceStep>();

	public void add(TAPNNetworkTimedTransitionStep step) {
		steps.add(step);
	}

	public @NotNull Iterator<TAPNNetworkTraceStep> iterator() {
		return steps.iterator();
	}

	public int length() {
		return steps.size();
	}

	public boolean isConcreteTrace() {
		return false;
	}
}
