package dk.aau.cs.translations.tapn;

import dk.aau.cs.model.tapn.TimeInterval;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedInputArc;
import dk.aau.cs.model.tapn.TimedOutputArc;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedTransition;

public class TAPNToConservativeTAPNConverter {

	public TimedArcPetriNet makeConservative(TimedArcPetriNet model) {
		TimedArcPetriNet conservativeModel = model.copy();
		
		TimedPlace capacity = new TimedPlace("P_capacity");
		conservativeModel.add(capacity);
		
		for (TimedTransition t : conservativeModel.transitions()) {
			int difference = t.postsetSize() - t.presetSize();

			if (difference < 0) {
				for(int i = 0; i < Math.abs(difference); i++) {
					conservativeModel.add(new TimedOutputArc(t, capacity));
				}
			} else if (difference > 0) {
				for(int i = 0; i < difference; i++) {
					conservativeModel.add(new TimedInputArc(capacity, t, TimeInterval.ZERO_INF));
				}
			}
		}
		
		return conservativeModel;
	}

	
}
