package dk.aau.cs.TA.trace;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class UppaalTrace implements Iterable<TAFiringAction> {
	private List<TAFiringAction> firingActions;
	private List<ConcreteState> concreteStates;
	
	public UppaalTrace(){
		firingActions = new ArrayList<TAFiringAction>();
		concreteStates = new ArrayList<ConcreteState>();
	}

	public void addConcreteState(ConcreteState state){
		concreteStates.add(state);
	}
	
	public void addFiringAction(TAFiringAction action){
		firingActions.add(action);
	}
	
	public Iterator<TAFiringAction> iterator() {
		return firingActions.iterator();
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer("Trace:\n");
		for(int i = 0; i < concreteStates.size()-1; i++){
			buffer.append(concreteStates.get(i).toString());
			buffer.append("\n\n");
			buffer.append(firingActions.get(i).toString());
			buffer.append("\n\n");
		}
		
		buffer.append(concreteStates.get(concreteStates.size()-1).toString());
				
		return buffer.toString();
	}

	public int length() {
		return firingActions.size();
	}

	public int numberOfStates() {
		return concreteStates.size();
	}

	public boolean isEmpty() {
		return length() == 0 && numberOfStates() == 0;
	}
}
