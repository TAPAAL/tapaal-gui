package dk.aau.cs.approximation;

import pipe.dataLayer.TAPNQuery;
import dk.aau.cs.model.tapn.TimedArcPetriNet;

public interface ITAPNApproximation {
	//Returns a copy of a network which is the approximated network
	public TimedArcPetriNet modifyTAPN(TimedArcPetriNet net, TAPNQuery query);
}
