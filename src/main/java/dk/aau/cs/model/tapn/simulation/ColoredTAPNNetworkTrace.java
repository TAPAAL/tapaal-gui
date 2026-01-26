package dk.aau.cs.model.tapn.simulation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jetbrains.annotations.NotNull;

public class ColoredTAPNNetworkTrace implements TAPNNetworkTrace {
    private final List<TAPNNetworkTraceStep> steps = new ArrayList<TAPNNetworkTraceStep>();

    public void add(TAPNNetworkTraceStep step) {
		steps.add(step);
	}

    @Override
    public @NotNull Iterator<TAPNNetworkTraceStep> iterator() {
		return steps.iterator();
	}

    @Override
    public int length() {
		return steps.size();
	}

    @Override
	public boolean isConcreteTrace() {
		return true;
	}

    @Override
    public boolean isColoredTrace() {
        return true;
    }
}
