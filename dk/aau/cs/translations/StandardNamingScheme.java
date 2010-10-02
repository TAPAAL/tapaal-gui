package dk.aau.cs.translations;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StandardNamingScheme implements TranslationNamingScheme {
	public TransitionTranslation[] interpretTransitionSequence(List<String> firingSequence) {
		List<TransitionTranslation> transitionTranslations = new ArrayList<TransitionTranslation>();

		String lastTransitionName = "";
		for(int i = 0; i < firingSequence.size(); i++){
			String transitionName = firingSequence.get(i);
			if(!isIgnoredTransition(transitionName)){
				String[] transitionNameSplit = transitionName.split("_");

				if(!transitionNameSplit[0].equals(lastTransitionName)){
					lastTransitionName = transitionNameSplit[0];
					transitionTranslations.add(new TransitionTranslation(i, transitionNameSplit[0]));
				}			
			}
		}
		
		TransitionTranslation[] array = new TransitionTranslation[transitionTranslations.size()];
		transitionTranslations.toArray(array);
		return array;
	}

	private boolean isIgnoredTransition(String string) {
		Pattern pattern = Pattern.compile("c\\d+");
		Matcher matcher = pattern.matcher(string);
		return matcher.find();
	}

	public String getTokenClockName() {
		return "x";
	}

	public boolean isIgnoredPlace(String location) {
		return location.equals("P_lock") ||  location.equals("P_capacity");
	}
}
