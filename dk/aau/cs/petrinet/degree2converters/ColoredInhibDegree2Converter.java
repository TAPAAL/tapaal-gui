package dk.aau.cs.petrinet.degree2converters;

import java.util.HashSet;
import java.util.Hashtable;

import dk.aau.cs.petrinet.Arc;
import dk.aau.cs.petrinet.Degree2Converter;
import dk.aau.cs.petrinet.PlaceTransitionObject;
import dk.aau.cs.petrinet.TAPN;
import dk.aau.cs.petrinet.TAPNPlace;
import dk.aau.cs.petrinet.TAPNTransition;
import dk.aau.cs.petrinet.TAPNTransportArc;
import dk.aau.cs.petrinet.Token;
import dk.aau.cs.petrinet.colors.ColorSet;
import dk.aau.cs.petrinet.colors.ColoredInputArc;
import dk.aau.cs.petrinet.colors.ColoredInterval;
import dk.aau.cs.petrinet.colors.ColoredOutputArc;
import dk.aau.cs.petrinet.colors.ColoredPlace;
import dk.aau.cs.petrinet.colors.ColoredTAPN;
import dk.aau.cs.petrinet.colors.ColoredTimeInvariant;
import dk.aau.cs.petrinet.colors.ColoredTimedArcPetriNet;
import dk.aau.cs.petrinet.colors.ColoredToken;
import dk.aau.cs.petrinet.colors.ColoredTransportArc;
import dk.aau.cs.petrinet.colors.CompositeInterval;
import dk.aau.cs.petrinet.colors.Preservation;

public class ColoredInhibDegree2Converter implements Degree2Converter {
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

	private Hashtable<String, PlaceTransitionObject> nameToPTO = new Hashtable<String, PlaceTransitionObject>();

	
	public TAPN transform(TAPN model) throws Exception {
		if(!(model instanceof ColoredTAPN)){
			throw new IllegalArgumentException("Model must be a colored TAPN");
		}
		
		try{
			model.convertToConservative();
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
		
		ColoredTAPN degree2Net = new ColoredTAPN();
		createInitialPlaces(model, degree2Net);
		createTransitions(model, degree2Net);
		createTokens(model, degree2Net);
		
		return degree2Net;
	}

	private void createTokens(TAPN model, ColoredTAPN degree2Net) {
		for(Token token : model.getTokens()){
			ColoredToken ct = (ColoredToken)token;
			ColoredPlace place = (ColoredPlace)getByName(ct.getPlace().getName());
			ColoredToken newToken = new ColoredToken(place, ct.getColor());
			place.addColoredToken(newToken);
		}
		ColoredPlace plock = (ColoredPlace)getByName(PLOCK);
		plock.addColoredToken(new ColoredToken(plock, 0));
	}

	private PlaceTransitionObject getByName(String name) {
		return nameToPTO.get(name);
	}

	private void createTransitions(TAPN model, ColoredTAPN degree2Net) throws Exception {
		for(TAPNTransition transition : model.getTransitions()){
			createSimulationOfTransition(transition, degree2Net);
		}
	}

	private void createSimulationOfTransition(TAPNTransition transition,
			ColoredTAPN degree2Net) throws Exception {
		if(transition.getPreset().size() == 0 && transition.getPostset().size() == 0){
			// Transition only has inhibitor arcs. Ignore it because the answer to the query cannot be affected by this
			return;
		}
		
		if(transition.getPreset().size() == 1){
			createSimulationOfTransitionOfDegree1(transition, degree2Net);
		}else if(transition.isDegree2() && transition.getInhibitorArcs().size() == 0){
			createOptimizedSimulation(transition, degree2Net);
		}else{
			createRingSimulationOfTransition(transition, degree2Net);
		}		
	}

	private void createSimulationOfTransitionOfDegree1(
			TAPNTransition transition, ColoredTAPN degree2Net) throws Exception {
		String trans = String.format(T_MAX_FORMAT, transition.getName(), 1);
		addTransition(degree2Net, trans);
		TAPNTransition newTransition = (TAPNTransition)getByName(trans);
		newTransition.setFromOriginalNet(true);		
		
		Arc presetArc = transition.getPreset().get(0);
		Arc postsetArc = transition.getPostset().get(0);
		if(presetArc instanceof ColoredTransportArc){
			ColoredTransportArc cta = (ColoredTransportArc)presetArc;
			ColoredPlace targetPlace = (ColoredPlace)cta.getTarget();
			addTransportArc(degree2Net,
					presetArc.getSource().getName(),
					trans,
					targetPlace.getName(),
					cta.getTimeGuard(),
					cta.getColorGuard(),
					cta.getPreservation(),
					cta.getOutputValue());	
		}else{
			ColoredInputArc inputArc = (ColoredInputArc)presetArc;
			ColoredOutputArc outputArc = (ColoredOutputArc)postsetArc;
			addColoredInputArc(degree2Net,
					presetArc.getSource().getName(),
					trans,
					new ColoredInterval(inputArc.getTimeGuard()),
					new ColorSet(inputArc.getColorGuard()));
			addColoredOutputArc(degree2Net,
					trans,
					postsetArc.getTarget().getName(),
					outputArc.getOutputValue());
		}
	}

	private void createRingSimulationOfTransition(TAPNTransition transition,
			ColoredTAPN degree2Net) throws Exception {
		createRingStructure(transition, degree2Net);
		createArcs(transition, degree2Net);
	}
	
	

	private void createArcs(TAPNTransition transition, ColoredTAPN degree2Net) throws Exception {
		HashSet<Arc> usedFromPostSet = new HashSet<Arc>();

		for(int i = 1; i <= transition.getPreset().size(); i++){
			Arc presetArc = transition.getPreset().get(i-1);

			if(presetArc instanceof ColoredTransportArc){
				addSimulationOfPresetPostsetPairing(degree2Net, transition, i, (ColoredTransportArc)presetArc);
				usedFromPostSet.add(presetArc);
			}else{
				for(Arc postsetArc : transition.getPostset()){
					if(!usedFromPostSet.contains(postsetArc) && !(postsetArc instanceof ColoredTransportArc)){
						addSimulationOfPresetPostsetPairing(degree2Net, transition, i, (ColoredInputArc)presetArc, (ColoredOutputArc)postsetArc);
						usedFromPostSet.add(postsetArc);
						break;
					}
				}
			}
		}
		
	}

	private void addSimulationOfPresetPostsetPairing(ColoredTAPN degree2Net,
			TAPNTransition transition, int i, ColoredInputArc presetArc,
			ColoredOutputArc postsetArc) throws Exception {
		String transitionName = transition.getName();

		if(i == transition.getPreset().size()){
			addColoredInputArc(degree2Net, 
					presetArc.getSource().getName(),
					String.format(T_MAX_FORMAT, transitionName, i),
					new ColoredInterval(presetArc.getTimeGuard()),
					createColorGuardForInputArc(presetArc, postsetArc));
			addColoredOutputArc(degree2Net, 
					String.format(T_MAX_FORMAT, transitionName, i),
					postsetArc.getTarget().getName(),
					postsetArc.getOutputValue());
		}else{
			String trans = String.format(T_I_IN_FORMAT, transitionName, i);
			String pholding = String.format(HOLDING_PLACE_FORMAT, transitionName, i);

			addColoredInputArc(degree2Net, 
					presetArc.getSource().getName(),
					trans,
					new ColoredInterval(presetArc.getTimeGuard()),
					createColorGuardForInputArc(presetArc, postsetArc));
			addColoredOutputArc(degree2Net,
					trans,
					pholding,
					postsetArc.getOutputValue());

			trans = String.format(T_I_OUT_FORMAT, transitionName, i);
			addColoredInputArc(degree2Net, 
					pholding,
					trans,
					new ColoredInterval(),
					new ColorSet());
			addColoredOutputArc(degree2Net,
					trans,
					postsetArc.getTarget().getName(),
					postsetArc.getOutputValue());
		}
		
	}

	private void addSimulationOfPresetPostsetPairing(ColoredTAPN degree2Net,
			TAPNTransition transition, int i, ColoredTransportArc presetArc) throws Exception {
		String transitionName = transition.getName();
		
		if(i == transition.getPreset().size()){
			addTransportArc(degree2Net, 
					presetArc.getSource().getName(),
					String.format(T_MAX_FORMAT, transitionName, i),
					presetArc.getTarget().getName(),
					createTimeGuardForTransportArc(presetArc),
					createColorGuardForTransportArc(presetArc),
					presetArc.getPreservation(),
					presetArc.getOutputValue());
		}else{
			String trans = String.format(T_I_IN_FORMAT, transitionName, i);
			String pholding = String.format(HOLDING_PLACE_FORMAT, transitionName, i);

			addTransportArc(degree2Net, 
					presetArc.getSource().getName(),
					trans,
					pholding,
					createTimeGuardForTransportArc(presetArc),
					createColorGuardForTransportArc(presetArc),
					presetArc.getPreservation(),
					presetArc.getOutputValue());

			trans = String.format(T_I_OUT_FORMAT, transitionName, i);
			addTransportArc(degree2Net, 
					pholding,
					trans,
					presetArc.getTarget().getName(),
					createTimeGuardForTransportArc(presetArc),
					createColorGuardForTransportArc(presetArc),
					presetArc.getPreservation(),
					presetArc.getOutputValue());
		}
		
	}

	private void createRingStructure(TAPNTransition transition,
			ColoredTAPN degree2Net) throws Exception {
		String transitionName = transition.getName();
		int transitionsToCreate = 2 * transition.getPreset().size() - 1;
		String lastPTO = null;		

		// create t^i_in (with corresponding places)
		for(int i = 1; i <= (transitionsToCreate-1)/2; i++){
			String tiin = String.format(T_I_IN_FORMAT, transitionName, i);
			addTransition(degree2Net, tiin);

			String pt = String.format(P_T_IN_FORMAT,transitionName, i);
			addPlace(degree2Net, pt, ColoredTimeInvariant.getZeroInvariant(), new ColorSet());

			addColoredOutputArc(degree2Net, tiin, pt, 0);
			if(lastPTO != null){
				addColoredInputArc(degree2Net, lastPTO, tiin, new ColoredInterval(), new ColorSet());
			}
			lastPTO = pt;

			String holdingPlace = String.format(HOLDING_PLACE_FORMAT, transitionName,i);
			addPlace(degree2Net, holdingPlace, new ColoredTimeInvariant(), new ColorSet());
		}
		addColoredInputArc(degree2Net, PLOCK, String.format(T_I_IN_FORMAT, transitionName, 1), new ColoredInterval(), new ColorSet());

		// Create t^max(t)
		String tmax = String.format(T_MAX_FORMAT,transitionName, transitionsToCreate/2 + 1);
		addTransition(degree2Net, tmax);

		addColoredInputArc(degree2Net, lastPTO, tmax, new ColoredInterval(), new ColorSet());
		lastPTO = tmax;

		// Create t^i_out (with corresponding places)
		for(int i = (transitionsToCreate-1)/2; i >= 1; i--){
			String tiout = String.format(T_I_OUT_FORMAT, transitionName, i);
			addTransition(degree2Net, tiout);

			String pt = String.format(P_T_OUT_FORMAT, transitionName,i);
			addPlace(degree2Net, pt, ColoredTimeInvariant.getZeroInvariant(), new ColorSet());

			addColoredInputArc(degree2Net, pt, tiout, new ColoredInterval(), new ColorSet());			
			addColoredOutputArc(degree2Net, lastPTO, pt, 0);

			lastPTO = tiout;
		}

		addColoredOutputArc(degree2Net, lastPTO, PLOCK, 0);
	}

	private void createOptimizedSimulation(TAPNTransition transition,
			ColoredTAPN degree2Net) throws Exception {
		addTransition(degree2Net, transition.getName());
		TAPNTransition newTransition = (TAPNTransition)getByName(transition.getName());
		newTransition.setFromOriginalNet(true);		
		
		for(Arc arc : transition.getPreset()){
			if(arc instanceof ColoredTransportArc){
				ColoredTransportArc tpa = (ColoredTransportArc)arc;
				addTransportArc(degree2Net, 
						tpa.getSource().getName(), 
						tpa.getIntermediate().getName(), 
						tpa.getTarget().getName(), 
						new ColoredInterval(tpa.getTimeGuard()),
						new ColorSet(tpa.getColorGuard()),
						tpa.getPreservation(),
						tpa.getOutputValue());
			}else{
				ColoredInputArc tapnArc = (ColoredInputArc)arc;
				addColoredInputArc(degree2Net,
						tapnArc.getSource().getName(),
						tapnArc.getTarget().getName(),
						tapnArc.getTimeGuard(),
						tapnArc.getColorGuard());
			}
		}
		
		for(Arc arc : transition.getPostset()){
			if(!(arc instanceof TAPNTransportArc)){
				addColoredOutputArc(degree2Net, 
						arc.getSource().getName(),
						arc.getTarget().getName(),
						((ColoredOutputArc)arc).getOutputValue());
			}
		}
		
	}


	private void addColoredOutputArc(ColoredTAPN degree2Net, String source,
			String target, int outputValue) throws Exception {
		ColoredOutputArc arc = new ColoredOutputArc((TAPNTransition)getByName(source),
				(ColoredPlace)getByName(target),
				outputValue);
		degree2Net.addArc(arc);		
	}

	private void addColoredInputArc(ColoredTAPN degree2Net, String source,
			String target, ColoredInterval timeGuard, ColorSet colorGuard) throws Exception {
		ColoredInputArc arc = new ColoredInputArc((ColoredPlace)getByName(source),
				(TAPNTransition)getByName(target),
				timeGuard,
				colorGuard);
		degree2Net.addArc(arc);
	}

	private void addTransportArc(ColoredTAPN degree2Net, String source,
			String intermediate, String target, ColoredInterval timeGuard,
			ColorSet colorGuard, Preservation preservation, int outputValue) throws Exception {
		ColoredTransportArc cta = new ColoredTransportArc(
				(ColoredPlace)getByName(source),
				(TAPNTransition)getByName(intermediate),
				(ColoredPlace)getByName(target),
				timeGuard,
				colorGuard,
				preservation,
				outputValue);
		degree2Net.addArc(cta);
		
	}

	private void addTransition(ColoredTAPN degree2Net, String name) {
		TAPNTransition trans = new TAPNTransition(name);
		nameToPTO.put(name, trans);		
		degree2Net.addTransition(trans);
	}

	private void createInitialPlaces(TAPN model, ColoredTAPN degree2Net) {
		for(TAPNPlace place : model.getPlaces()){
			ColoredPlace cp = (ColoredPlace)place;
			ColoredTimeInvariant timeInvariant = new ColoredTimeInvariant(cp.getTimeInvariant());
			ColorSet colorInvariant = new ColorSet(cp.getColorInvariant());
			addPlace(degree2Net, place.getName(), timeInvariant, colorInvariant);
		}
		
		addPlace(degree2Net, PLOCK, new ColoredTimeInvariant(), new ColorSet());
	}

	private void addPlace(ColoredTimedArcPetriNet degree2Net, String name,
			ColoredTimeInvariant timeInvariant, ColorSet colorInvariant) {
		ColoredPlace place = new ColoredPlace(name, timeInvariant, colorInvariant);
		degree2Net.addPlace(place);
		nameToPTO.put(name, place);
	}
	
	private ColorSet createColorGuardForInputArc(ColoredInputArc inputArc, ColoredOutputArc outputArc){
		ColoredPlace target = (ColoredPlace)outputArc.getTarget();
		
		if(!target.getColorInvariant().contains(outputArc.getOutputValue())){
			return new ColorSet(true);
		}else{
			return new ColorSet(inputArc.getColorGuard());
		}
	}
	
	private ColoredInterval createTimeGuardForTransportArc(ColoredTransportArc tarc){
		ColoredPlace target = (ColoredPlace)tarc.getTarget();
		
		if(tarc.getPreservation().equals(Preservation.AgeAndValue) || tarc.getPreservation().equals(Preservation.Age)){
			return new CompositeInterval(tarc.getTimeGuard(), target.getTimeInvariant());
		}else{
			return new ColoredInterval(tarc.getTimeGuard());
		}
	}
	
	private ColorSet createColorGuardForTransportArc(ColoredTransportArc tarc){
		ColoredPlace target = (ColoredPlace)tarc.getTarget();
		
		if(tarc.getPreservation().equals(Preservation.AgeAndValue) || tarc.getPreservation().equals(Preservation.Value)){
			return tarc.getColorGuard().intersect(target.getColorInvariant());
		}else{
			if(!target.getColorInvariant().contains(tarc.getOutputValue())){
				return new ColorSet(true);
			}
			return new ColorSet(tarc.getColorGuard());
		}
	}
}
