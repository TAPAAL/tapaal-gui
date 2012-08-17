package dk.aau.cs.model.tapn.simulation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import dk.aau.cs.verification.VerifyTAPN.TraceType;

public class TimedArcPetriNetTrace implements Iterable<TimedArcPetriNetStep> {
	private boolean nextIsLoop;	
	private int loopToIndex = -1;
	private TraceType traceType;
	
	
	private List<TimedArcPetriNetStep> steps = new ArrayList<TimedArcPetriNetStep>();
	private boolean isTimedTrace = true;
	
	public TimedArcPetriNetTrace(boolean isTimedTrace) {
		this.isTimedTrace = isTimedTrace;
		traceType = TraceType.NOT_EG;
	}

	public void add(TimedArcPetriNetStep step) {
		if(nextIsLoop){
			assert(loopToIndex == -1); //There can only be one step to loop to in a trace
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
	
	public TraceType getTraceType(){
		return traceType;
	}
	
	public void setTraceType(TraceType traceType){
		this.traceType = traceType;
	}
}
