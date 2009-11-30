package dk.aau.cs.petrinet.degree2converters;

import java.util.ArrayList;
import java.util.Hashtable;

import dk.aau.cs.petrinet.Degree2Converter;
import dk.aau.cs.petrinet.PlaceTransitionObject;
import dk.aau.cs.petrinet.TAPN;
import dk.aau.cs.petrinet.TAPNInhibitorArc;
import dk.aau.cs.petrinet.TAPNPlace;
import dk.aau.cs.petrinet.TAPNTransition;
import dk.aau.cs.petrinet.TimedArcPetriNet;

public class InhibitorToPrioritiesDegree2Converter implements Degree2Converter {

	private Hashtable<PlaceTransitionObject, PlaceTransitionObject> oldToNew = new Hashtable<PlaceTransitionObject, PlaceTransitionObject>();
	private final String PLOCK = "P_lock";
	
	@Override
	public TAPN transform(TAPN model) throws Exception {
		if(model.isDegree2()) return model;

		TAPN tapn = new TAPN(); 
		
		createPlaces(model, tapn);
		createTransitions(model, tapn);
		createArcs(model, tapn);

		return tapn;
	}

	private void createPlaces(TimedArcPetriNet model, TimedArcPetriNet degree2Net) {
		for(TAPNPlace p : model.getPlaces()){
			TAPNPlace copy = new TAPNPlace(p);
			degree2Net.addPlace(copy);			
			oldToNew.put(p, copy);
		}
		
		TAPNPlace plock = new TAPNPlace(PLOCK, "", 0);
		degree2Net.addPlace(plock);
		
		createCheckPlaces(model, degree2Net);
	}

	private void createCheckPlaces(TimedArcPetriNet model, TimedArcPetriNet degree2Net) {
		ArrayList<TAPNTransition> processed = new ArrayList<TAPNTransition>();
		
		for(TAPNInhibitorArc inhib : model.getInhibitorArcs()){
			TAPNTransition target = (TAPNTransition)inhib.getTarget();
			
			if(!processed.contains(target)){
				TAPNPlace pcheck = new TAPNPlace("P_" + target.getName() + "_check", "<=0", 0);
				degree2Net.addPlace(pcheck);
				processed.add(target);
			}
		}	
	}
	
	private void createTransitions(TimedArcPetriNet model, TimedArcPetriNet degree2Net) {
		for(TAPNTransition transition : model.getTransitions()){
			int transitionsToCreate = 2 * transition.getPreset().size() - 1;
			// 2*size: half t^i_in, half: t^i_out, one: t_(size)
			for(int i = 1; i <= (transitionsToCreate-1); i++){
				TAPNTransition trans = new TAPNTransition();
			}
			
			
			
		}
	}
	
	private void createArcs(TimedArcPetriNet model, TimedArcPetriNet degree2Net) {
		
	}
}
