package dk.aau.cs.verification.UPPAAL;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.TA.trace.Participant;
import dk.aau.cs.TA.trace.TransitionFiringAction;
import dk.aau.cs.petrinet.TAPNPlace;
import dk.aau.cs.petrinet.TAPNTransition;
import dk.aau.cs.petrinet.colors.ColoredTimedArcPetriNet;
import dk.aau.cs.petrinet.colors.ColoredToken;
import dk.aau.cs.petrinet.trace.TAPNFiringAction;
import dk.aau.cs.translations.ColoredTranslationNamingScheme;
import dk.aau.cs.translations.TranslationNamingScheme.TransitionTranslation;

public class VerifytaColoredTraceInterpreter extends VerifytaTraceInterpreter {

	public VerifytaColoredTraceInterpreter(ColoredTimedArcPetriNet tapn,
			ColoredTranslationNamingScheme namingScheme) {
		super(tapn, namingScheme);
	}
	
	@Override
	protected ColoredTranslationNamingScheme namingScheme() {
		return (ColoredTranslationNamingScheme)super.namingScheme();
	}
	
	@Override
	protected ColoredTimedArcPetriNet tapn() {
		return (ColoredTimedArcPetriNet)super.tapn();
	}

	@Override
	protected TAPNFiringAction interpretTransitionFiring
	(
			List<TransitionFiringAction> firingSequence,
			TransitionTranslation transitionTranslation
	) {
		TAPNTransition transition = tapn().getTransitionsByName(transitionTranslation.originalTransitionName());

		TransitionFiringAction transitionFiring = firingSequence.get(transitionTranslation.startsAt());
		List<ColoredToken> tokens = parseConsumedColoredTokens(transitionFiring);				

		return new dk.aau.cs.petrinet.trace.ColoredTransitionFiringAction(transition, tokens);
	}

	private List<ColoredToken> parseConsumedColoredTokens(
			TransitionFiringAction transitionFiring) {
		ArrayList<ColoredToken> tokens = new ArrayList<ColoredToken>();

		for(Participant participant : transitionFiring.participants()){
			if(!namingScheme().isIgnoredAutomata(participant.automata()) && !namingScheme().isIgnoredPlace(participant.location())){
				TAPNPlace place = tapn().getPlaceByName(participant.location());
				BigDecimal clockValue = participant.clockValue(namingScheme().tokenClockName());
				int color = participant.variable(namingScheme().colorVariableName());
				
				ColoredToken token = new ColoredToken(place, clockValue, color);
				tokens.add(token);
			}
		}

		return tokens;
	}

}
