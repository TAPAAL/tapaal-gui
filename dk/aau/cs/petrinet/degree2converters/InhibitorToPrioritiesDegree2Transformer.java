package dk.aau.cs.petrinet.degree2converters;

import java.util.Hashtable;
import java.util.List;

import dk.aau.cs.petrinet.Arc;
import dk.aau.cs.petrinet.Degree2Converter;
import dk.aau.cs.petrinet.PlaceTransitionObject;
import dk.aau.cs.petrinet.TAPN;
import dk.aau.cs.petrinet.TAPNArc;
import dk.aau.cs.petrinet.TAPNPlace;
import dk.aau.cs.petrinet.TAPNTransition;
import dk.aau.cs.petrinet.TAPNTransportArc;
import dk.aau.cs.petrinet.TimedArcPetriNet;

public class InhibitorToPrioritiesDegree2Transformer implements Degree2Converter {

	private Hashtable<PlaceTransitionObject, PlaceTransitionObject> oldToNew = new Hashtable<PlaceTransitionObject, PlaceTransitionObject>();

	@Override
	public TAPN transform(TAPN model) throws Exception {
		if(!model.isDegree2()) throw new IllegalArgumentException("model must be degree 2!");

		TAPN tapn = new TAPN(); // TODO: dont think it is necessary to take a copy of the model.
		

		return tapn;

	}

	private void createCheckPlaces(List<TAPNTransition> transitions,
			TimedArcPetriNet tapn) {
		for(TAPNTransition transition : transitions){
			
		}
		
	}

	private void createArcs(TimedArcPetriNet model, TimedArcPetriNet tapn, TAPNPlace plock)
	throws Exception {
		
	}

	private TAPNPlace createPlaces(TimedArcPetriNet model, TimedArcPetriNet tapn) {
		TAPNPlace plock = null;
		for(TAPNPlace p : model.getPlaces()){
			TAPNPlace copy = new TAPNPlace(p);
			tapn.addPlace(copy);			
			oldToNew.put(p, copy);
			if(copy.getName().equals("P_lock")){
				plock = copy;
			}
		}

		return plock;
	}

	private void createTransitions(TimedArcPetriNet model, TimedArcPetriNet tapn) {
		
	}

	private Arc copyTAPNArc(TAPNArc arc) {
		PlaceTransitionObject source = oldToNew.get(arc.getSource());
		PlaceTransitionObject dest = oldToNew.get(arc.getTarget());
		String guard = arc.getGuard();

		return new TAPNArc(source, dest, guard);
	}

	private Arc copyTransportArc(TAPNTransportArc arc) {
		TAPNPlace source = (TAPNPlace)oldToNew.get(arc.getSource());
		TAPNPlace dest = (TAPNPlace)oldToNew.get(arc.getTarget());
		TAPNTransition transition = (TAPNTransition)oldToNew.get(arc.getIntermediate());
		String guard = arc.getGuard(); // not sure if we need to copy this?

		return new TAPNTransportArc(source, transition, dest, guard);
	}

	private Arc copyArc(Arc arc) {
		PlaceTransitionObject source = oldToNew.get(arc.getSource());
		PlaceTransitionObject dest = oldToNew.get(arc.getTarget());

		return new Arc(source, dest);
	}

}
