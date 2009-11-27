package dk.aau.cs.petrinet;

import java.util.List;


public interface TimedArcPetriNet {
	/* We should consider adding these later to allow
	 * construction of TAPN from the interface
	 */
//	void add(TAPNPlace place);
//	void add(TAPNTransition transition);
//	void add(TAPNArc arc);
//	
//	//void remove(TAPNPlace place);
//	void remove(TAPNTransition transition);
//	boolean remove(TAPNArc arc);
	
	
	List<TAPNPlace> getPlaces();
	//TAPNPlace getPlaceByName(String name);
	
	List<TAPNArc> getArcs();
	
	List<TAPNTransition> getTransitions();
	List<Token> getTokens();

	boolean isDegree2();
}
