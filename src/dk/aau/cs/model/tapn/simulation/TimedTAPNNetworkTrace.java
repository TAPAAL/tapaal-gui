package dk.aau.cs.model.tapn.simulation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TimedTAPNNetworkTrace implements TAPNNetworkTrace {
	private List<TAPNNetworkTraceStep> steps = new ArrayList<TAPNNetworkTraceStep>();

	private int loopToIndex;
	
	public TimedTAPNNetworkTrace(int loopToIndex) {
		this.loopToIndex = loopToIndex;
	}
	
	public void add(TAPNNetworkTraceStep step) {
		if(steps.size() == loopToIndex){
			step.setLoopStep();
		}
		steps.add(step);
	}

	public Iterator<TAPNNetworkTraceStep> iterator() {
		return steps.iterator();
	}

	public int length() {
		return steps.size();
	}

	public boolean isConcreteTrace() {
		return true;
	}
	
	public List<TAPNNetworkTraceStep> getLoopSteps(){
		return steps.subList(loopToIndex, steps.size());
	}
	
	public int getLoopToIndex(){
		return loopToIndex;
	}
}
