package dk.aau.cs.TAPN;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dk.aau.cs.TA.Edge;
import dk.aau.cs.TA.Location;
import dk.aau.cs.TA.NTA;
import dk.aau.cs.TA.StandardUPPAALQuery;
import dk.aau.cs.TA.TimedAutomata;
import dk.aau.cs.TA.UPPAALQuery;
import dk.aau.cs.petrinet.Arc;
import dk.aau.cs.petrinet.PrioritizedTAPNTransition;
import dk.aau.cs.petrinet.TAPNArc;
import dk.aau.cs.petrinet.TAPNPlace;
import dk.aau.cs.petrinet.TAPNQuery;
import dk.aau.cs.petrinet.TAPNTransition;
import dk.aau.cs.petrinet.TAPNTransportArc;
import dk.aau.cs.petrinet.TimedArcPetriNet;
import dk.aau.cs.petrinet.Token;

public class TAPNToNTAStandardTransformer 
	implements ModelTransformer<TimedArcPetriNet, NTA>,
			   QueryTransformer<TAPNQuery, UPPAALQuery>{

	private static final String TANAME = "Token";
	private Hashtable<TAPNPlace, Location> placesToLocations = new Hashtable<TAPNPlace, Location>();
	private int extraTokens = 0;
	private int taCount = 0;
	private boolean usesPriorities = false;

	public TAPNToNTAStandardTransformer(int extraNumberOfTokens){
		extraTokens = extraNumberOfTokens;
	}


	public NTA transformModel(TimedArcPetriNet model) throws Exception {
		try{
			model.convertToConservative();
		}catch(Exception e){
			e.printStackTrace();
		}

		usesPriorities = model.getInhibitorArcs().size() != 0;
		TimedArcPetriNet degree2Model = model.toDegree2();

		for(int i = 0; i < extraTokens; i++){
			Token token = new Token(degree2Model.getPlaceByName("P_capacity"));
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

			boolean highHasElement = false;
			boolean lowHasElement = false;
			int size = model.getTransitions().size();
			for(int i = 0; i < size; i++){
				PrioritizedTAPNTransition t = (PrioritizedTAPNTransition)model.getTransitions().get(i);
				if(t.getPriority() == 2){
					if(highHasElement){
						high.append(",");
					}
					high.append(t.getName());
					highHasElement = true;
				}else{
					if(lowHasElement){
						low.append(",");
					}
					low.append(t.getName());
					lowHasElement = true;
				}
			}

			builder.append(low);
			builder.append("&lt;");
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
		Location source = placesToLocations.get(sourceArc.getSource());
		Location destination = placesToLocations.get(destinationArc.getTarget());

		String guard = createTransitionGuard(sourceArc.getGuard());
		String sync = createSyncExpression(transition, symbol);
		String update = createUpdateExpression(sourceArc);

		Edge e = new Edge(source, destination, guard, sync, update);
		return e;
	}


	private String createSyncExpression(TAPNTransition transition, char symbol) throws IllegalArgumentException {
		if(transition.getPreset().size() == 1 && transition.getPostset().size() == 1){
			return "";
		}else if (transition.getPreset().size() == 2 && transition.getPostset().size() == 2) {
			return transition.getName() + symbol;
		}else
			throw new IllegalArgumentException("The size of the transition's preset and postset does not match!");
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
