package dk.aau.cs.translations.tapn;


import java.util.Hashtable;

import dk.aau.cs.model.tapn.IntWeight;
import dk.aau.cs.model.tapn.TimeInterval;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedInputArc;
import dk.aau.cs.model.tapn.TimedOutputArc;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.model.tapn.LocalTimedPlace;

public class TAPNToTimedConservativeTAPNConverter {
	public static final String BOTTOM = "_BOTTOMIN_";

	public TimedArcPetriNet makeConservative(TimedArcPetriNet model, Hashtable<String,Boolean> placeNameToTimed, int largestPresetSize) {
		TimedArcPetriNet conservativeModel = model.copy();

		TimedPlace bottom = conservativeModel.getPlaceByName(BOTTOM);
		if(bottom == null) {
			bottom = new LocalTimedPlace(BOTTOM);
			conservativeModel.add(bottom);
		}

		for(TimedTransition t : model.transitions()){
			for(TimedInputArc ia : t.getInputArcs()){
				if(placeNameToTimed.get(ia.source().name()) && ia.getWeight().value()>1){
					for(int i = 1; i<ia.getWeight().value();i++){
						TimedInputArc arc = new TimedInputArc(conservativeModel.getPlaceByName(ia.source().name()), conservativeModel.getTransitionByName(t.name()), TimeInterval.ZERO_INF);
						arc.setTimeInterval(ia.interval());
						conservativeModel.add(arc);
					}
				}
			}

			for(TimedOutputArc oa : t.getOutputArcs()){
				if(placeNameToTimed.get(oa.destination().name()) && oa.getWeight().value()>1){
					for(int i = 1; i<oa.getWeight().value();i++){
						TimedOutputArc arc = new TimedOutputArc(conservativeModel.getTransitionByName(t.name()) , conservativeModel.getPlaceByName(oa.destination().name()));
						conservativeModel.add(arc);
					}
				}
			}
		}

		for (TimedTransition t : conservativeModel.transitions()) {
			IntWeight weight1 = new IntWeight(1);
			
			int timedPresetSize = 0;
			for(TimedInputArc ia : t.getInputArcs()){
				if(placeNameToTimed.get(ia.source().name())){
					ia.setWeight(weight1);
					timedPresetSize++;
				}
			}

			int timedPostsetSize = 0;
			for(TimedOutputArc oa : t.getOutputArcs()){
				if(placeNameToTimed.get(oa.destination().name())){
					oa.setWeight(weight1);
					timedPostsetSize ++;
				}
			}

			int difference = timedPostsetSize - timedPresetSize;

			if (difference < 0) {
				for(int i = 0; i < Math.abs(difference); i++) {
					TimedOutputArc arc = new TimedOutputArc(t, bottom);
					conservativeModel.add(arc);					
				}
			} else if (difference > 0) {
				for(int i = 0; i < difference; i++) {
					TimedInputArc arc = new TimedInputArc(bottom, t, TimeInterval.ZERO_INF);
					conservativeModel.add(arc);
				}
			}
		}

		return conservativeModel;
	}
}
