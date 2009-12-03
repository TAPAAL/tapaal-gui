package dk.aau.cs.TAPN;

import java.util.ArrayList;
import java.util.Hashtable;

import dk.aau.cs.TA.Location;
import dk.aau.cs.TA.NTA;
import dk.aau.cs.TA.TimedAutomata;
import dk.aau.cs.TA.UPPAALQuery;
import dk.aau.cs.petrinet.PrioritizedTAPNTransition;
import dk.aau.cs.petrinet.TAPNPlace;
import dk.aau.cs.petrinet.TAPNQuery;
import dk.aau.cs.petrinet.TAPNTransition;
import dk.aau.cs.petrinet.TimedArcPetriNet;
import dk.aau.cs.petrinet.Token;

public abstract class TAPNToNTATransformer implements 
ModelTransformer<TimedArcPetriNet, NTA>,
QueryTransformer<TAPNQuery, UPPAALQuery>{

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
		try{
			model.convertToConservative();
		}catch(Exception e){
			e.printStackTrace();
		}
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
		ArrayList<TimedAutomata> tas = createAutomata(model);
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

	protected abstract ArrayList<TimedAutomata> createAutomata(TimedArcPetriNet model);
	protected abstract String createSystemDeclaration();


}
