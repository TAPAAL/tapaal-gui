package dk.aau.cs.petrinet.trace;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import dk.aau.cs.petrinet.trace.TimeDelayFiringAction;

public class TAPNTrace implements Iterable<TAPNFiringAction> {
	//private Marking initialMarking = null;
	private List<TAPNFiringAction> firingActions = new ArrayList<TAPNFiringAction>();
	private final boolean untimedTrace;
	
	public TAPNTrace(){
		this(false);
	}
	
	public TAPNTrace(boolean untimed){
		this.untimedTrace = untimed;
	}
	
	public boolean addFiringAction(TAPNFiringAction firingAction) {
		if(!untimedTrace || !(firingAction instanceof TimeDelayFiringAction)){
			firingActions.add(firingAction);
			return true;
		}
		
		return false;
	}

	public Iterator<TAPNFiringAction> iterator() {
		return firingActions.iterator();
	}

	public int length() {
		return firingActions.size();
	}
}
