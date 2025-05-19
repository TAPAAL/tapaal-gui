package dk.aau.cs.model.tapn.simulation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import dk.aau.cs.model.tapn.TimedToken;
import dk.aau.cs.util.Require;
import dk.aau.cs.verification.VerifyTAPN.TraceType;
import org.jetbrains.annotations.NotNull;

public class TimedArcPetriNetTrace implements Iterable<PetriNetStep> {
	private boolean nextIsLoop;	
	private int loopToIndex = -1;
	private TraceType traceType;
	private String traceName;
    private boolean isColoredTrace;

	private final List<PetriNetStep> steps = new ArrayList<PetriNetStep>();
	private final boolean isTimedTrace;

    public TimedArcPetriNetTrace(boolean isTimedTrace, String traceName) {
        this.isTimedTrace = isTimedTrace;
        traceType = TraceType.NOT_EG;
        this.traceName = traceName;
    }
	
	public TimedArcPetriNetTrace(boolean isTimedTrace) {
		this.isTimedTrace = isTimedTrace;
		traceType = TraceType.NOT_EG;
		this.traceName = "";
	}

	public void add(PetriNetStep step) {
        if (step instanceof ColoredTransitionStep) {
            isColoredTrace = true;
        }

		if(nextIsLoop){
			Require.that(loopToIndex == -1, "There can only be one step to loop to in a trace");
			loopToIndex = steps.size();
			nextIsLoop = false;
		}
		steps.add(step);
	}

	public String getTraceName() {
        return this.traceName;
    }

    public void setTraceName(String traceName) {
        this.traceName = traceName;
    }
	public @NotNull Iterator<PetriNetStep> iterator() {
		return steps.iterator();
	}

	public int length() {
		return steps.size();
	}

    public boolean isEmpty() {
        return steps.isEmpty();
    }

    public PetriNetStep getLastStep() {
        if (steps.isEmpty()) {
            return null;
        }

        return steps.get(steps.size() - 1);
    }

	public boolean isTimedTrace() {
		return isTimedTrace;
	}

    public boolean isColoredTrace() {
        return isColoredTrace;
    }
	
	public void nextIsLoop(){
		nextIsLoop = true;
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
		for (PetriNetStep step : steps){
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
		for (PetriNetStep step : steps){
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
