package dk.aau.cs.model.tapn.simulation;

import dk.aau.cs.model.tapn.LocalTimedMarking;

public interface TimedArcPetriNetStep {
	LocalTimedMarking performStepFrom(LocalTimedMarking marking);
}
