package dk.aau.cs.translations;

import java.util.ArrayList;
import java.util.List;

public class StandardNamingScheme implements TranslationNamingScheme {
	public TransitionTranslation[] interpretTransitionSequence(List<String> firingSequence) {
		List<TransitionTranslation> transitionTranslations = new ArrayList<TransitionTranslation>();

		String lastTransitionName = "";
		for(int i = 0; i < firingSequence.size(); i++){
			String[] parts = firingSequence.get(i).split("_");
			
			if(!parts[0].equals(lastTransitionName)){
				lastTransitionName = parts[0];
				transitionTranslations.add(new TransitionTranslation(i, parts[0]));
			}			
		}
		
		TransitionTranslation[] array = new TransitionTranslation[transitionTranslations.size()];
		transitionTranslations.toArray(array);
		return array;
	}

	public String getTokenClockName() {
		return "x";
	}

	public boolean isIgnoredPlace(String location) {
		return location.equals("P_lock") ||  location.equals("P_capacity");
	}

}
