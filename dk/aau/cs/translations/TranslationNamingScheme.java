package dk.aau.cs.translations;

import java.util.List;

public interface TranslationNamingScheme {
	String[] interpretTransitionSequence(List<String> firingSequence);
}
