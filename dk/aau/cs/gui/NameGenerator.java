package dk.aau.cs.gui;

import java.util.Hashtable;

public class NameGenerator<TNet> {
	private static final String PLACE_NAME_PREFIX = "P";
	private static final String TRANSITION_NAME_PREFIX = "T";
	private Hashtable<TNet, Integer> placeIDs;
	private Hashtable<TNet, Integer> transitionIDs;
	
	public NameGenerator(){
		placeIDs = new Hashtable<TNet, Integer>();
		transitionIDs = new Hashtable<TNet, Integer>();
	}
	
	public void add(TNet net){
		if(!placeIDs.containsKey(net) && !transitionIDs.containsKey(net)){
			placeIDs.put(net, 0);
			transitionIDs.put(net, 0);
		}
	}
	
	public String getNewPlaceName(TNet net){
		int newId = placeIDs.get(net);
		placeIDs.put(net, newId + 1);
		return PLACE_NAME_PREFIX + newId;
	}
	
	public String getNewTransitionName(TNet net){
		int newId = transitionIDs.get(net);
		transitionIDs.put(net, newId + 1);
		return TRANSITION_NAME_PREFIX + newId;
	}
}
