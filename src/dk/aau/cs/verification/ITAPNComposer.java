package dk.aau.cs.verification;

import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.util.Tuple;

public interface ITAPNComposer {

	Tuple<TimedArcPetriNet, NameMapping> transformModel(TimedArcPetriNetNetwork model);

}
