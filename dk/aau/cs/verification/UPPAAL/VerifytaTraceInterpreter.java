package dk.aau.cs.verification.UPPAAL;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import dk.aau.cs.TA.trace.Participant;
import dk.aau.cs.TA.trace.TAFiringAction;
import dk.aau.cs.TA.trace.TimeDelayFiringAction;
import dk.aau.cs.TA.trace.TransitionFiringAction;
import dk.aau.cs.TA.trace.UppaalTrace;
import dk.aau.cs.petrinet.TAPNPlace;
import dk.aau.cs.petrinet.TAPNTransition;
import dk.aau.cs.petrinet.TimedArcPetriNet;
import dk.aau.cs.petrinet.Token;
import dk.aau.cs.petrinet.trace.TAPNFiringAction;
import dk.aau.cs.petrinet.trace.TAPNTrace;
import dk.aau.cs.translations.TranslationNamingScheme;
import dk.aau.cs.translations.TranslationNamingScheme.TransitionTranslation;

public class VerifytaTraceInterpreter {
	private final TimedArcPetriNet tapn;
	private final TranslationNamingScheme namingScheme;

	public VerifytaTraceInterpreter(TimedArcPetriNet tapn, TranslationNamingScheme namingScheme){
		this.tapn = tapn;
		this.namingScheme = namingScheme;
	}
	
	protected TimedArcPetriNet tapn(){
		return tapn;
	}
	
	protected TranslationNamingScheme namingScheme(){
		return namingScheme;
	}

	public TAPNTrace interpretTrace(UppaalTrace trace){
		TAPNTrace result = new TAPNTrace();

		Iterator<TAFiringAction> iterator = trace.iterator();
		TAFiringAction action = null;

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
				TAPNFiringAction firingAction = interpretTransitionFiring(firingSequence, transitionTranslation);
				result.addFiringAction(firingAction);
			}


			if(action != null && action instanceof TimeDelayFiringAction){
				BigDecimal delay = ((TimeDelayFiringAction)action).getDelay();
				TAPNFiringAction delayAction = new dk.aau.cs.petrinet.trace.TimeDelayFiringAction(delay);
				result.addFiringAction(delayAction);
			}
		}

		return result;
	}

	protected TAPNFiringAction interpretTransitionFiring
	(
			List<TransitionFiringAction> firingSequence,
			TransitionTranslation transitionTranslation
	) {
		TAPNTransition transition = tapn.getTransitionsByName(transitionTranslation.originalTransitionName());

		TransitionFiringAction transitionFiring = firingSequence.get(transitionTranslation.startsAt());
		List<Token> tokens = parseConsumedTokens(transitionFiring);				

		return new dk.aau.cs.petrinet.trace.TransitionFiringAction(transition, tokens);
	}

	private List<Token> parseConsumedTokens(TransitionFiringAction transitionFiring) {
		ArrayList<Token> tokens = new ArrayList<Token>();

		for(Participant participant : transitionFiring.participants()){
			if(!namingScheme.isIgnoredAutomata(participant.automata()) && !namingScheme.isIgnoredPlace(participant.location())){
				TAPNPlace place = tapn.getPlaceByName(participant.location());
				Token token = new Token(place, participant.clockValue(namingScheme.tokenClockName()));
				tokens.add(token);
			}
		}

		return tokens;
	}
}
