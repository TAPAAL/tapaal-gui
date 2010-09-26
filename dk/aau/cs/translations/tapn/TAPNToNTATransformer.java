package dk.aau.cs.translations.tapn;

import java.util.Hashtable;
import java.util.List;

import dk.aau.cs.TA.Location;
import dk.aau.cs.TA.NTA;
import dk.aau.cs.TA.TimedAutomaton;
import dk.aau.cs.TA.UPPAALQuery;
import dk.aau.cs.petrinet.Degree2Converter;
import dk.aau.cs.petrinet.PrioritizedTAPNTransition;
import dk.aau.cs.petrinet.TAPN;
import dk.aau.cs.petrinet.TAPNArc;
import dk.aau.cs.petrinet.TAPNPlace;
import dk.aau.cs.petrinet.TAPNQuery;
import dk.aau.cs.petrinet.TAPNTransition;
import dk.aau.cs.petrinet.TAPNTransportArc;
import dk.aau.cs.petrinet.TimedArcPetriNet;
import dk.aau.cs.petrinet.Token;
import dk.aau.cs.petrinet.degree2converters.InhibitorToPrioritiesDegree2Converter;
import dk.aau.cs.translations.ModelTransformer;
import dk.aau.cs.translations.QueryTransformer;

public abstract class TAPNToNTATransformer implements 
ModelTransformer<TimedArcPetriNet, NTA>,
QueryTransformer<TAPNQuery, UPPAALQuery>{

	protected static final String QUERY_PATTERN = "([a-zA-Z][a-zA-Z0-9_]*) (==|<|<=|>=|>) ([0-9])*";
	protected static final String CLOCK_NAME = "x";
	
	private int extraTokens;
	private boolean usesPriorities;
	
	
	private Hashtable<String, Location> namesToLocations = new Hashtable<String, Location>();
	private Degree2Converter degree2Converter = new InhibitorToPrioritiesDegree2Converter();
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
		TimedArcPetriNet degree2Model = getDegree2Converter().transform((TAPN)model); // fix this cast

		TAPNPlace pcapacity = degree2Model.getPlaceByName("P_capacity");
		for(int i = 0; i < extraTokens; i++){

			Token token = new Token(pcapacity);
			degree2Model.addToken(token);
		}

		return transformToNTA(degree2Model);
	}

	private NTA transformToNTA(TimedArcPetriNet model) {
		List<TimedAutomaton> tas = createAutomata(model);
		String system = createSystemDeclaration(tas);
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

		if(usesPriorities && model.getTransitions().size() > 0){ // Make this work generally
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
			builder.append(";\n");
		}
		
		builder.append("bool lock = 0;");

		return builder.toString();
	}

	protected abstract List<TimedAutomaton> createAutomata(TimedArcPetriNet model);
	
	
	protected String createSystemDeclaration(List<TimedAutomaton> tas) {
		StringBuilder builder = new StringBuilder("system ");
		builder.append(tas.get(0).getName());
		
		for(int i = 1; i < tas.size(); i++){
			builder.append(",");
			builder.append(tas.get(i).getName());
		}
		
		builder.append(";");
		return builder.toString();
	}

	protected String convertInvariant(String invariant) {
		String inv = "";
		if(!invariant.equals("<inf")){
			inv = CLOCK_NAME + " " + invariant;
		}

		return inv;
	}

	protected String createTransitionGuard(String guard) {
		if(guard.equals("[0,inf)")) return "";
	
		String[] splitGuard = guard.substring(1, guard.length()-1).split(",");
		char firstDelim = guard.charAt(0);
		char secondDelim = guard.charAt(guard.length()-1);
	
		StringBuilder builder = new StringBuilder();
		builder.append(CLOCK_NAME);
		builder.append(" ");
	
		if(firstDelim == '('){
			builder.append(">");
		} else {
			builder.append(">=");
		}
	
		builder.append(splitGuard[0]);
	
		if(!splitGuard[1].equals("inf")){
			builder.append(" && ");
			builder.append(CLOCK_NAME);
			builder.append(" ");
	
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
			return CLOCK_NAME + " := 0"; //TODO: lock boolean?
		}else{
			return "";
		}
	}

	protected String createTransitionGuard(String guard, boolean fromOriginalNet) {
		String taGuard = createTransitionGuard(guard);
	
		if(fromOriginalNet){
			if(taGuard.length() != 0)
				taGuard = "lock == 0 && " + taGuard;
			else
				taGuard = "lock == 0";
		}
	
		return taGuard;
	}

	public void setDegree2Converter(Degree2Converter degree2Converter) {
		this.degree2Converter = degree2Converter;
	}

	public Degree2Converter getDegree2Converter() {
		return degree2Converter;
	}


}
