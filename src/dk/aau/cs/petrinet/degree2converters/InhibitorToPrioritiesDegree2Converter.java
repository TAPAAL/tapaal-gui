package dk.aau.cs.petrinet.degree2converters;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import dk.aau.cs.petrinet.Arc;
import dk.aau.cs.petrinet.Degree2Converter;
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

	protected static final String T_ZEROTEST_FORMAT = "%1$s_%2$s_zerotest";
	protected static final String T_CHECK_FORMAT = "%1$s_check";
	protected static final String P_CHECK_FORMAT = "P_%1$s_check";
	protected static final String T_MAX_FORMAT = "%1$s_%2$d";
	protected static final String T_I_OUT_FORMAT = "%1$s_%2$d_out";
	protected static final String T_I_IN_FORMAT = "%1$s_%2$d_in";
	protected static final String HOLDING_PLACE_FORMAT = "P_hp_%1$s_%2$d";
	protected static final String P_T_IN_FORMAT = "P_" + T_I_IN_FORMAT;
	protected static final String P_T_OUT_FORMAT = "P_" + T_I_OUT_FORMAT;
	protected static final String PLOCK = "P_lock";
	protected static final String PDEADLOCK = "P_deadlock";

	protected static final int LOW = 1;
	protected static final int HIGH = 2;

	protected static final String LT_INF = "<inf";
	protected static final String LTEQ_ZERO = "<=0";
	protected static final String ZERO_INF_GUARD = "[0,inf)";

	private Hashtable<String, PlaceTransitionObject> nameToPTO = new Hashtable<String, PlaceTransitionObject>();

	public TAPN transform(TAPN model) throws Exception { // TODO: use interface
															// instead of TAPN
		try {
			model.convertToConservative();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		// if(model.isDegree2() && model.getInhibitorArcs().size() == 0) return
		// model;

		TAPN tapn = new TAPN();

		createInitialPlaces(model, tapn);

		for (TAPNTransition transition : model.getTransitions()) {
			createSimulationOfTransition(transition, tapn);
		}

		List<Token> tokens = model.getTokens();
		for (Token token : tokens) {
			tapn.addToken(new Token((TAPNPlace) nameToPTO.get(token.place()
					.getName())));
		}

		tapn.addToken(new Token((TAPNPlace) nameToPTO.get(PLOCK)));

		nameToPTO.clear();
		return tapn;
	}

	protected void createInitialPlaces(TimedArcPetriNet model,
			TimedArcPetriNet degree2Net) {
		for (TAPNPlace p : model.getPlaces()) {
			addPlace(degree2Net, p.getName(), p.getInvariant(), p.getCapacity());
		}

		addPlace(degree2Net, PLOCK, LT_INF, 0);
		addPlace(degree2Net, PDEADLOCK, LTEQ_ZERO, 0);
	}

	protected void createSimulationOfTransition(TAPNTransition transition,
			TimedArcPetriNet degree2Net) throws Exception {
		if (transition.getPreset().size() == 0
				&& transition.getPostset().size() == 0) {
			// Transition only has inhibitor arcs. Ignore it because the answer
			// to the query cannot be affected by this
			return;
		}

		if (transition.getPreset().size() == 1) {
			createSimulationOfTransitionOfDegree1(transition, degree2Net);
		} else {
			createRingStructure(transition, degree2Net);
			createArcs(transition, degree2Net);
		}
		createInhibitorArcSimulation(transition, degree2Net);
	}

	private void createRingStructure(TAPNTransition transition,
			TimedArcPetriNet degree2Net) throws Exception {
		String transitionName = transition.getName();
		int transitionsToCreate = 2 * transition.getPreset().size() - 1;
		String lastPTO = null;

		// create t^i_in (with corresponding places)
		for (int i = 1; i <= (transitionsToCreate - 1) / 2; i++) {
			String tiin = String.format(T_I_IN_FORMAT, transitionName, i);
			addTransition(degree2Net, tiin, LOW);

			String pt = String.format(P_T_IN_FORMAT, transitionName, i);
			addPlace(degree2Net, pt, LTEQ_ZERO, 0);

			addNormalArc(degree2Net, tiin, pt);
			if (lastPTO != null) {
				addTAPNArc(degree2Net, lastPTO, tiin, ZERO_INF_GUARD);
			}
			lastPTO = pt;

			String holdingPlace = String.format(HOLDING_PLACE_FORMAT,
					transitionName, i);
			addPlace(degree2Net, holdingPlace, LT_INF, 0);
		}

		// Create t^max(t)
		String tmax = String.format(T_MAX_FORMAT, transitionName,
				transitionsToCreate / 2 + 1);
		addTransition(degree2Net, tmax, LOW);

		addTAPNArc(degree2Net, lastPTO, tmax, ZERO_INF_GUARD);
		lastPTO = tmax;

		// Create t^i_out (with corresponding places)
		for (int i = (transitionsToCreate - 1) / 2; i >= 1; i--) {
			String tiout = String.format(T_I_OUT_FORMAT, transitionName, i);
			addTransition(degree2Net, tiout, LOW);

			String pt = String.format(P_T_OUT_FORMAT, transitionName, i);
			addPlace(degree2Net, pt, LTEQ_ZERO, 0);

			addTAPNArc(degree2Net, pt, tiout, ZERO_INF_GUARD);
			addNormalArc(degree2Net, lastPTO, pt);

			lastPTO = tiout;
		}

		addNormalArc(degree2Net, lastPTO, PLOCK);
	}

	private void createSimulationOfTransitionOfDegree1(
			TAPNTransition transition, TimedArcPetriNet degree2Net)
			throws Exception {
		String trans = String.format(T_MAX_FORMAT, transition.getName(), 1);
		addTransition(degree2Net, trans, LOW);

		Arc presetArc = transition.getPreset().get(0);
		Arc postsetArc = transition.getPostset().get(0);
		if (presetArc instanceof TAPNTransportArc) {
			addTransportArc(degree2Net, presetArc.getSource().getName(), trans,
					presetArc.getTarget().getName(), createGuard(
							((TAPNTransportArc) presetArc).getGuard(),
							(TAPNPlace) presetArc.getTarget(), true));
		} else {
			addTAPNArc(degree2Net, presetArc.getSource().getName(), trans,
					((TAPNArc) presetArc).getGuard());
			addNormalArc(degree2Net, trans, postsetArc.getTarget().getName());
		}

		addNormalArc(degree2Net, trans, PLOCK);
	}

	private void createArcs(TAPNTransition transition,
			TimedArcPetriNet degree2Net) throws Exception {
		HashSet<Arc> usedFromPostSet = new HashSet<Arc>();

		for (int i = 1; i <= transition.getPreset().size(); i++) {
			Arc presetArc = transition.getPreset().get(i - 1);

			if (presetArc instanceof TAPNTransportArc) {
				addSimulationOfPresetPostsetPairing(degree2Net, transition, i,
						(TAPNTransportArc) presetArc);
				usedFromPostSet.add(presetArc);
			} else {
				for (Arc postsetArc : transition.getPostset()) {
					if (!usedFromPostSet.contains(postsetArc)
							&& !(postsetArc instanceof TAPNTransportArc)) {
						addSimulationOfPresetPostsetPairing(degree2Net,
								transition, i, (TAPNArc) presetArc, postsetArc);
						usedFromPostSet.add(postsetArc);
						break;
					}
				}
			}
		}
	}

	private void addSimulationOfPresetPostsetPairing(TimedArcPetriNet degree2Net, TAPNTransition transition, int i,	TAPNArc presetArc, Arc postsetArc) throws Exception {
		String transitionName = transition.getName();

		if (i == transition.getPreset().size()) {
			addTAPNArc(degree2Net, presetArc.getSource().getName(), String.format(T_MAX_FORMAT, transitionName, i), 
					createGuard(presetArc.getGuard(), (TAPNPlace) postsetArc.getTarget(), presetArc instanceof TAPNTransportArc));
			
			addNormalArc(degree2Net, String.format(T_MAX_FORMAT,transitionName, i), postsetArc.getTarget().getName());
		} else {
			String trans = String.format(T_I_IN_FORMAT, transitionName, i);
			String pholding = String.format(HOLDING_PLACE_FORMAT, transitionName, i);

			addTAPNArc(
					degree2Net,
					presetArc.getSource().getName(),
					trans,
					createGuard(presetArc.getGuard(), (TAPNPlace) postsetArc.getTarget(), presetArc instanceof TAPNTransportArc));
			addNormalArc(degree2Net, trans, pholding);

			trans = String.format(T_I_OUT_FORMAT, transitionName, i);
			addTAPNArc(degree2Net, pholding, trans, ZERO_INF_GUARD);
			addNormalArc(degree2Net, trans, postsetArc.getTarget().getName());
		}
	}

	protected String createGuard(String guard, TAPNPlace target,
			boolean isTransportArc) {
		return guard;
	}

	private void addSimulationOfPresetPostsetPairing(
			TimedArcPetriNet degree2Net, TAPNTransition transition, int i,
			TAPNTransportArc presetArc) throws Exception {
		String transitionName = transition.getName();
		if (i == transition.getPreset().size()) {
			addTransportArc(degree2Net, presetArc.getSource().getName(), String
					.format(T_MAX_FORMAT, transitionName, i), presetArc
					.getTarget().getName(), createGuard(presetArc.getGuard(),
					presetArc.getTarget(), true));
		} else {
			String trans = String.format(T_I_IN_FORMAT, transitionName, i);
			String pholding = String.format(HOLDING_PLACE_FORMAT,
					transitionName, i);

			addTransportArc(degree2Net, presetArc.getSource().getName(), trans,
					pholding, createGuard(presetArc.getGuard(), presetArc
							.getTarget(), true));

			trans = String.format(T_I_OUT_FORMAT, transitionName, i);
			addTransportArc(degree2Net, pholding, trans, presetArc.getTarget()
					.getName(), createGuard(presetArc.getGuard(), presetArc
					.getTarget(), true));
		}
	}

	protected void createInhibitorArcSimulation(TAPNTransition transition,
			TimedArcPetriNet degree2Net) throws Exception {
		String transitionName = transition.getName();

		if (transition.hasInhibitorArcs()) {
			String pcheck = String.format(P_CHECK_FORMAT, transitionName);
			addPlace(degree2Net, pcheck, LTEQ_ZERO, 0);

			String tcheck = String.format(T_CHECK_FORMAT, transitionName);
			addTransition(degree2Net, tcheck, LOW);

			addTAPNArc(degree2Net, PLOCK, tcheck, ZERO_INF_GUARD);

			String tiin = transition.getPreset().size() == 1 ? String.format(
					T_MAX_FORMAT, transitionName, 1) : String.format(
					T_I_IN_FORMAT, transitionName, 1);
			addTAPNArc(degree2Net, pcheck, tiin, ZERO_INF_GUARD);
			addNormalArc(degree2Net, tcheck, pcheck);

			for (TAPNInhibitorArc inhib : transition.getInhibitorArcs()) {
				String zerotest = String.format(T_ZEROTEST_FORMAT,
						transitionName, inhib.getSource().getName());
				addTransition(degree2Net, zerotest, HIGH);

				addTAPNArc(degree2Net, pcheck, zerotest, ZERO_INF_GUARD);
				addNormalArc(degree2Net, zerotest, PDEADLOCK);
				addTransportArc(degree2Net, inhib.getSource().getName(),
						zerotest, inhib.getSource().getName(), inhib.getGuard());
			}
		} else {
			String tiin = transition.getPreset().size() == 1 ? String.format(
					T_MAX_FORMAT, transitionName, 1) : String.format(
					T_I_IN_FORMAT, transitionName, 1);
			addTAPNArc(degree2Net, PLOCK, tiin, ZERO_INF_GUARD);
		}
	}

	/*
	 * Helper methods to create arcs, places and transitions from parameters
	 */

	protected void addTransportArc(TimedArcPetriNet degree2Net, String source,
			String intermediateTransition, String target, String guard)
			throws Exception {
		TAPNTransportArc arc = new TAPNTransportArc(
				(TAPNPlace) getByName(source),
				(TAPNTransition) getByName(intermediateTransition),
				(TAPNPlace) getByName(target), guard);
		degree2Net.addArc(arc);
	}

	protected void addNormalArc(TimedArcPetriNet degree2Net, String source,
			String target) throws Exception {
		Arc arc = new Arc(getByName(source), getByName(target));
		degree2Net.addArc(arc);
	}

	protected void addTAPNArc(TimedArcPetriNet degree2Net, String source,
			String target, String guard) throws Exception {
		TAPNArc arc = new TAPNArc(getByName(source), getByName(target), guard);
		degree2Net.addArc(arc);
	}

	protected void addTransition(TimedArcPetriNet degree2Net, String name,
			int priority) {
		TAPNTransition t = new PrioritizedTAPNTransition(name, priority);
		degree2Net.addTransition(t);
		nameToPTO.put(name, t);
	}

	protected void addPlace(TimedArcPetriNet degree2Net, String name,
			String invariant, int capacity) {
		TAPNPlace place = new TAPNPlace(name, invariant, capacity);
		degree2Net.addPlace(place);
		nameToPTO.put(name, place);
	}

	protected PlaceTransitionObject getByName(String name) {
		return nameToPTO.get(name);
	}
}
