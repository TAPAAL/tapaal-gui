package dk.aau.cs.petrinet.degree2converters;

import dk.aau.cs.petrinet.PetriNetUtil;
import dk.aau.cs.petrinet.TAPNPlace;
import dk.aau.cs.petrinet.TAPNTransition;
import dk.aau.cs.petrinet.TimedArcPetriNet;


public class InhibDegree2Converter extends OptimizedInhibitorToPrioritiesDegree2Converter {
	protected void createInitialPlaces(TimedArcPetriNet model, TimedArcPetriNet degree2Net) {
		super.createInitialPlaces(model, degree2Net);
		degree2Net.removePlaceByName(PDEADLOCK);
	}

	protected void createSimulationOfTransition(TAPNTransition transition, TimedArcPetriNet degree2Net) throws Exception {
		//		if(transition.isDegree2() && transition.getInhibitorArcs().size() == 0){
		//			createOptimizedSimulation(transition, degree2Net);
		//		}else{
		super.createSimulationOfTransition(transition, degree2Net, false);
		//		}
	}

	protected void createInhibitorArcSimulation(TAPNTransition transition, TimedArcPetriNet degree2Net) throws Exception {
		String transitionName = transition.getName();

		String tiin = transition.getPreset().size() == 1 ? String.format(T_MAX_FORMAT, transitionName, 1) 
				: String.format(T_I_IN_FORMAT, transitionName, 1);
		addTAPNArc(degree2Net, PLOCK, tiin, ZERO_INF_GUARD);
	}

	protected String createGuard(String guard, TAPNPlace target, boolean isTransportArc) {
		return PetriNetUtil.createGuard(guard, target, isTransportArc);
	}
}
