package dk.aau.cs.model.tapn.simulation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TimedArcPetriNetTrace implements Iterable<TimedArcPetriNetStep> {
	private List<TimedArcPetriNetStep> steps = new ArrayList<TimedArcPetriNetStep>();
	private boolean isTimedTrace = true;
	
	public TimedArcPetriNetTrace(boolean isTimedTrace) {
		this.isTimedTrace = isTimedTrace;
	}

	public void add(TimedArcPetriNetStep step) {
		steps.add(step);
	}

	public Iterator<TimedArcPetriNetStep> iterator() {
		return steps.iterator();
	}

	public int length() {
		return steps.size();
	}

	public boolean isTimedTrace() {
		return isTimedTrace;
	}
}
