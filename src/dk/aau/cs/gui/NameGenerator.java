package dk.aau.cs.gui;

import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pipe.dataLayer.Template;
import dk.aau.cs.model.tapn.SharedPlace;
import dk.aau.cs.model.tapn.SharedTransition;
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
			setupTemplate(net);
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

	public void setupNameGeneratorFromTemplates(Iterable<Template> templates) {
		for(Template tapn : templates) {
			setupTemplate(tapn.model());
		}
	}

	private void setupTemplate(TimedArcPetriNet tapn) {
		Pattern templatePattern = Pattern.compile("^TAPN(\\d+)$", Pattern.CASE_INSENSITIVE);
		
		setupPlaceIDCounter(tapn);
		setupTransitionIDCounter(tapn);

		Matcher m = templatePattern.matcher(tapn.name());
		if(m.matches()) {
			int number = Integer.parseInt(m.group(1));
			if(number > tapnId) tapnId = number;
		}
	}

	private void setupTransitionIDCounter(TimedArcPetriNet tapn) {
		Pattern transitionPattern = Pattern.compile("^T(\\d+)$", Pattern.CASE_INSENSITIVE);
		int nameCounter = -1;
		for(TimedTransition t : tapn.transitions()) {
			Matcher m = transitionPattern.matcher(t.name().toLowerCase());
			if(m.matches()) {
				int number = Integer.parseInt(m.group(1));
				if(number > nameCounter) nameCounter = number;
			}
		}
		
		for(SharedTransition t : tapn.parentNetwork().sharedTransitions()) {
			Matcher m = transitionPattern.matcher(t.name().toLowerCase());
			if(m.matches()) {
				int number = Integer.parseInt(m.group(1));
				if(number > nameCounter) nameCounter = number;
			}
		}
		transitionIDs.put(tapn, ++nameCounter);
	}

	private void setupPlaceIDCounter(TimedArcPetriNet tapn) {
		Pattern placePattern = Pattern.compile("^P(\\d+)$", Pattern.CASE_INSENSITIVE);
		int nameCounter = -1;
		for(TimedPlace p : tapn.places()) {
			Matcher m = placePattern.matcher(p.name().toLowerCase());
			if(m.matches()) {
				int number = Integer.parseInt(m.group(1));
				if(number > nameCounter) nameCounter = number;
			}
		}
		
		for(SharedPlace p : tapn.parentNetwork().sharedPlaces()) {
			Matcher m = placePattern.matcher(p.name().toLowerCase());
			if(m.matches()) {
				int number = Integer.parseInt(m.group(1));
				if(number > nameCounter) nameCounter = number;
			}
		}
		
		placeIDs.put(tapn, ++nameCounter);
	}

	public void updateTemplateIndex(String newName){
		Pattern templatePattern = Pattern.compile("^TAPN(\\d+)$", Pattern.CASE_INSENSITIVE);
		Matcher m = templatePattern.matcher(newName);
		if(m.matches()) {
			int id = Integer.parseInt(m.group(1));
			if(id >= tapnId)
				tapnId = ++id;
		}
	}

	private void updateMap(TimedArcPetriNet model, String newName, String pattern, Hashtable<TimedArcPetriNet, Integer> map) {
		Pattern compiledPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
		Matcher m = compiledPattern.matcher(newName.toLowerCase());
		if(m.matches()){
			int current = map.get(model);
			int id = Integer.parseInt(m.group(1));
			if(id >= current){
				map.put(model, ++id);
			}
		}
	}

	public void updateIndices(TimedArcPetriNet model, String newName) {
		updateMap(model, newName, "^T(\\d+)$", transitionIDs);
		updateMap(model, newName, "^P(\\d+)$", placeIDs);		
	}

	public void updateIndicesForAllModels(String name) {
		for(TimedArcPetriNet net : placeIDs.keySet()){
			updateIndices(net, name);
		}
	}
}
