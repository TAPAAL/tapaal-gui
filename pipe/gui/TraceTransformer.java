package pipe.gui;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.TAPNTrace;
import pipe.dataLayer.TimedPlace;
import pipe.dataLayer.Transition;
import pipe.dataLayer.simulation.Token;
import dk.aau.cs.TA.trace.Participant;
import dk.aau.cs.TA.trace.TimeDelayFiringAction;
import dk.aau.cs.TA.trace.TransitionFiringAction;
import dk.aau.cs.TA.trace.UppaalTrace;
import dk.aau.cs.translations.TranslationNamingScheme;
import dk.aau.cs.translations.TranslationNamingScheme.TransitionTranslation;
import dk.aau.cs.verification.FiringAction;

public class TraceTransformer {
	private final DataLayer tapn;
	private final TranslationNamingScheme namingScheme;

	public TraceTransformer(DataLayer tapn, TranslationNamingScheme namingScheme){
		this.tapn = tapn;
		this.namingScheme = namingScheme;

	}

	public TAPNTrace interpretTrace(UppaalTrace trace){
		TAPNTrace result = new TAPNTrace();

		Iterator<FiringAction> iterator = trace.iterator();
		FiringAction action = null;

		while(iterator.hasNext()){
			List<TransitionFiringAction> firingSequence = new ArrayList<TransitionFiringAction>();
			List<String> firingSequenceNames = new ArrayList<String>();

			while(iterator.hasNext() && 
					(action = iterator.next()) instanceof TransitionFiringAction){
				firingSequence.add((TransitionFiringAction)action);
				firingSequenceNames.add(((TransitionFiringAction)action).channel());
			}

			TransitionTranslation[] transitions = namingScheme.interpretTransitionSequence(firingSequenceNames);

			for(TransitionTranslation transitionTranslation : transitions){
				Transition transition = tapn.getTransitionByName(transitionTranslation.originalTransitionName());

				TransitionFiringAction transitionFiring = firingSequence.get(transitionTranslation.startsAt());
				List<Token> tokens = parseConsumedTokens(transitionFiring);				

				pipe.dataLayer.FiringAction firingAction = new pipe.dataLayer.DiscreetFiringAction(transition, tokens);
				result.addFiringAction(firingAction);
			}


			if(action != null && action instanceof TimeDelayFiringAction){
				BigDecimal delay = ((TimeDelayFiringAction)action).getDelay();
				pipe.dataLayer.TimeDelayFiringAction delayAction = new pipe.dataLayer.TimeDelayFiringAction(delay);
				result.addFiringAction(delayAction);
			}
		}

		return result;
	}

	private List<Token> parseConsumedTokens(TransitionFiringAction transitionFiring) {
		ArrayList<Token> tokens = new ArrayList<Token>();

		for(Participant participant : transitionFiring.participants()){
			if(!namingScheme.isIgnoredPlace(participant.location())){
				TimedPlace place = (TimedPlace)tapn.getPlaceByName(participant.location());
				Token token = new Token(place, participant.clockValue(namingScheme.getTokenClockName()));
				tokens.add(token);
			}
		}

		return tokens;
	}
}
