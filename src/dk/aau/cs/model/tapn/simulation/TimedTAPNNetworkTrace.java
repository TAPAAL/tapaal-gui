package dk.aau.cs.model.tapn.simulation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import dk.aau.cs.verification.VerifyTAPN.TraceType;

public class TimedTAPNNetworkTrace implements TAPNNetworkTrace {
	private List<TAPNNetworkTraceStep> steps = new ArrayList<TAPNNetworkTraceStep>();
	TraceType traceType;
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
        
        public List<TAPNNetworkTraceStep> getSteps(){
		return steps;
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
