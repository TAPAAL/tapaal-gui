package dk.aau.cs.gui;

import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pipe.dataLayer.Template;

import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedTransition;

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
		return TEMPLATE_NAME_PREFIX + (tapnId++); 
	}
	
	public void setupNameGeneratorFromTemplates(Iterable<Template<TimedArcPetriNet>> templates) {
		Pattern templatePattern = Pattern.compile("([tT][aA][pP][nN])(\\d+)");
		
		int templateNameCounter = 0;
		for(Template<TimedArcPetriNet> tapn : templates) {
			setupPlaceIDCounter(tapn);
			setupTransitionIDCounter(tapn);
		
			Matcher m = templatePattern.matcher(tapn.model().getName());
			if(m.matches()) {
				int number = Integer.parseInt(m.group(2));
				if(number > templateNameCounter) templateNameCounter = number;
			}
		}
		tapnId = ++templateNameCounter;
	}

	private void setupTransitionIDCounter(Template<TimedArcPetriNet> tapn) {
		Pattern transitionPattern = Pattern.compile("([tT])(\\d+)");
		int nameCounter = 0;
		for(TimedTransition t : tapn.model().transitions()) {
			Matcher m = transitionPattern.matcher(t.name().toLowerCase());
			if(m.matches()) {
				int number = Integer.parseInt(m.group(2));
				if(number > nameCounter) nameCounter = number;
			}
		}
		transitionIDs.put(tapn.model(), ++nameCounter);
	}

	private void setupPlaceIDCounter(Template<TimedArcPetriNet> tapn) {
		Pattern placePattern = Pattern.compile("([pP])(\\d+)");
		int nameCounter = 0;
		for(TimedPlace p : tapn.model().places()) {
			Matcher m = placePattern.matcher(p.name().toLowerCase());
			if(m.matches()) {
				int number = Integer.parseInt(m.group(2));
				if(number > nameCounter) nameCounter = number;
			}
		}
		placeIDs.put(tapn.model(), ++nameCounter);
	}
}
