package dk.aau.cs.verification.UPPAAL;

import java.io.BufferedReader;
import java.io.IOException;

import dk.aau.cs.TA.trace.SymbolicState;
import dk.aau.cs.TA.trace.TimeDelayFiringAction;
import dk.aau.cs.TA.trace.TransitionFiringAction;
import dk.aau.cs.TA.trace.UppaalTrace;
import dk.aau.cs.verification.Trace;

public class VerifytaTraceParser {
	public Trace[] parseTrace(BufferedReader reader){
		UppaalTrace trace = new UppaalTrace();
		try {			
			String line;
			while(reader.ready()){
				StringBuffer buffer = new StringBuffer();
				while((line = reader.readLine()) != null && !line.isEmpty()){
					buffer.append(line);
					buffer.append("\n");
				}
				
				if(line == null) break; // we are done parsing trace, exit outer loop
				
				String element = buffer.toString();
				if(element.contains("State:\n")){ // TODO: Two states in a row, indicates start of new trace (for next query)
					SymbolicState state = SymbolicState.parse(element);
					trace.addSymbolicState(state);
				}else if(element.contains("Delay:")){
					TimeDelayFiringAction delay = TimeDelayFiringAction.parse(element);
					trace.addFiringAction(delay);
				}else if(element.contains("Transitions:")){
					TransitionFiringAction transition = TransitionFiringAction.parse(element);
					trace.addFiringAction(transition);
				}
			}
		} catch (IOException e) {
			return null;
		}
		
		return new Trace[] { trace };
	}
}
