package pipe.dataLayer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import pipe.dataLayer.simulation.Marking;

public class TAPNTrace implements Iterable<FiringAction> {
	private Marking initialMarking = null;
	private List<FiringAction> firingActions = new ArrayList<FiringAction>();
	
	public void addFiringAction(FiringAction firingAction) {
		firingActions.add(firingAction);
	}

	public Iterator<FiringAction> iterator() {
		return firingActions.iterator();
	}

	public int length() {
		return firingActions.size();
	}

}
