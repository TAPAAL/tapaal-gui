package dk.aau.cs.petrinet;

import java.util.List;


public interface TimedArcPetriNet {
	List<TAPNPlace> getPlaces();
	TAPNPlace getPlaceByName(String name);
	
	List<TAPNTransition> getTransitions();
	List<Token> getTokens();

	boolean isDegree2();
}
