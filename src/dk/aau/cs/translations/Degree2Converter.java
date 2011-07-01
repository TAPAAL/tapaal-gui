package dk.aau.cs.translations;

import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.model.tapn.IntBound;
import dk.aau.cs.model.tapn.LocalTimedPlace;
import dk.aau.cs.model.tapn.TimeInterval;
import dk.aau.cs.model.tapn.TimeInvariant;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedInhibitorArc;
import dk.aau.cs.model.tapn.TimedInputArc;
import dk.aau.cs.model.tapn.TimedOutputArc;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedToken;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.model.tapn.TransportArc;
import dk.aau.cs.translations.tapn.TAPNToConservativeTAPNConverter;

public class Degree2Converter {

	protected static final String T_MAX_FORMAT = "%1$s_%2$d";
	protected static final String T_I_OUT_FORMAT = "%1$s_%2$d_out";
	protected static final String T_I_IN_FORMAT = "%1$s_%2$d_in";
	protected static final String HOLDING_PLACE_FORMAT = "P_hp_%1$s_%2$d";
	protected static final String P_T_IN_FORMAT = "P_" + T_I_IN_FORMAT;
	protected static final String P_T_OUT_FORMAT = "P_" + T_I_OUT_FORMAT;
	protected static final String PLOCK = "P_lock";
	
	private TimedArcPetriNet degree2Model;
	private TimedArcPetriNet conservativeModel;
	
	private List<TimedTransition> retainedDegree2Transitions = new ArrayList<TimedTransition>();

	public TimedArcPetriNet transformModel(TimedArcPetriNet model) throws Exception {
		try{
			TAPNToConservativeTAPNConverter converter = new TAPNToConservativeTAPNConverter();
			conservativeModel = converter.makeConservative(model.copy());
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
		
		degree2Model = new TimedArcPetriNet(conservativeModel.name() + "_degree2");
		
		for(TimedPlace p : conservativeModel.places()) {
			TimedPlace copy = p.copy();
			degree2Model.add(copy);
			for(int i = 0; i < p.numberOfTokens(); i++) {
				degree2Model.addToken(new TimedToken(copy));
			}
		}
		
		TimedPlace plock = new LocalTimedPlace(PLOCK);
		degree2Model.add(plock);
		
		degree2Model.addToken(new TimedToken(plock));

		
		for(TimedTransition t : conservativeModel.transitions())
			createDegree2TransitionSimulation(t);
		
		return degree2Model;
		
	}
	
	public List<TimedTransition> getRetainedTransitions() {
		return retainedDegree2Transitions;
	}

	protected void createDegree2TransitionSimulation(TimedTransition t) {
		Pairing p = new Pairing(t);
		
		if((isTransitionDegree1(t) || isTransitionDegree2(t)) && !t.hasInhibitorArcs())
			createCopyOfOriginalTransition(t, p);
		else
			createTransitionSimulation(t,p);
	}



	private boolean isTransitionDegree1(TimedTransition t) {
		return t.presetSize() == 1 && t.postsetSize() == 1;
	}
	
	private boolean isTransitionDegree2(TimedTransition t) {
		return t.presetSize() == 2 && t.postsetSize() == 2;
	}	

	private void createCopyOfOriginalTransition(TimedTransition transition, Pairing pairing) {
		TimedTransition t = new TimedTransition(transition.name());
		degree2Model.add(t);
		retainedDegree2Transitions.add(t);
		
		for(TimedInputArc inputArc : transition.getInputArcs()) {
			degree2Model.add(new TimedInputArc(degree2Model.getPlaceByName(inputArc.source().name()), t, inputArc.interval().copy()));
			degree2Model.add(new TimedOutputArc(t, degree2Model.getPlaceByName(pairing.getOutputArcFor(inputArc).destination().name())));
		}
		
		for(TransportArc transArc : transition.getTransportArcsGoingThrough()) {
			degree2Model.add(new TransportArc(degree2Model.getPlaceByName(transArc.source().name()), t, degree2Model.getPlaceByName(transArc.destination().name()), transArc.interval().copy()));
		}
		
	}
	
	protected void createTransitionSimulation(TimedTransition t, Pairing p) {
		createRingStructure(t, p);
		createArcsFromInputToOutputAccordingToPairing(t,p);
		createInhibitorArcs(t);
	}

	private void createRingStructure(TimedTransition transition, Pairing p) {
		String transitionName = transition.name();
		int transitionsToCreate = 2 * transition.presetSize() - 1;
		TimedPlace previousPlace = null;
		
		// create t^i_in (with corresponding places)
		for (int i = 1; i <= (transitionsToCreate - 1) / 2; i++) {
			String tiinName = String.format(T_I_IN_FORMAT, transitionName, i);
			TimedTransition tiin = new TimedTransition(tiinName);
			degree2Model.add(tiin);
			
			if(previousPlace == null) {
				degree2Model.add(new TimedInputArc(degree2Model.getPlaceByName(PLOCK), tiin, TimeInterval.ZERO_INF));
			}

			String ptName = String.format(P_T_IN_FORMAT, transitionName, i);
			LocalTimedPlace pt = new LocalTimedPlace(ptName, new TimeInvariant(true, new IntBound(0)));
			degree2Model.add(pt);

			degree2Model.add(new TimedOutputArc(tiin, pt));
			if (previousPlace != null) {
				degree2Model.add(new TimedInputArc(previousPlace, tiin, TimeInterval.ZERO_INF));
			}
			previousPlace = pt;

			String holdingPlaceName = String.format(HOLDING_PLACE_FORMAT, transitionName, i);
			degree2Model.add(new LocalTimedPlace(holdingPlaceName, TimeInvariant.LESS_THAN_INFINITY));
		}

		// Create t^max(t)
		String tmaxName = String.format(T_MAX_FORMAT, transitionName, transitionsToCreate / 2 + 1);
		TimedTransition tmax = new TimedTransition(tmaxName);
		degree2Model.add(tmax);

		if(previousPlace != null)
			degree2Model.add(new TimedInputArc(previousPlace, tmax, TimeInterval.ZERO_INF));
		else
			degree2Model.add(new TimedInputArc(degree2Model.getPlaceByName(PLOCK), tmax, TimeInterval.ZERO_INF));
			
		TimedTransition previousTransition = tmax;

		// Create t^i_out (with corresponding places)
		for (int i = (transitionsToCreate - 1) / 2; i >= 1; i--) {
			String tiOutName = String.format(T_I_OUT_FORMAT, transitionName, i);
			TimedTransition tiOut = new TimedTransition(tiOutName);
			degree2Model.add(tiOut);

			String ptOutName = String.format(P_T_OUT_FORMAT, transitionName, i);
			TimedPlace ptOut = new LocalTimedPlace(ptOutName, new TimeInvariant(true, new IntBound(0)));
			degree2Model.add(ptOut);

			degree2Model.add(new TimedOutputArc(previousTransition, ptOut));
			degree2Model.add(new TimedInputArc(ptOut, tiOut, TimeInterval.ZERO_INF));

			previousTransition = tiOut;
		}
		
		degree2Model.add(new TimedOutputArc(previousTransition, degree2Model.getPlaceByName(PLOCK)));
	}
	
	private void createArcsFromInputToOutputAccordingToPairing(TimedTransition transition, Pairing pairing) {
		int numPresetArcs = 1;
		String transitionName = transition.name();
		for(TimedInputArc inputArc : transition.getInputArcs()) {
			if (numPresetArcs == transition.presetSize()) {
				TimedTransition tmax = degree2Model.getTransitionByName(String.format(T_MAX_FORMAT, transitionName, numPresetArcs));
				degree2Model.add(new TimedInputArc(degree2Model.getPlaceByName(inputArc.source().name()), tmax, inputArc.interval().copy()));				
				degree2Model.add(new TimedOutputArc(tmax, degree2Model.getPlaceByName(pairing.getOutputArcFor(inputArc).destination().name())));
			} else {
				TimedPlace inputPlace = degree2Model.getPlaceByName(inputArc.source().name());
				TimedTransition tiIn = degree2Model.getTransitionByName(String.format(T_I_IN_FORMAT, transitionName, numPresetArcs));
				TimedPlace pHolding = degree2Model.getPlaceByName(String.format(HOLDING_PLACE_FORMAT, transitionName, numPresetArcs));

				degree2Model.add(new TimedInputArc(inputPlace, tiIn, inputArc.interval().copy()));
				degree2Model.add(new TimedOutputArc(tiIn, pHolding));

				TimedTransition tiOut = degree2Model.getTransitionByName(String.format(T_I_OUT_FORMAT, transitionName, numPresetArcs));
				TimedPlace outputPlace = degree2Model.getPlaceByName(pairing.getOutputArcFor(inputArc).destination().name());
				
				degree2Model.add(new TimedInputArc(pHolding, tiOut, TimeInterval.ZERO_INF));
				degree2Model.add(new TimedOutputArc(tiOut, outputPlace));
			}
			
			numPresetArcs++;
		}
		
		for(TransportArc transArc : transition.getTransportArcsGoingThrough()) {
			if (numPresetArcs == transition.presetSize()) {
				degree2Model.add(new TransportArc(degree2Model.getPlaceByName(transArc.source().name()), 
						degree2Model.getTransitionByName(String.format(T_MAX_FORMAT, transitionName, numPresetArcs)), 
						degree2Model.getPlaceByName(transArc.destination().name()), 
						transArc.interval().copy()));
			} else {
				TimedPlace inputPlace = degree2Model.getPlaceByName(transArc.source().name());
				TimedTransition tiIn = degree2Model.getTransitionByName(String.format(T_I_IN_FORMAT, transitionName, numPresetArcs));
				TimedPlace pHolding = degree2Model.getPlaceByName(String.format(HOLDING_PLACE_FORMAT, transitionName, numPresetArcs));
				TimedTransition tiOut = degree2Model.getTransitionByName(String.format(T_I_OUT_FORMAT, transitionName, numPresetArcs));
				TimedPlace outputPlace = degree2Model.getPlaceByName(transArc.destination().name());
				TimeInterval newGuard = transArc.interval().intersect(transArc.destination().invariant());
				
				degree2Model.add(new TransportArc(inputPlace, tiIn, pHolding, newGuard));
				degree2Model.add(new TransportArc(pHolding, tiOut, outputPlace, TimeInterval.ZERO_INF));
			}
			numPresetArcs++;
		}
	}
	
	private void createInhibitorArcs(TimedTransition t) {
		if(t.presetSize() == 0)
			return;
		
		for(TimedInhibitorArc inhibArc : t.getInhibitorArcs()) {
			if(t.presetSize() == 1) {
				degree2Model.add(new TimedInhibitorArc(degree2Model.getPlaceByName(inhibArc.source().name()), 
						degree2Model.getTransitionByName(String.format(T_MAX_FORMAT, t.name(), 1)), 
						inhibArc.interval().copy()));
			} else {
				degree2Model.add(new TimedInhibitorArc(degree2Model.getPlaceByName(inhibArc.source().name()), 
						degree2Model.getTransitionByName(String.format(T_I_IN_FORMAT, t.name(), 1)), 
						inhibArc.interval().copy()));
			}
		}
	}
	
}
