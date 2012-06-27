package dk.aau.cs.model.tapn.simulation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TimedArcPetriNetTrace implements Iterable<TimedArcPetriNetStep> {
	private boolean nextIsLoop;
	
	int loopToIndex = -1;
	
	private List<TimedArcPetriNetStep> steps = new ArrayList<TimedArcPetriNetStep>();
	private boolean isTimedTrace = true;
	
	public TimedArcPetriNetTrace(boolean isTimedTrace) {
		this.isTimedTrace = isTimedTrace;
	}

	public void add(TimedArcPetriNetStep step) {
		if(nextIsLoop){
			assert(loopToIndex == -1); //There can only be one place to loop to in a trace
			loopToIndex = steps.size();
			nextIsLoop = false;
		}
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
	
	public void nextIsLoop(){
		nextIsLoop = true;
	}
	
	public List<TimedArcPetriNetStep> getLoopSteps(){
		return steps.subList(loopToIndex, steps.size());
	}
	
	public int getLoopToIndex(){
		return loopToIndex;
	}
}
