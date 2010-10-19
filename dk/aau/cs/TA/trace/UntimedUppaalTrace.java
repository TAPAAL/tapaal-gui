package dk.aau.cs.TA.trace;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class UntimedUppaalTrace implements Iterable<TransitionFiringAction> {
	private List<TransitionFiringAction> firingActions;
	private List<SymbolicState> symbolicStates;
	
	public UntimedUppaalTrace(){
		firingActions = new ArrayList<TransitionFiringAction>();
		symbolicStates = new ArrayList<SymbolicState>();
	}

	public void addSymbolicState(SymbolicState state){
		symbolicStates.add(state);
	}
	
	public void addFiringAction(TransitionFiringAction action){
		firingActions.add(action);
	}
	
	public Iterator<TransitionFiringAction> iterator() {
		return firingActions.iterator();
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer("Trace:\n");
		for(int i = 0; i < symbolicStates.size()-1; i++){
			buffer.append(symbolicStates.get(i).toString());
			buffer.append("\n\n");
			buffer.append(firingActions.get(i).toString());
			buffer.append("\n\n");
		}
		
		buffer.append(symbolicStates.get(symbolicStates.size()-1).toString());
				
		return buffer.toString();
	}

	public int length() {
		return firingActions.size();
	}
	
	private int numberOfStates() {
		return symbolicStates.size();
	}

	public List<SymbolicState> symbolicStates() {
		return symbolicStates;
	}

	public boolean isEmpty() {
		return length() == 0 && numberOfStates() == 0;
	}
}
