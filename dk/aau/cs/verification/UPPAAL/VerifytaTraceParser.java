package dk.aau.cs.verification.UPPAAL;

import java.io.BufferedReader;
import java.io.IOException;

import dk.aau.cs.TA.trace.ConcreteState;
import dk.aau.cs.TA.trace.SymbolicState;
import dk.aau.cs.TA.trace.TimeDelayFiringAction;
import dk.aau.cs.TA.trace.TransitionFiringAction;
import dk.aau.cs.TA.trace.UntimedUppaalTrace;
import dk.aau.cs.TA.trace.UppaalTrace;

public class VerifytaTraceParser {
	public UppaalTrace parseTimedTrace(BufferedReader reader){
		UppaalTrace trace = new UppaalTrace();
		try {			
			String line;
			ConcreteState previousState = null;
			TransitionFiringAction previousTransitionFiring = null;
			while(reader.ready()){
				StringBuffer buffer = new StringBuffer();
				while((line = reader.readLine()) != null && !line.isEmpty()){
					buffer.append(line);
					buffer.append("\n");
				}
				
				String element = buffer.toString();
				
				if(line == null && element.isEmpty()) break; // we are done parsing trace, exit outer loop
				
				
				if(element.contains("State:\n")){ 
					ConcreteState state = ConcreteState.parse(element);
					trace.addConcreteState(state);
					previousState = state;
					if(previousTransitionFiring != null){
						previousTransitionFiring.setTargetState(state);
						previousTransitionFiring = null;
					}					
				}else if(element.contains("Delay:")){
					TimeDelayFiringAction delay = TimeDelayFiringAction.parse(previousState, element);
					trace.addFiringAction(delay);
				}else if(element.contains("Transitions:")){
					TransitionFiringAction transition = TransitionFiringAction.parse(previousState, element);
					trace.addFiringAction(transition);
					previousTransitionFiring = transition;
				}
			}
		} catch (IOException e) {
			return null;
		}
		
		return trace.isEmpty() ? null : trace;
	}
	
	
	public UntimedUppaalTrace parseUntimedTrace(BufferedReader reader){
		UntimedUppaalTrace trace = new UntimedUppaalTrace();
		try {			
			String line;
			SymbolicState previousState = null;
			TransitionFiringAction previousTransitionFiring = null;
			boolean nextIsState = false;
			while(reader.ready()){
				StringBuffer buffer = new StringBuffer();
				while((line = reader.readLine()) != null && !line.isEmpty()){
					buffer.append(line);
					buffer.append("\n");
				}
				
				String element = buffer.toString();
				
				if(line == null && element.isEmpty()) break; // we are done parsing trace, exit outer loop
				
				if(nextIsState){
					SymbolicState state = SymbolicState.parse(element);
					trace.addSymbolicState(state);
					previousState = state;
//					if(previousTransitionFiring != null){
//						previousTransitionFiring.setTargetState(state);
//						previousTransitionFiring = null;
//					}	
					nextIsState = false;
				}else if(element.contains("State:\n")){ 
					nextIsState = true;				
				}else if(element.contains("Transitions:")){
//					TransitionFiringAction transition = TransitionFiringAction.parse(previousState, element);
//					trace.addFiringAction(transition);
//					previousTransitionFiring = transition;
				}
			}
		} catch (IOException e) {
			return null;
		}
		
		return trace.isEmpty() ? null : trace;
	}
}
