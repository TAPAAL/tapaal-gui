package dk.aau.cs.petrinet;

import java.util.List;


public interface TimedArcPetriNet {	
	void addPlace(TAPNPlace place);
	void addTransition(TAPNTransition transition);
	void addArc(Arc arc) throws Exception;
	void addArc(TAPNArc arc) throws Exception;
	void addArc(TAPNTransportArc arc) throws Exception;
	void addArc(TAPNInhibitorArc arc) throws Exception;
	void addToken(Token token);
	
	/* We should consider adding these later to allow
	 * de-construction of TAPN from the interface
	 */	
//	//void remove(TAPNPlace place);
//	void remove(TAPNTransition transition);
//	boolean remove(TAPNArc arc);
	
	
	List<TAPNPlace> 	   getPlaces();
	TAPNPlace			   getPlaceByName(String name);
	List<TAPNTransition>   getTransitions();
	List<Token> 		   getTokens();
		
	List<TAPNTransportArc> getTransportArcs();
	List<TAPNInhibitorArc> getInhibitorArcs();
	List<TAPNArc>		   getTAPNArcs();
	List<Arc> 			   getNormalArcs();
	
	boolean isDegree2();
	void convertToConservative() throws Exception;
}
