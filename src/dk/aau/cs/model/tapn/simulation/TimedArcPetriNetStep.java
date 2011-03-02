package dk.aau.cs.model.tapn.simulation;

import dk.aau.cs.model.tapn.TimedMarking;

public interface TimedArcPetriNetStep {
	TimedMarking performStepFrom(TimedMarking marking);
}
