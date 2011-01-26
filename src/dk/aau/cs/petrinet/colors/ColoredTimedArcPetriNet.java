package dk.aau.cs.petrinet.colors;

import dk.aau.cs.petrinet.TimedArcPetriNet;

public interface ColoredTimedArcPetriNet extends TimedArcPetriNet {
	int getLowerBoundForColor();
	int getUpperBoundForColor();
}
