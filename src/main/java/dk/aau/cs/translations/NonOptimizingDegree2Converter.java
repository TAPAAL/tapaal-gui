package dk.aau.cs.translations;

import dk.aau.cs.model.tapn.TimedTransition;

public class NonOptimizingDegree2Converter extends Degree2Converter {
	protected void createDegree2TransitionSimulation(TimedTransition t) {
		Pairing p = new Pairing(t);
		createTransitionSimulation(t,p);
	}
}
