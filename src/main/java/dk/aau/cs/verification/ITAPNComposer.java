package dk.aau.cs.verification;

import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.util.Tuple;
import pipe.gui.petrinet.dataLayer.DataLayer;

public interface ITAPNComposer {

	Tuple<TimedArcPetriNet, NameMapping> transformModel(TimedArcPetriNetNetwork model);
	DataLayer getGuiModel();

}
