package dk.aau.cs.gui;

import java.util.Hashtable;

import dk.aau.cs.model.tapn.TimedArcPetriNet;

public class NameGenerator {
	private static final String PLACE_NAME_PREFIX = "P";
	private static final String TRANSITION_NAME_PREFIX = "T";
	private static final String TEMPLATE_NAME_PREFIX = "TAPN";
	private Hashtable<TimedArcPetriNet, Integer> placeIDs;
	private Hashtable<TimedArcPetriNet, Integer> transitionIDs;
	private int tapnId = 0;

	public NameGenerator() {
		placeIDs = new Hashtable<TimedArcPetriNet, Integer>();
		transitionIDs = new Hashtable<TimedArcPetriNet, Integer>();
	}

	public void add(TimedArcPetriNet net) {
		if (!placeIDs.containsKey(net) && !transitionIDs.containsKey(net)) {
			placeIDs.put(net, 0);
			transitionIDs.put(net, 0);
		}
	}

	public String getNewPlaceName(TimedArcPetriNet net) {
		int newId = placeIDs.get(net);
		placeIDs.put(net, newId + 1);
		return PLACE_NAME_PREFIX + newId;
	}

	public String getNewTransitionName(TimedArcPetriNet net) {
		int newId = transitionIDs.get(net);
		transitionIDs.put(net, newId + 1);
		return TRANSITION_NAME_PREFIX + newId;
	}
	
	public String getNewTemplateName() {
		return TEMPLATE_NAME_PREFIX + (++tapnId); 
	}
}
