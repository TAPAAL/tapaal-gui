package dk.aau.cs.model.NTA.trace;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class UppaalTrace implements Iterable<TAFiringAction> {
	private List<TAFiringAction> firingActions;
	private List<SymbolicState> states;

	public UppaalTrace() {
		firingActions = new ArrayList<TAFiringAction>();
		states = new ArrayList<SymbolicState>();
	}

	public void addState(SymbolicState state) {
		states.add(state);
	}

	public void addFiringAction(TAFiringAction action) {
		firingActions.add(action);
	}

	public Iterator<TAFiringAction> iterator() {
		return firingActions.iterator();
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer("Trace:\n");
		for (int i = 0; i < states.size() - 1; i++) {
			buffer.append(states.get(i).toString());
			buffer.append("\n\n");
			buffer.append(firingActions.get(i).toString());
			buffer.append("\n\n");
		}

		buffer.append(states.get(states.size() - 1).toString());

		return buffer.toString();
	}

	public int length() {
		return firingActions.size();
	}

	public int numberOfStates() {
		return states.size();
	}

	public boolean isEmpty() {
		return length() == 0 && numberOfStates() == 0;
	}

	public List<SymbolicState> getStates() {
		return states;
	}

	public boolean isConcreteTrace() {
		for (SymbolicState state : states) {
			if (!state.isConcreteState()) {
				return false;
			}
		}
		return true;
	}
}
