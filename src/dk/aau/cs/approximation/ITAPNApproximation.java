package dk.aau.cs.approximation;

import pipe.dataLayer.TAPNQuery;
import dk.aau.cs.model.tapn.TimedArcPetriNet;

public interface ITAPNApproximation {
	//Returns a copy of a network which is the approximated network
	public void modifyTAPN(TimedArcPetriNet net, TAPNQuery query);
}
