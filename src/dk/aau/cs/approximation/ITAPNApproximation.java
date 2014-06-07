package dk.aau.cs.approximation;

import dk.aau.cs.model.tapn.TimedArcPetriNet;

public interface ITAPNApproximation {
	//Returns a copy of a network which is the approximated network
	public void modifyTAPN(TimedArcPetriNet net, int approximationDenominator);
}
