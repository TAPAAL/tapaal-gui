package dk.aau.cs.verification;

import dk.aau.cs.petrinet.TAPNPlace;

public class NewModelToOldModelTransformer {

	public dk.aau.cs.petrinet.TimedArcPetriNet transformModel(dk.aau.cs.model.tapn.TimedArcPetriNet model) {
		dk.aau.cs.petrinet.TAPN constructedModel = new dk.aau.cs.petrinet.TAPN();

		try{
			CreatePlaces(model, constructedModel);
			CreateTransitions(model, constructedModel);
			CreateInputArcs(model, constructedModel);
			CreateOutputArcs(model, constructedModel);
			CreateTransportArcs(model, constructedModel);
			CreateInhibitorArcs(model, constructedModel);
		}catch(Exception e){
			return null;
		}
		
		return constructedModel;
	}

	private void CreatePlaces(dk.aau.cs.model.tapn.TimedArcPetriNet model, dk.aau.cs.petrinet.TAPN constructedModel) {
		for(dk.aau.cs.model.tapn.TimedPlace place : model.places()){
			dk.aau.cs.petrinet.TAPNPlace tapnPlace = new TAPNPlace(place.name(), place.invariant().toString(), 0);
			constructedModel.addPlace(tapnPlace);

			for(dk.aau.cs.model.tapn.TimedToken token: place.tokens()){
				constructedModel.addToken(new dk.aau.cs.petrinet.Token(tapnPlace, token.age()));
			}
		}
	}

	private void CreateTransitions(dk.aau.cs.model.tapn.TimedArcPetriNet model, dk.aau.cs.petrinet.TAPN constructedModel) {
		for(dk.aau.cs.model.tapn.TimedTransition timedTransition : model.transitions()){
			dk.aau.cs.petrinet.TAPNTransition tapnTransition = new dk.aau.cs.petrinet.TAPNTransition(timedTransition.name());
			constructedModel.addTransition(tapnTransition);
		}		
	}

	private void CreateInputArcs(dk.aau.cs.model.tapn.TimedArcPetriNet model, dk.aau.cs.petrinet.TAPN constructedModel) throws Exception {
		for(dk.aau.cs.model.tapn.TimedInputArc arc : model.inputArcs()){
			dk.aau.cs.petrinet.TAPNPlace source = constructedModel.getPlaceByName(arc.source().name());
			dk.aau.cs.petrinet.TAPNTransition target = constructedModel.getTransitionsByName(arc.destination().name());

			dk.aau.cs.petrinet.TAPNArc tapnArc = new dk.aau.cs.petrinet.TAPNArc(source, target, arc.interval().toString());
			constructedModel.add(tapnArc);
		}
	}

	private void CreateOutputArcs(dk.aau.cs.model.tapn.TimedArcPetriNet model, dk.aau.cs.petrinet.TAPN constructedModel) throws Exception {
		for(dk.aau.cs.model.tapn.TimedOutputArc arc : model.outputArcs()){
			dk.aau.cs.petrinet.TAPNTransition source = constructedModel.getTransitionsByName(arc.source().name());
			dk.aau.cs.petrinet.TAPNPlace target = constructedModel.getPlaceByName(arc.destination().name());

			dk.aau.cs.petrinet.Arc normalArc = new dk.aau.cs.petrinet.Arc(source, target);
			constructedModel.add(normalArc);
		}
	}

	private void CreateTransportArcs(dk.aau.cs.model.tapn.TimedArcPetriNet model, dk.aau.cs.petrinet.TAPN constructedModel) throws Exception {
		for(dk.aau.cs.model.tapn.TransportArc arc : model.transportArcs()){
			dk.aau.cs.petrinet.TAPNPlace source = constructedModel.getPlaceByName(arc.source().name());
			dk.aau.cs.petrinet.TAPNTransition transition = constructedModel.getTransitionsByName(arc.transition().name());
			dk.aau.cs.petrinet.TAPNPlace target = constructedModel.getPlaceByName(arc.destination().name());

			dk.aau.cs.petrinet.TAPNTransportArc transportArc = new dk.aau.cs.petrinet.TAPNTransportArc(source, transition, target, arc.timeInterval().toString());
			constructedModel.add(transportArc);
		}
	}

	private void CreateInhibitorArcs(dk.aau.cs.model.tapn.TimedArcPetriNet model, dk.aau.cs.petrinet.TAPN constructedModel) throws Exception {
		for(dk.aau.cs.model.tapn.TimedInhibitorArc arc : model.inhibitorArcs()){
			dk.aau.cs.petrinet.TAPNPlace source = constructedModel.getPlaceByName(arc.source().name());
			dk.aau.cs.petrinet.TAPNTransition target = constructedModel.getTransitionsByName(arc.destination().name());

			dk.aau.cs.petrinet.TAPNInhibitorArc inhibitorArc = new dk.aau.cs.petrinet.TAPNInhibitorArc(source, target, arc.interval().toString());
			constructedModel.add(inhibitorArc);
		}
	}
}
