package dk.aau.cs.model.tapn.simulation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import dk.aau.cs.model.TapaalTrace;
import dk.aau.cs.model.TapaalTraceStep;

public class TimedTrace implements TapaalTrace {
	private List<TapaalTraceStep> steps = new ArrayList<TapaalTraceStep>();
	
	public void add(TapaalTraceStep step){
		steps.add(step);
	}
		
	public Iterator<TapaalTraceStep> iterator() {
		return steps.iterator();
	}
	
	public int length(){
		return steps.size();
	}
}
