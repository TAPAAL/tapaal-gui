package dk.aau.cs.TAPN;

import java.util.Hashtable;
import java.util.List;

import dk.aau.cs.TA.Location;
import dk.aau.cs.TA.NTA;
import dk.aau.cs.TA.TimedAutomata;
import dk.aau.cs.TA.UPPAALQuery;
import dk.aau.cs.petrinet.PrioritizedTAPNTransition;
import dk.aau.cs.petrinet.TAPNArc;
import dk.aau.cs.petrinet.TAPNPlace;
import dk.aau.cs.petrinet.TAPNQuery;
import dk.aau.cs.petrinet.TAPNTransition;
import dk.aau.cs.petrinet.TAPNTransportArc;
import dk.aau.cs.petrinet.TimedArcPetriNet;
import dk.aau.cs.petrinet.Token;

public abstract class TAPNToNTATransformer implements 
ModelTransformer<TimedArcPetriNet, NTA>,
QueryTransformer<TAPNQuery, UPPAALQuery>{

	protected static final String QUERY_PATTERN = "([a-zA-Z][a-zA-Z0-9_]*) (==|<|<=|>=|>) ([0-9])*";
	private int extraTokens;
	private boolean usesPriorities;
	private Hashtable<String, Location> namesToLocations = new Hashtable<String, Location>();

	protected TAPNToNTATransformer(int extraTokens){
		this.extraTokens = extraTokens;
	}

	protected int getExtraTokens() {
		return extraTokens;
	}

	protected boolean isUsingPriorities(){
		return usesPriorities;
	}

	protected Location getLocationByName(String name){
		return namesToLocations.get(name);
	}

	protected void addLocationMapping(String name, Location location){
		namesToLocations.put(name, location);
	}

	protected void clearLocationMappings(){
		namesToLocations.clear();
	}

	public NTA transformModel(TimedArcPetriNet model) throws Exception{
		usesPriorities = model.getInhibitorArcs().size() != 0;
		TimedArcPetriNet degree2Model = model.toDegree2();

		TAPNPlace pcapacity = degree2Model.getPlaceByName("P_capacity");
		for(int i = 0; i < extraTokens; i++){

			Token token = new Token(pcapacity);
			degree2Model.addToken(token);
		}

		return transformToNTA(degree2Model);
	}

	private NTA transformToNTA(TimedArcPetriNet model) {
		List<TimedAutomata> tas = createAutomata(model);
		String system = createSystemDeclaration();
		String decl = createGlobalDeclarations(model);

		return new NTA(tas, system, decl);
	}

	protected String createGlobalDeclarations(TimedArcPetriNet model){
		StringBuilder builder = new StringBuilder();

		for(TAPNTransition t : model.getTransitions()){
			if(!(t.getPreset().size() == 1 && t.getPostset().size() == 1)){
				if (t.isUrgent()){
					builder.append("urgent ");
				} 

				builder.append("chan ");
				builder.append(t.getName());
				builder.append(";\n");
			}
		}

		if(usesPriorities){ // Make this work generally
			StringBuilder low = new StringBuilder("chan priority ");
			StringBuilder high = new StringBuilder();

			boolean highHasElement = false;
			boolean lowHasElement = false;
			int size = model.getTransitions().size();
			for(int i = 0; i < size; i++){
				PrioritizedTAPNTransition t = (PrioritizedTAPNTransition)model.getTransitions().get(i);

				if(!(t.getPreset().size() == 1 && t.getPostset().size() == 1)){
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
			}

			builder.append(low);
			builder.append("&lt;");
			builder.append(high);
			builder.append(";");
		}

		return builder.toString();
	}

	protected abstract List<TimedAutomata> createAutomata(TimedArcPetriNet model);
	protected abstract String createSystemDeclaration();

	protected String convertInvariant(String invariant) {
		String inv = "";
		if(!invariant.equals("<inf")){
			inv = invariant.replace("<", "&lt;");
		}
	
		return inv;
	}

	protected String createTransitionGuard(String guard) {
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

	protected String createSyncExpression(TAPNTransition transition, char symbol)
			throws IllegalArgumentException {
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

	protected String createUpdateExpression(TAPNArc sourceArc) {
		if(!(sourceArc instanceof TAPNTransportArc)){
			return "x := 0"; //TODO: lock boolean?
		}else{
			return "";
		}
	}


}
