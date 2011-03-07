package dk.aau.cs.verification;

import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.model.tapn.TimedInhibitorArc;
import dk.aau.cs.model.tapn.TimedInputArc;
import dk.aau.cs.model.tapn.TimedOutputArc;
import dk.aau.cs.model.tapn.LocalTimedPlace;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedToken;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.model.tapn.TransportArc;
import dk.aau.cs.util.Tuple;

public class TAPNComposer {
	private static final String PLACE_FORMAT = "P%1$d";
	private static final String TRANSITION_FORMAT = "T%1$d";

	private int nextPlaceIndex;
	private int nextTransitionIndex;

	public Tuple<TimedArcPetriNet, NameMapping> transformModel(
			TimedArcPetriNetNetwork model) {
		nextPlaceIndex = -1;
		nextTransitionIndex = -1;
		TimedArcPetriNet tapn = new TimedArcPetriNet("ComposedModel");
		NameMapping mapping = new NameMapping();

		CreatePlaces(model, tapn, mapping);
		CreateTransitions(model, tapn, mapping);
		CreateInputArcs(model, tapn, mapping);
		CreateOutputArcs(model, tapn, mapping);
		CreateTransportArcs(model, tapn, mapping);
		CreateInhibitorArcs(model, tapn, mapping);

		return new Tuple<TimedArcPetriNet, NameMapping>(tapn, mapping);
	}

	private void CreatePlaces(TimedArcPetriNetNetwork model, TimedArcPetriNet constructedModel, NameMapping mapping) {
		for (TimedArcPetriNet tapn : model.templates()) {
			for (TimedPlace timedPlace : tapn.places()) {
				String uniquePlaceName = getUniquePlaceName();

				LocalTimedPlace place = new LocalTimedPlace(uniquePlaceName, timedPlace.invariant());
				constructedModel.add(place);
				mapping.addMapping(tapn.getName(), timedPlace.name(), uniquePlaceName);

				for (TimedToken token : timedPlace.tokens()) {
					place.addToken(new TimedToken(place, token.age()));
				}
			}
		}
	}

	private String getUniquePlaceName() {
		nextPlaceIndex++;
		return String.format(PLACE_FORMAT, nextPlaceIndex);
	}

	private void CreateTransitions(TimedArcPetriNetNetwork model,
			TimedArcPetriNet constructedModel, NameMapping mapping) {
		for (TimedArcPetriNet tapn : model.templates()) {
			for (TimedTransition timedTransition : tapn.transitions()) {
				String uniqueTransitionName = getUniqueTransitionName();

				constructedModel.add(new TimedTransition(uniqueTransitionName));
				mapping.addMapping(tapn.getName(), timedTransition.name(),
						uniqueTransitionName);
			}
		}
	}

	private String getUniqueTransitionName() {
		nextTransitionIndex++;
		return String.format(TRANSITION_FORMAT, nextTransitionIndex);
	}

	private void CreateInputArcs(TimedArcPetriNetNetwork model,
			TimedArcPetriNet constructedModel, NameMapping mapping) {
		for (TimedArcPetriNet tapn : model.templates()) {
			for (TimedInputArc arc : tapn.inputArcs()) {
				TimedPlace source = constructedModel.getPlaceByName(mapping.map(tapn.getName(), arc.source().name()));
				TimedTransition target = constructedModel.getTransitionByName(mapping.map(tapn.getName(), arc.destination().name()));

				constructedModel.add(new TimedInputArc(source, target, arc.interval()));
			}
		}
	}

	private void CreateOutputArcs(TimedArcPetriNetNetwork model,
			TimedArcPetriNet constructedModel, NameMapping mapping) {
		for (TimedArcPetriNet tapn : model.templates()) {
			for (TimedOutputArc arc : tapn.outputArcs()) {
				TimedTransition source = constructedModel.getTransitionByName(mapping.map(tapn.getName(), arc.source().name()));
				TimedPlace target = constructedModel.getPlaceByName(mapping.map(tapn.getName(), arc.destination().name()));

				constructedModel.add(new TimedOutputArc(source, target));
			}
		}
	}

	private void CreateTransportArcs(TimedArcPetriNetNetwork model,
			TimedArcPetriNet constructedModel, NameMapping mapping) {
		for (TimedArcPetriNet tapn : model.templates()) {
			for (TransportArc arc : tapn.transportArcs()) {
				TimedPlace source = constructedModel.getPlaceByName(mapping.map(tapn.getName(), arc.source().name()));
				TimedTransition transition = constructedModel.getTransitionByName(mapping.map(tapn.getName(), arc.transition().name()));
				TimedPlace target = constructedModel.getPlaceByName(mapping.map(tapn.getName(), arc.destination().name()));

				constructedModel.add(new TransportArc(source, transition,target, arc.interval()));
			}
		}
	}

	private void CreateInhibitorArcs(TimedArcPetriNetNetwork model,
			TimedArcPetriNet constructedModel, NameMapping mapping) {
		for (TimedArcPetriNet tapn : model.templates()) {
			for (TimedInhibitorArc arc : tapn.inhibitorArcs()) {
				TimedPlace source = constructedModel.getPlaceByName(mapping.map(tapn.getName(), arc.source().name()));
				TimedTransition target = constructedModel.getTransitionByName(mapping.map(tapn.getName(), arc.destination().name()));

				constructedModel.add(new TimedInhibitorArc(source, target, arc.interval()));
			}
		}
	}
}
