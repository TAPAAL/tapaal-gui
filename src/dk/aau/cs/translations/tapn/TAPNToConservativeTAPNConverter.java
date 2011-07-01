package dk.aau.cs.translations.tapn;

import dk.aau.cs.model.tapn.LocalTimedPlace;
import dk.aau.cs.model.tapn.TimeInterval;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedInputArc;
import dk.aau.cs.model.tapn.TimedOutputArc;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedTransition;

public class TAPNToConservativeTAPNConverter {
	public static final String BOTTOM = "_BOTTOM_";

	public TimedArcPetriNet makeConservative(TimedArcPetriNet model) {
		TimedArcPetriNet conservativeModel = model.copy();

		
		TimedPlace bottom = conservativeModel.getPlaceByName(BOTTOM);
		if(bottom == null) {
			bottom = new LocalTimedPlace(BOTTOM);
			conservativeModel.add(bottom);
		}
		
		
		for (TimedTransition t : conservativeModel.transitions()) {
			int difference = t.postsetSize() - t.presetSize();

			if (difference < 0) {
				for(int i = 0; i < Math.abs(difference); i++) {
					conservativeModel.add(new TimedOutputArc(t, bottom));
				}
			} else if (difference > 0) {
				for(int i = 0; i < difference; i++) {
					conservativeModel.add(new TimedInputArc(bottom, t, TimeInterval.ZERO_INF));
				}
			}
		}
		
		return conservativeModel;
	}

	
}
