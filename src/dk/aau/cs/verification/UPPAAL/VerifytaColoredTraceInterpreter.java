package dk.aau.cs.verification.UPPAAL;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.TA.trace.Participant;
import dk.aau.cs.TA.trace.TransitionFiring;
import dk.aau.cs.petrinet.TAPNPlace;
import dk.aau.cs.petrinet.TAPNTransition;
import dk.aau.cs.petrinet.colors.ColoredTimedArcPetriNet;
import dk.aau.cs.petrinet.colors.ColoredToken;
import dk.aau.cs.petrinet.trace.TAPNFiringAction;
import dk.aau.cs.translations.ColoredTranslationNamingScheme;
import dk.aau.cs.translations.TranslationNamingScheme.TransitionTranslation;
import dk.aau.cs.translations.TranslationNamingScheme.TransitionTranslation.SequenceInfo;

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
			List<TransitionFiring> firingSequence,
			TransitionTranslation transitionTranslation,
			boolean isConcreteTrace
	) {
		TAPNTransition transition = tapn().getTransitionsByName(transitionTranslation.originalTransitionName());

		List<ColoredToken> consumedTokens = null;
		List<ColoredToken> producedTokens = null;
		if(isConcreteTrace){
			if(transitionTranslation.sequenceInfo().equals(SequenceInfo.WHOLE)){
				List<TransitionFiring> actions = firingSequence.subList(transitionTranslation.startsAt(), transitionTranslation.endsAt()+1);
				consumedTokens = parseConsumedTokens(actions);
				producedTokens = parseProducedTokens(actions);
			}else if(transitionTranslation.sequenceInfo().equals(SequenceInfo.END)){
				TransitionFiring start = firingSequence.get(transitionTranslation.startsAt());
				TransitionFiring end = firingSequence.get(transitionTranslation.endsAt());
				consumedTokens = parseConsumedTokens(start, end);	
				producedTokens = parseProducedTokens(start, end);	
			}
		}

		return new dk.aau.cs.petrinet.trace.ColoredTransitionFiringAction(transition, consumedTokens, producedTokens);
	}

	private List<ColoredToken> parseProducedTokens(List<TransitionFiring> actions) {
		ArrayList<ColoredToken> tokens = new ArrayList<ColoredToken>();
		TransitionFiring end = actions.get(actions.size()-1);

		for(int i = actions.size()/2; i < actions.size(); i++){
			TransitionFiring action = actions.get(i);

			for(Participant participant : action.participants()){
				String automata = participant.automata();
				String targetLocation = end.targetState().locationFor(automata);

				if(!namingScheme().isIgnoredAutomata(automata) && !namingScheme().isIgnoredPlace(targetLocation)){
					TAPNPlace place = tapn().getPlaceByName(targetLocation);
					BigDecimal clockValue = end.targetState().getLocalClockOrVariable(automata, namingScheme().tokenClockName()).lower();
					int color = end.targetState().getLocalClockOrVariable(automata, namingScheme().colorVariableName()).lower().intValue();

					ColoredToken token = new ColoredToken(place, clockValue, color); 
					tokens.add(token);
				}
			}
		}

		return tokens;
	}

	private List<ColoredToken> parseProducedTokens(TransitionFiring start, TransitionFiring end) {
		ArrayList<ColoredToken> tokens = new ArrayList<ColoredToken>();

		for(Participant participant : end.participants()){
			String automata = participant.automata();
			String targetLocation = end.targetState().locationFor(automata);

			if(!namingScheme().isIgnoredAutomata(automata) && !namingScheme().isIgnoredPlace(targetLocation)){
				TAPNPlace place = tapn().getPlaceByName(targetLocation);
				BigDecimal clockValue = end.targetState().getLocalClockOrVariable(automata, namingScheme().tokenClockName()).lower();
				int color = end.targetState().getLocalClockOrVariable(automata, namingScheme().colorVariableName()).lower().intValue();

				ColoredToken token = new ColoredToken(place, clockValue, color);
				tokens.add(token);
			}
		}

		return tokens;
	}

	private List<ColoredToken> parseConsumedTokens(List<TransitionFiring> actions) {
		ArrayList<ColoredToken> tokens = new ArrayList<ColoredToken>();

		for(int i = 0; i < actions.size(); i++){
			TransitionFiring action = actions.get(i);

			for(Participant participant : action.participants()){
				String automata = participant.automata();
				String sourceLocation = participant.location();

				if(!namingScheme().isIgnoredAutomata(automata) && !namingScheme().isIgnoredPlace(sourceLocation)){
					TAPNPlace place = tapn().getPlaceByName(sourceLocation);
					BigDecimal clockValue = participant.clockOrVariableValue(namingScheme().tokenClockName()).lower();
					int color = participant.clockOrVariableValue(namingScheme().colorVariableName()).lower().intValue();

					ColoredToken token = new ColoredToken(place, clockValue, color); 
					tokens.add(token);
				}
			}
		}

		return tokens;
	}

	private List<ColoredToken> parseConsumedTokens(TransitionFiring start, TransitionFiring end) {
		ArrayList<ColoredToken> tokens = new ArrayList<ColoredToken>();

		for(Participant participant : end.participants()){
			String automata = participant.automata();			
			String sourceLocation = start.sourceState().locationFor(automata);

			if(!namingScheme().isIgnoredAutomata(automata) && !namingScheme().isIgnoredPlace(sourceLocation)){
				TAPNPlace place = tapn().getPlaceByName(sourceLocation);
				BigDecimal clockValue = start.sourceState().getLocalClockOrVariable(automata, namingScheme().tokenClockName()).lower();
				int color = start.sourceState().getLocalClockOrVariable(automata, namingScheme().colorVariableName()).lower().intValue();

				ColoredToken token = new ColoredToken(place, clockValue, color);
				tokens.add(token);
			}
		}

		return tokens;
	}

}
