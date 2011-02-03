package dk.aau.cs.verification.UPPAAL;

import java.io.BufferedReader;
import java.io.IOException;

import dk.aau.cs.TA.trace.SymbolicState;
import dk.aau.cs.TA.trace.TimeDelayFiringAction;
import dk.aau.cs.TA.trace.TransitionFiring;
import dk.aau.cs.TA.trace.UppaalTrace;

public class VerifytaTraceParser {
	public UppaalTrace parseTrace(BufferedReader reader) {
		UppaalTrace trace = new UppaalTrace();
		try {
			String line;
			SymbolicState previousState = null;
			TransitionFiring previousTransitionFiring = null;
			boolean nextIsState = false;
			while (reader.ready()) {
				StringBuffer buffer = new StringBuffer();
				while ((line = reader.readLine()) != null && !line.isEmpty()) {
					buffer.append(line);
					buffer.append("\n");
				}

				String element = buffer.toString();

				if (line == null && element.isEmpty())
					break; // we are done parsing trace, exit outer loop

				if (nextIsState) { // untimed trace
					SymbolicState state = SymbolicState.parse("State:\n"
							+ element);
					trace.addState(state);
					previousState = state;
					if (previousTransitionFiring != null) {
						previousTransitionFiring.setTargetState(state);
						previousTransitionFiring = null;
					}
					nextIsState = false;
				} else if (element.contains("State\n")) { // untimed trace
					nextIsState = true;
				} else if (element.contains("State:\n")) { // timed trace
					SymbolicState state = SymbolicState.parse(element);
					trace.addState(state);
					previousState = state;
					if (previousTransitionFiring != null) {
						previousTransitionFiring.setTargetState(state);
						previousTransitionFiring = null;
					}
				} else if (element.contains("Delay:")) {
					TimeDelayFiringAction delay = TimeDelayFiringAction.parse(
							previousState, element);
					trace.addFiringAction(delay);
				} else if (element.contains("Transitions:")) {
					TransitionFiring transition = TransitionFiring.parse(
							previousState, element);
					trace.addFiringAction(transition);
					previousTransitionFiring = transition;
				}
			}
		} catch (IOException e) {
			return null;
		}

		return trace.isEmpty() ? null : trace;
	}
}
