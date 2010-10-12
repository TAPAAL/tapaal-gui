package dk.aau.cs.translations;

import java.util.List;

public interface TranslationNamingScheme {
	TransitionTranslation[] interpretTransitionSequence(List<String> firingSequence);
	String tokenClockName();
	boolean isIgnoredPlace(String location);	
	boolean isIgnoredAutomata(String automata);
	
	public class TransitionTranslation {
		private int startsAt;
		private String originalTransitionName;
		private int endsAt;
		
		public TransitionTranslation(int startsAt, int endsAt, String originalTransitionName){			
			this.startsAt = startsAt;
			this.endsAt = endsAt;
			this.originalTransitionName = originalTransitionName;
		}
		
		public int startsAt(){
			return startsAt;
		}
		
		public int endsAt(){
			return endsAt;
		}
		
		public String originalTransitionName(){
			return originalTransitionName;
		}
		
		@Override
		public String toString() {
			return originalTransitionName;
		}
	}
}
