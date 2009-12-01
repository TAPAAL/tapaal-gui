package dk.aau.cs.TAPN;

import java.util.ArrayList;
import java.util.Hashtable;

import dk.aau.cs.TA.Edge;
import dk.aau.cs.TA.Location;
import dk.aau.cs.TA.NTA;
import dk.aau.cs.TA.TimedAutomata;
import dk.aau.cs.petrinet.Arc;
import dk.aau.cs.petrinet.PrioritizedTAPNTransition;
import dk.aau.cs.petrinet.TAPNArc;
import dk.aau.cs.petrinet.TAPNPlace;
import dk.aau.cs.petrinet.TAPNTransition;
import dk.aau.cs.petrinet.TAPNTransportArc;
import dk.aau.cs.petrinet.TimedArcPetriNet;
import dk.aau.cs.petrinet.Token;

public class TAPNToNTAStandardTransformer implements ModelTransformer<TimedArcPetriNet, NTA> {

	private static final String TANAME = "Token";
	private Hashtable<TAPNPlace, Location> placesToLocations = new Hashtable<TAPNPlace, Location>();
	private int extraTokens = 0;
	private int taCount = 0;
	private boolean usesPriorities = false;

	public TAPNToNTAStandardTransformer(int extraNumberOfTokens){
		extraTokens = extraNumberOfTokens;
	}


	public NTA transform(TimedArcPetriNet model) throws Exception {
		try{
			model.convertToConservative();
		}catch(Exception e){
			e.printStackTrace();
		}

		usesPriorities = model.getInhibitorArcs().size() != 0;
		TimedArcPetriNet degree2Model = model.toDegree2();

		for(int i = 0; i < extraTokens; i++){
			Token token = new Token(model.getPlaceByName("P_capacity"));
			degree2Model.addToken(token);
		}

		return transformToNTA(degree2Model);
	}


	private NTA transformToNTA(TimedArcPetriNet model) {
		ArrayList<TimedAutomata> tas = new ArrayList<TimedAutomata>();

		for(Token token : model.getTokens()){
			placesToLocations.clear(); // TODO: Only need to create this once, no need to clear it every time
			TimedAutomata ta = createTimedAutomata(token, model);
			tas.add(ta);
		}

		String systemDeclarations = createSystemDeclaration();
		String globalDeclarations = createGlobalDeclarations(model);

		taCount = 0;
		placesToLocations.clear();
		return new NTA(tas, systemDeclarations, globalDeclarations);
	}


	private String createSystemDeclaration() {
		StringBuilder builder = new StringBuilder("system ");
		for(int i = 0; i < taCount; i++){
			builder.append(TANAME);
			builder.append(i);

			if(i != taCount-1){
				builder.append(",");
			}
		}
		builder.append(";");

		return builder.toString();
	}


	private String createGlobalDeclarations(TimedArcPetriNet model) {
		StringBuilder builder = new StringBuilder();

		for(TAPNTransition t : model.getTransitions()){
			if (t.isUrgent()){
				builder.append("urgent ");
			} 
			
			builder.append("chan ");
			builder.append(t.getName());
			builder.append(";\n");
		}

		if(usesPriorities){ // Make this work generally
			StringBuilder low = new StringBuilder("chan priority ");
			StringBuilder high = new StringBuilder();
			
			boolean highAddComma = false;
			boolean lowAddComma = false;
			for(TAPNTransition t : model.getTransitions()){
				if(((PrioritizedTAPNTransition)t).getPriority() == 2){
					high.append(t.getName());
					if(highAddComma){
						high.append(",");
					}
					highAddComma = true;
				}else{
					low.append(t.getName());
					if(lowAddComma){
						low.append(",");
					}
					lowAddComma = true;
				}
			}
			
			builder.append(low);
			builder.append("<");
			builder.append(high);
			builder.append(";");
		}
		
		return builder.toString();
	}


	private TimedAutomata createTimedAutomata(Token token, TimedArcPetriNet model) {
		TimedAutomata ta = new TimedAutomata();

		createLocations(model, ta);
		createTransitions(model, ta);

		ta.setDeclarations("clock x;");
		ta.setName(TANAME + taCount);
		ta.setInitLocation(placesToLocations.get(token.getPlace()));
		taCount++;
		return ta;
	}


	private void createTransitions(TimedArcPetriNet model, TimedAutomata ta) {
		for(TAPNTransition transition : model.getTransitions()){
			
		}
		
		//		for(TAPNTransition transition : model.getTransitions()){
//			for(Arc presetArc : transition.getPreset()){
//				Arc usedPostSetArc = null;
//
//				for(Arc postsetArc : transition.getPostset()){
//					if(presetArc instanceof TAPNTransportArc){
//						if(postsetArc instanceof TAPNTransportArc && !(usedPostSetArc == postsetArc)){
//							Edge e = createEdge(transition,(TAPNArc)presetArc, postsetArc, '!');
//							ta.addTransition(e);
//							usedPostSetArc = postsetArc;
//							break;
//						}
//					}else{
//						if(postsetArc != usedPostSetArc){
//							Edge e = createEdge(transition,(TAPNArc)presetArc, postsetArc);
//							ta.addTransition(e);
//							usedPostSetArc = postsetArc;
//							break;
//						}
//					}
//				}
//			}	
//		}
	}


	private Edge createEdge(TAPNTransition transition, TAPNArc sourceArc, Arc destinationArc, char symbol) {
		Location source = placesToLocations.get(sourceArc.getSource());
		Location destination = placesToLocations.get(destinationArc.getSource());

		String guard = createTransitionGuard(sourceArc.getGuard());
		String sync = transition.getName() + symbol;
		String update = createUpdateExpression(sourceArc);

		Edge e = new Edge(source, destination, guard, sync, update);
		return e;
	}


	private String createUpdateExpression(TAPNArc sourceArc) {
		if(!(sourceArc instanceof TAPNTransportArc)){
			return "x := 0"; //TODO: lock boolean?
		}else{
			return "";
		}
	}

	private String createTransitionGuard(String guard) {
		String[] splitGuard = guard.substring(1, guard.length()-2).split(",");
		char firstDelim = guard.charAt(0);
		char secondDelim = guard.charAt(guard.length()-1);

		StringBuilder builder = new StringBuilder();
		builder.append("x ");

		if(firstDelim == '('){
			builder.append(">");
		} else {
			builder.append(">=");
		}

		builder.append(splitGuard[0]);

		if(!splitGuard[1].equals("inf")){
			builder.append(" && x ");

			if(secondDelim == ')'){
				builder.append("<");
			}else {
				builder.append("<=");
			}
			builder.append(splitGuard[1]);
		}

		return builder.toString();
	}


	private void createLocations(TimedArcPetriNet model, TimedAutomata ta) {
		for(TAPNPlace p : model.getPlaces()){
			Location l = new Location(p.getName(), convertInvariant(p.getInvariant()));
			l.setUrgent(p.isUrgent());

			ta.addLocation(l);
			placesToLocations.put(p, l);
		}
	}


	private String convertInvariant(String invariant) {
		String inv = "";
		if(!invariant.equals("<inf")){
			inv = invariant.replace("<", "&lt;");
		}

		return inv;
	}

}
