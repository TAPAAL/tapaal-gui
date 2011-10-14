package dk.aau.cs.translations;

import java.util.List;

public interface TranslationNamingScheme {
	TransitionTranslation[] interpretTransitionSequence(
			List<String> firingSequence);

	String tokenClockName();

	boolean isIgnoredPlace(String location);

	boolean isIgnoredAutomata(String automata);

	class TransitionTranslation {
		private int startsAt;
		private String originalTransitionName;
		private int endsAt;
		private SequenceInfo sequenceInfo;

		public enum SequenceInfo {
			WHOLE, END
		};

		public TransitionTranslation(int startsAt, int endsAt,
				String originalTransitionName, SequenceInfo sequenceInfo) {
			this.startsAt = startsAt;
			this.endsAt = endsAt;
			this.originalTransitionName = originalTransitionName;
			this.sequenceInfo = sequenceInfo;
		}

		public int startsAt() {
			return startsAt;
		}

		public int endsAt() {
			return endsAt;
		}

		public SequenceInfo sequenceInfo() {
			return sequenceInfo;
		}

		public String originalTransitionName() {
			return originalTransitionName;
		}

		@Override
		public String toString() {
			return originalTransitionName;
		}
	}
}
