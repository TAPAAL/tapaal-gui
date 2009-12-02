package dk.aau.cs.petrinet.degree2converters;

import java.util.Hashtable;

import dk.aau.cs.petrinet.Arc;
import dk.aau.cs.petrinet.Degree2Converter;
import dk.aau.cs.petrinet.Place;
import dk.aau.cs.petrinet.PlaceTransitionObject;
import dk.aau.cs.petrinet.PrioritizedTAPNTransition;
import dk.aau.cs.petrinet.TAPN;
import dk.aau.cs.petrinet.TAPNArc;
import dk.aau.cs.petrinet.TAPNInhibitorArc;
import dk.aau.cs.petrinet.TAPNPlace;
import dk.aau.cs.petrinet.TAPNTransition;
import dk.aau.cs.petrinet.TAPNTransportArc;
import dk.aau.cs.petrinet.TimedArcPetriNet;
import dk.aau.cs.petrinet.Token;

public class InhibitorToPrioritiesDegree2Converter implements Degree2Converter {

	private static final String T_ZEROTEST_FORMAT = "%1$s_%2$s_zerotest";
	private static final String T_CHECK_FORMAT = "%1$s_check";
	private static final String P_CHECK_FORMAT = "P_%1$s_check";
	private static final String T_I_OUT_FORMAT = "%1$s_%2$d_out";
	private static final String T_I_IN_FORMAT = "%1$s_%2$d_in";
	private static final String T_MAX_FORMAT = "%1$s_%2$d";
	private static final String HOLDING_PLACE_FORMAT = "P_hp_%1$s_%2$d";
	private static final String P_T_IN_FORMAT = "P_" + T_I_IN_FORMAT;
	private static final String P_T_OUT_FORMAT = "P_" + T_I_OUT_FORMAT;
	private static final String PLOCK = "P_lock";
	private static final String PCAPACITY = "P_capacity";

	private static final int LOW = 1;
	private static final int HIGH = 2;

	private static final String LT_INF = "<inf";
	private static final String LTEQ_ZERO = "<=0";
	private static final String ZERO_INF_GUARD = "[0,inf)";

	private Hashtable<String, PlaceTransitionObject> nameToPTO = new Hashtable<String, PlaceTransitionObject>();

	@Override
	public TAPN transform(TAPN model) throws Exception { // TODO: use interface instead of TAPN
		if(model.isDegree2()) return model;

		TAPN tapn = new TAPN(); 

		createInitialPlaces(model, tapn);

		for(TAPNTransition transition : model.getTransitions()){
			createSimulationOfTransition(transition, tapn);
		}
		
		for(Token token : model.getTokens()){
			tapn.addToken(new Token((Place)nameToPTO.get(token.getPlace().getName())));
		}
		
		tapn.addToken(new Token((Place)nameToPTO.get(PLOCK)));

		nameToPTO.clear();
		return tapn;
	}

	private void createInitialPlaces(TimedArcPetriNet model, TimedArcPetriNet degree2Net) {
		for(TAPNPlace p : model.getPlaces()){
			addPlace(degree2Net, p.getName(), p.getInvariant(), p.getCapacity());			
		}

		addPlace(degree2Net, PLOCK, LT_INF, 0);
	}

	private void createSimulationOfTransition(TAPNTransition transition, TimedArcPetriNet degree2Net) throws Exception {
		createRingStructure(transition, degree2Net);
		createInhibitorArcSimulation(transition, degree2Net);
		createArcsForPreset(transition, degree2Net);
		createArcsForPostset(transition, degree2Net);
	}

	private void createRingStructure(TAPNTransition transition, TimedArcPetriNet degree2Net) throws Exception {
		String transitionName = transition.getName();
		int transitionsToCreate = 2 * transition.getPreset().size() - 1;
		String lastPTO = null;		
		
		// create t^i_in (with corresponding places)
		for(int i = 1; i <= (transitionsToCreate-1)/2; i++){
			String tiin = String.format(T_I_IN_FORMAT, transitionName, i);
			addTransition(degree2Net, tiin, LOW);

			String pt = String.format(P_T_IN_FORMAT,transitionName, i);
			addPlace(degree2Net, pt, LTEQ_ZERO, 0);

			addNormalArc(degree2Net, tiin, pt);
			if(lastPTO != null){
				addTAPNArc(degree2Net, lastPTO, tiin, ZERO_INF_GUARD);
			}
			lastPTO = pt;

			String holdingPlace = String.format(HOLDING_PLACE_FORMAT, transitionName,i);
			addPlace(degree2Net, holdingPlace, LT_INF, 0);
		}


		// Create t^max(t)
		String tmax = String.format(T_MAX_FORMAT,transitionName, transitionsToCreate/2 + 1);
		addTransition(degree2Net, tmax, LOW);

		addTAPNArc(degree2Net, lastPTO, tmax, ZERO_INF_GUARD);
		lastPTO = tmax;

		// Create t^i_out (with corresponding places)
		for(int i = (transitionsToCreate-1)/2; i >= 1; i--){
			String tiout = String.format(T_I_OUT_FORMAT, transitionName, i);
			addTransition(degree2Net, tiout, LOW);

			String pt = String.format(P_T_OUT_FORMAT, transitionName,i);
			addPlace(degree2Net, pt, LTEQ_ZERO, 0);

			addTAPNArc(degree2Net, pt, tiout, ZERO_INF_GUARD);			
			addNormalArc(degree2Net, lastPTO, pt);

			lastPTO = tiout;
		}

		addNormalArc(degree2Net, lastPTO, PLOCK);
	}

	private void createArcsForPostset(TAPNTransition transition, TimedArcPetriNet degree2Net) throws Exception {
		String transitionName = transition.getName();
		
		for(int i = 0; i < transition.getPostset().size(); i++){
			Arc arc = transition.getPostset().get(i);

			if(!(arc instanceof TAPNTransportArc)){
				String tiout = i == transition.getPostset().size() - 1 ? String.format(T_MAX_FORMAT, transitionName, i+1)
						: String.format(T_I_OUT_FORMAT, transitionName, i+1);
				if(i < transition.getPostset().size()-1){
					addTAPNArc(degree2Net, 
							String.format(HOLDING_PLACE_FORMAT, transitionName, i+1),
							tiout, 
							ZERO_INF_GUARD);
				}
				addNormalArc(degree2Net, 
						tiout,
						arc.getTarget().getName());				
			}
		}
	}

	private void createArcsForPreset(TAPNTransition transition, TimedArcPetriNet degree2Net) throws Exception {
		String transitionName = transition.getName();
		for(int i = 0; i < transition.getPreset().size(); i++){
			Arc arc = transition.getPreset().get(i);

			String tiin = i == transition.getPreset().size()-1 ? String.format(T_MAX_FORMAT, transitionName, i+1) 
					: String.format(T_I_IN_FORMAT, transitionName, i+1);
			String pholding = String.format(HOLDING_PLACE_FORMAT, transitionName, i+1);

			if(arc instanceof TAPNTransportArc){
				addTransportArc(degree2Net, 
						arc.getSource().getName(),
						tiin,
						pholding,
						((TAPNTransportArc)arc).getGuard());
				addTransportArc(degree2Net, 
						pholding,
						String.format(T_I_OUT_FORMAT, transitionName, i+1),
						arc.getTarget().getName(),
						ZERO_INF_GUARD);
			}else{
				addTAPNArc(degree2Net, 
						arc.getSource().getName(),
						tiin, 
						((TAPNArc)arc).getGuard());

				if(i < transition.getPreset().size()-1){
					addNormalArc(degree2Net, 
							tiin,
							pholding);
				}
			}
		}
	}

	private void createInhibitorArcSimulation(TAPNTransition transition, TimedArcPetriNet degree2Net) throws Exception {
		String transitionName = transition.getName();
		
		if(transition.hasInhibitorArcs()){
			String pcheck = String.format(P_CHECK_FORMAT,transitionName);
			addPlace(degree2Net, pcheck, LTEQ_ZERO, 0);

			String tcheck = String.format(T_CHECK_FORMAT, transitionName);
			addTransition(degree2Net, tcheck, LOW);

			addTAPNArc(degree2Net, PLOCK, tcheck, ZERO_INF_GUARD);

			String tiin = String.format(T_I_IN_FORMAT, transitionName, 1);
			addTAPNArc(degree2Net, pcheck, tiin, ZERO_INF_GUARD);
			addNormalArc(degree2Net, tcheck, pcheck);

			for(TAPNInhibitorArc inhib : transition.getInhibitorArcs()){
				String zerotest = String.format(T_ZEROTEST_FORMAT,transitionName, inhib.getSource().getName());
				addTransition(degree2Net, zerotest, HIGH);

				addTAPNArc(degree2Net, pcheck, zerotest, ZERO_INF_GUARD);
				addNormalArc(degree2Net, zerotest, PCAPACITY);
				addTransportArc(degree2Net, inhib.getSource().getName(), zerotest, inhib.getSource().getName(), inhib.getGuard());
			}			
		}else{
			String tiin = String.format(T_I_IN_FORMAT, transitionName, 1);
			addTAPNArc(degree2Net, PLOCK, tiin, ZERO_INF_GUARD);
		}
	}
	
	
	/*
	 * Helper methods to create arcs, places and transitions from parameters
	 */

	private void addTransportArc(TimedArcPetriNet degree2Net, String source, String intermediateTransition,
			String target, String guard) throws Exception {
		TAPNTransportArc arc = new TAPNTransportArc(
				(TAPNPlace)getByName(source), 
				(TAPNTransition)getByName(intermediateTransition), 
				(TAPNPlace)getByName(target), 
				guard);
		degree2Net.addArc(arc);
	}

	private void addNormalArc(TimedArcPetriNet degree2Net, String source, String target) throws Exception {
		Arc arc = new Arc(getByName(source), getByName(target));
		degree2Net.addArc(arc);
	}

	private void addTAPNArc(TimedArcPetriNet degree2Net, String source, String target, String guard)
	throws Exception {
		TAPNArc arc = new TAPNArc(getByName(source), getByName(target), guard);
		degree2Net.addArc(arc);
	}

	private void addTransition(TimedArcPetriNet degree2Net, String name, int priority) {
		TAPNTransition t = new PrioritizedTAPNTransition(name,priority);
		degree2Net.addTransition(t);
		nameToPTO.put(name, t);
	}

	private void addPlace(TimedArcPetriNet degree2Net, String name, String invariant, int capacity) {
		TAPNPlace place = new TAPNPlace(name, invariant, capacity);
		degree2Net.addPlace(place);
		nameToPTO.put(name, place);
	}

	private PlaceTransitionObject getByName(String name) {
		return nameToPTO.get(name);
	}
}
