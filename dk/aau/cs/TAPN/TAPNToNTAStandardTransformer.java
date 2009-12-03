package dk.aau.cs.TAPN;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dk.aau.cs.TA.Edge;
import dk.aau.cs.TA.Location;
import dk.aau.cs.TA.StandardUPPAALQuery;
import dk.aau.cs.TA.TimedAutomata;
import dk.aau.cs.TA.UPPAALQuery;
import dk.aau.cs.petrinet.Arc;
import dk.aau.cs.petrinet.TAPNArc;
import dk.aau.cs.petrinet.TAPNPlace;
import dk.aau.cs.petrinet.TAPNQuery;
import dk.aau.cs.petrinet.TAPNTransition;
import dk.aau.cs.petrinet.TAPNTransportArc;
import dk.aau.cs.petrinet.TimedArcPetriNet;
import dk.aau.cs.petrinet.Token;

public class TAPNToNTAStandardTransformer 
extends TAPNToNTATransformer{

	private static final String TANAME = "Token";
	private int taCount = 0;

	public TAPNToNTAStandardTransformer(int extraNumberOfTokens){
		super(extraNumberOfTokens);
	}

	@Override
	protected ArrayList<TimedAutomata> createAutomata(TimedArcPetriNet model) {
		ArrayList<TimedAutomata> tas = new ArrayList<TimedAutomata>();

		List<Token> tokens = model.getTokens();
		for(Token token : tokens){
			clearLocationMappings();
			
			TimedAutomata ta = createTimedAutomata(model);
			ta.setInitLocation(getLocationByName(token.getPlace().getName()));

			tas.add(ta);
		}
		return tas;
	}


	@Override
	protected String createSystemDeclaration() {
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

	private TimedAutomata createTimedAutomata(TimedArcPetriNet model) {
		TimedAutomata ta = new TimedAutomata();

		createLocations(model, ta);
		createTransitions(model, ta);
		ta.setDeclarations("clock x;");
		ta.setName(TANAME + taCount);
		taCount++;
		
		return ta;
	}


	private void createTransitions(TimedArcPetriNet model, TimedAutomata ta) {
		for(TAPNTransition transition : model.getTransitions()){
			char symbol = '!';
			Arc usedPostSetArc = null;

			for(Arc presetArc : transition.getPreset()){ // at most two
				for(Arc postsetArc : transition.getPostset()){
					if(presetArc instanceof TAPNTransportArc){
						if(postsetArc instanceof TAPNTransportArc && postsetArc != usedPostSetArc){
							Edge e = createEdge(transition, (TAPNArc)presetArc, postsetArc, symbol);
							ta.addTransition(e);
							usedPostSetArc = postsetArc;
							symbol = '?';
							break;
						}
					}else{
						if(!(postsetArc instanceof TAPNTransportArc) && postsetArc != usedPostSetArc){
							Edge e = createEdge(transition, (TAPNArc)presetArc, postsetArc, symbol);
							ta.addTransition(e);
							usedPostSetArc = postsetArc;
							symbol = '?'; // Makes next edge a ? edge
							break;
						}
					}
				}
			}
		}
	}


	private Edge createEdge(TAPNTransition transition, TAPNArc sourceArc, Arc destinationArc, char symbol) {
		Location source = getLocationByName(sourceArc.getSource().getName());
		Location destination = getLocationByName(destinationArc.getTarget().getName());

		String guard = createTransitionGuard(sourceArc.getGuard());
		String sync = createSyncExpression(transition, symbol);
		String update = createUpdateExpression(sourceArc);

		Edge e = new Edge(source, destination, guard, sync, update);
		return e;
	}
	

	private String createSyncExpression(TAPNTransition transition, char symbol) throws IllegalArgumentException {
		if(isDegree1(transition)){
			return "";
		}else if (isDegree2(transition)) {
			return transition.getName() + symbol;
		}else
			throw new IllegalArgumentException("The size of the transition's preset and postset does not match!");
	}


	private boolean isDegree2(TAPNTransition transition) {
		return transition.getPreset().size() == 2 && transition.getPostset().size() == 2;
	}


	private boolean isDegree1(TAPNTransition transition) {
		return transition.getPreset().size() == 1 && transition.getPostset().size() == 1;
	}


	private String createUpdateExpression(TAPNArc sourceArc) {
		if(!(sourceArc instanceof TAPNTransportArc)){
			return "x := 0"; //TODO: lock boolean?
		}else{
			return "";
		}
	}

	private String createTransitionGuard(String guard) {
		if(guard.equals("[0,inf)")) return "";

		String[] splitGuard = guard.substring(1, guard.length()-1).split(",");
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
			addLocationMapping(p.getName(), l);
		}
	}


	private String convertInvariant(String invariant) {
		String inv = "";
		if(!invariant.equals("<inf")){
			inv = invariant.replace("<", "&lt;");
		}

		return inv;
	}


	@Override
	public UPPAALQuery transformQuery(TAPNQuery tapnQuery) throws Exception {
		String query = tapnQuery.toString();

		Pattern pattern = Pattern.compile("([a-zA-Z][a-zA-Z0-9_]*) (==|<|<=|>=|>) ([0-9])*");
		Matcher matcher = pattern.matcher(query);

		StringBuilder builder = new StringBuilder("(");
		for(int i = 0; i < tapnQuery.getTotalTokens(); i++){
			if(i > 0){
				builder.append(" + ");
			}
			builder.append(TANAME);
			builder.append(i);
			builder.append(".$1");
		}
		builder.append(") $2 $3");
		return new StandardUPPAALQuery(matcher.replaceAll(String.format(builder.toString())));
	}
}
