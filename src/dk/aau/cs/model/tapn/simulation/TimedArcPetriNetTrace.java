package dk.aau.cs.model.tapn.simulation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import dk.aau.cs.model.tapn.TimedOutputArc;
import dk.aau.cs.model.tapn.TimedToken;
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
	
	public void reduceTraceForOriginalNet(String matchForTransition, String matchTokenRemoval) {
		for (TimedArcPetriNetStep step : steps){
			if (step instanceof TimedTransitionStep) {
				if (((TimedTransitionStep) step).transition().name().contains(matchForTransition)) {
					((TimedTransitionStep) step).transition().setName(((TimedTransitionStep) step).transition().name().substring(0, ((TimedTransitionStep) step).transition().name().indexOf(matchForTransition)));
				}
				if(((TimedTransitionStep) step).consumedTokens() != null){
					for (TimedToken token : ((TimedTransitionStep) step).consumedTokens()){
						if(token.place().name().contains(matchTokenRemoval)){
							((TimedTransitionStep) step).consumedTokens().remove(token);
							break;
						}
					}
				}
			}
		}
	}
	
	public void removeTokens(String matchTokenRemoval){
		for (TimedArcPetriNetStep step : steps){
			if (step instanceof TimedTransitionStep){
				if(((TimedTransitionStep) step).consumedTokens() != null){
					for (TimedToken token : ((TimedTransitionStep) step).consumedTokens()){
						if(token.place().name().contains(matchTokenRemoval)){
							((TimedTransitionStep) step).consumedTokens().remove(token);
							break;
						}
					}
				}
			}
		}
	}
}
