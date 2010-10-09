package dk.aau.cs.petrinet.trace;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TAPNTrace implements Iterable<TAPNFiringAction> {
	//private Marking initialMarking = null;
	private List<TAPNFiringAction> firingActions = new ArrayList<TAPNFiringAction>();
	
	public void addFiringAction(TAPNFiringAction firingAction) {
		firingActions.add(firingAction);
	}

	public Iterator<TAPNFiringAction> iterator() {
		return firingActions.iterator();
	}

	public int length() {
		return firingActions.size();
	}

}
