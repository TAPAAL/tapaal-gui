package dk.aau.cs.translations.tapn;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dk.aau.cs.translations.TranslationNamingScheme;
import dk.aau.cs.translations.TranslationNamingScheme.TransitionTranslation.SequenceInfo;

public class StandardNamingScheme implements TranslationNamingScheme {
	private static final int NOT_FOUND = -1;
	private final String START_OF_SEQUENCE_PATTERN = "^(\\w+?)_T0$";
	private Pattern startPattern = Pattern.compile(START_OF_SEQUENCE_PATTERN);
	private Pattern ignoredPlacePattern = Pattern.compile("^P_lock|P_capacity|\\w+_im\\d+|\\w+_hp_0|\\w+_hp\\d+$");;
	private final SequenceInfo seqInfo = SequenceInfo.WHOLE;
	
	public TransitionTranslation[] interpretTransitionSequence(List<String> firingSequence) {
		List<TransitionTranslation> transitionTranslations = new ArrayList<TransitionTranslation>();

		int startIndex = NOT_FOUND;
		String originalTransitionName = null;
		for(int i = 0; i < firingSequence.size(); i++){
			String transitionName = firingSequence.get(i);
			Matcher startMatcher = startPattern.matcher(transitionName);

			boolean isStartTransition = startMatcher.matches();

			if(isStartTransition){ 
				if(startIndex != NOT_FOUND){
					transitionTranslations.add(new TransitionTranslation(startIndex, i-1, originalTransitionName, seqInfo));
				}
				startIndex = i; 
				originalTransitionName = startMatcher.group(1); 
			}			
		}
		
		if(startIndex != NOT_FOUND){
			transitionTranslations.add(new TransitionTranslation(startIndex, firingSequence.size()-1, originalTransitionName, seqInfo));
		}
		TransitionTranslation[] array = new TransitionTranslation[transitionTranslations.size()];
		transitionTranslations.toArray(array);
		return array;
	}

	public String tokenClockName() {
		return "x";
	}

	public boolean isIgnoredPlace(String location) {
		Matcher matcher = ignoredPlacePattern.matcher(location);
		return matcher.matches();
	}

	public boolean isIgnoredAutomata(String automata) {
		return false;
	}
}
