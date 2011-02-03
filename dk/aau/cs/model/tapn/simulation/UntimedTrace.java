package dk.aau.cs.model.tapn.simulation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class UntimedTrace implements TapaalTrace {
	private List<TapaalTraceStep> steps = new ArrayList<TapaalTraceStep>();
	
	public void add(TimedTransitionStep step){
		steps.add(step);
	}
		
	public Iterator<TapaalTraceStep> iterator() {
		return steps.iterator();
	}
	
	public int length(){
		return steps.size();
	}

	public boolean isConcreteTrace() {
		return false;
	}
}
