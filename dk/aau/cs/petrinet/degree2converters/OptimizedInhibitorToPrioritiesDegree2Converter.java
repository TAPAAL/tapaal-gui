package dk.aau.cs.petrinet.degree2converters;

import dk.aau.cs.petrinet.Arc;
import dk.aau.cs.petrinet.TAPNArc;
import dk.aau.cs.petrinet.TAPNTransition;
import dk.aau.cs.petrinet.TAPNTransportArc;
import dk.aau.cs.petrinet.TimedArcPetriNet;

// TODO: Merge this (and base type) into InhibDegree2Converter since this is the only place it's used
public class OptimizedInhibitorToPrioritiesDegree2Converter extends
		InhibitorToPrioritiesDegree2Converter {
	@Override
	protected void createSimulationOfTransition(TAPNTransition transition,
			TimedArcPetriNet degree2Net) throws Exception {
		createSimulationOfTransition(transition, degree2Net, true);
	}

	protected void createSimulationOfTransition(TAPNTransition transition,
			TimedArcPetriNet degree2Net, boolean optimize) throws Exception {
		if (optimize && transition.isDegree2()
				&& transition.getInhibitorArcs().size() == 0) {
			createOptimizedSimulation(transition, degree2Net);
		} else {
			super.createSimulationOfTransition(transition, degree2Net);
		}
	}

	protected void createOptimizedSimulation(TAPNTransition transition,
			TimedArcPetriNet degree2Net) throws Exception {
		addTransition(degree2Net, transition.getName(), LOW);
		TAPNTransition newTransition = (TAPNTransition) getByName(transition
				.getName());
		newTransition.setFromOriginalNet(true);

		for (Arc arc : transition.getPreset()) {
			if (arc instanceof TAPNTransportArc) {
				TAPNTransportArc tpa = (TAPNTransportArc) arc;
				addTransportArc(degree2Net, tpa.getSource().getName(), tpa
						.getIntermediate().getName(),
						tpa.getTarget().getName(), tpa.getGuard());
			} else {
				TAPNArc tapnArc = (TAPNArc) arc;
				addTAPNArc(degree2Net, tapnArc.getSource().getName(), tapnArc
						.getTarget().getName(), tapnArc.getGuard());
			}
		}

		for (Arc arc : transition.getPostset()) {
			if (!(arc instanceof TAPNTransportArc)) {
				addNormalArc(degree2Net, arc.getSource().getName(), arc
						.getTarget().getName());
			}
		}
	}
}
