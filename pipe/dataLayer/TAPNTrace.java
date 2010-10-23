package pipe.dataLayer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TAPNTrace implements Iterable<FiringAction> {
	private boolean isConcreteTrace;
	//private Marking initialMarking = null;
	private List<FiringAction> firingActions = new ArrayList<FiringAction>();
	
	public TAPNTrace(boolean isConcreteTrace){
		this.isConcreteTrace = isConcreteTrace;
	}
	
	public void addFiringAction(FiringAction firingAction) {
		firingActions.add(firingAction);
	}

	public Iterator<FiringAction> iterator() {
		return firingActions.iterator();
	}

	public int length() {
		return firingActions.size();
	}

	public boolean isConcreteTrace() {
		return isConcreteTrace;
	}

}
