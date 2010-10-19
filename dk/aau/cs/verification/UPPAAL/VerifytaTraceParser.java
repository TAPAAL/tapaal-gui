package dk.aau.cs.verification.UPPAAL;

import java.io.BufferedReader;
import java.io.IOException;

import dk.aau.cs.TA.trace.ConcreteState;
import dk.aau.cs.TA.trace.TimeDelayFiringAction;
import dk.aau.cs.TA.trace.TransitionFiringAction;
import dk.aau.cs.TA.trace.UppaalTrace;

public class VerifytaTraceParser {
	public UppaalTrace parseTrace(BufferedReader reader){
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
					trace.addSymbolicState(state);
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
		
		return trace.length() == 0 ? null : trace;
	}
}
