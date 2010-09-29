package pipe.dataLayer;

import java.util.ArrayList;
import java.util.List;

import pipe.dataLayer.simulation.Marking;

public class TAPNTrace {
	private Marking initialMarking = null;
	private List<FiringAction> firingActions = new ArrayList<FiringAction>();
	
	public void addFiringAction(FiringAction firingAction) {
		firingActions.add(firingAction);
	}

}
