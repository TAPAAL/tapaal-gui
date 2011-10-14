package dk.aau.cs.verification.UPPAAL;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import dk.aau.cs.model.NTA.trace.Participant;
import dk.aau.cs.model.NTA.trace.TAFiringAction;
import dk.aau.cs.model.NTA.trace.TimeDelayFiringAction;
import dk.aau.cs.model.NTA.trace.TransitionFiring;
import dk.aau.cs.model.NTA.trace.UppaalTrace;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedToken;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.model.tapn.simulation.TimeDelayStep;
import dk.aau.cs.model.tapn.simulation.TimedArcPetriNetTrace;
import dk.aau.cs.model.tapn.simulation.TimedTransitionStep;
import dk.aau.cs.translations.TranslationNamingScheme;
import dk.aau.cs.translations.TranslationNamingScheme.TransitionTranslation;
import dk.aau.cs.translations.TranslationNamingScheme.TransitionTranslation.SequenceInfo;

public class VerifytaTraceInterpreter {
	private final TimedArcPetriNet tapn;
	private final TranslationNamingScheme namingScheme;

	public VerifytaTraceInterpreter(TimedArcPetriNet model, TranslationNamingScheme namingScheme) {
		tapn = model;
		this.namingScheme = namingScheme;
	}

	protected TimedArcPetriNet tapn() {
		return tapn;
	}

	protected TranslationNamingScheme namingScheme() {
		return namingScheme;
	}

	public TimedArcPetriNetTrace interpretTrace(UppaalTrace trace) {
		return interpretTimedTrace(trace);
	}

	private TimedArcPetriNetTrace interpretTimedTrace(UppaalTrace trace) {
		boolean isConcreteTrace = trace.isConcreteTrace();
		TimedArcPetriNetTrace result = new TimedArcPetriNetTrace(isConcreteTrace);

		Iterator<TAFiringAction> iterator = trace.iterator();
		TAFiringAction action = null;

		while (iterator.hasNext()) {
			List<TransitionFiring> firingSequence = new ArrayList<TransitionFiring>();
			List<String> firingSequenceNames = new ArrayList<String>();

			while (iterator.hasNext() && (action = iterator.next()) instanceof TransitionFiring) {
				firingSequence.add((TransitionFiring) action);
				firingSequenceNames.add(((TransitionFiring) action).channel());
			}

			TransitionTranslation[] transitions = namingScheme.interpretTransitionSequence(firingSequenceNames);

			for (TransitionTranslation transitionTranslation : transitions) {
				TimedTransitionStep transitionStep = interpretTransitionFiring(firingSequence, transitionTranslation, isConcreteTrace);
				result.add(transitionStep);
			}

			if (action != null && action instanceof TimeDelayFiringAction) {
				BigDecimal delay = ((TimeDelayFiringAction) action).getDelay();
				TimeDelayStep delayAction = new TimeDelayStep(delay);
				result.add(delayAction);
			}
		}

		return result;
	}

	protected TimedTransitionStep interpretTransitionFiring(List<TransitionFiring> firingSequence,	TransitionTranslation transitionTranslation, boolean isConcreteTrace) {
		TimedTransition transition = tapn.getTransitionByName(transitionTranslation.originalTransitionName());
		List<TimedToken> tokens = null;
		if (isConcreteTrace) {
			if (transitionTranslation.sequenceInfo().equals(SequenceInfo.WHOLE)) {
				tokens = parseConsumedTokens(firingSequence.subList(transitionTranslation.startsAt(), transitionTranslation.endsAt() + 1));
			} else if (transitionTranslation.sequenceInfo().equals(SequenceInfo.END)) {
				TransitionFiring start = firingSequence.get(transitionTranslation.startsAt());
				TransitionFiring end = firingSequence.get(transitionTranslation.endsAt());
				tokens = parseConsumedTokens(start, end);
			}
		}

		return new TimedTransitionStep(transition,tokens);
	}

	private List<TimedToken> parseConsumedTokens(List<TransitionFiring> actions) {
		ArrayList<TimedToken> tokens = new ArrayList<TimedToken>();

		for (int i = 0; i < actions.size(); i++) {
			TransitionFiring action = actions.get(i);

			for (Participant participant : action.participants()) {
				String automata = participant.automata();
				String sourceLocation = participant.location();

				if (!namingScheme.isIgnoredAutomata(automata) && !namingScheme.isIgnoredPlace(sourceLocation)) {
					TimedPlace place = tapn.getPlaceByName(sourceLocation);
					BigDecimal clockValue = participant.clockOrVariableValue(namingScheme().tokenClockName()).lower();
					TimedToken token = new TimedToken(place, clockValue);
					tokens.add(token);
				}
			}
		}

		return tokens;
	}

	private List<TimedToken> parseConsumedTokens(TransitionFiring start, TransitionFiring end) {
		ArrayList<TimedToken> tokens = new ArrayList<TimedToken>();

		for (Participant participant : end.participants()) {
			String automata = participant.automata();
			String sourceLocation = start.sourceState().locationFor(automata);

			if (!namingScheme.isIgnoredAutomata(automata) && !namingScheme.isIgnoredPlace(sourceLocation)) {
				TimedPlace place = tapn.getPlaceByName(sourceLocation);
				BigDecimal clockValue = start.sourceState().getLocalClockOrVariable(automata, namingScheme().tokenClockName()).lower();
				TimedToken token = new TimedToken(place, clockValue);
				tokens.add(token);
			}
		}

		return tokens;
	}
}
