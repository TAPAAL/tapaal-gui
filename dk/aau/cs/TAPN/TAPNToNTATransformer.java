package dk.aau.cs.TAPN;

import java.util.ArrayList;

import dk.aau.cs.TA.Location;
import dk.aau.cs.TA.NTA;
import dk.aau.cs.TA.TimedAutomata;
import dk.aau.cs.petrinet.TAPNPlace;
import dk.aau.cs.petrinet.TAPNTransition;
import dk.aau.cs.petrinet.TimedArcPetriNet;
import dk.aau.cs.petrinet.Token;

public class TAPNToNTATransformer implements ModelTransformer<TimedArcPetriNet, NTA> {

	public TAPNToNTATransformer(){
		
	}
	
	
	public NTA transform(TimedArcPetriNet model) throws Exception {
		if(!model.isDegree2()) throw new IllegalArgumentException("model must be degree 2.");
		
		ArrayList<TimedAutomata> tas = new ArrayList<TimedAutomata>();
		
		for(Token token : model.getTokens()){
			TimedAutomata ta = createTimedAutomata(token, model);
			tas.add(ta);
		}
		
		String systemDeclarations = "";
		String globalDeclarations = "";
		
		return new NTA(tas, systemDeclarations, globalDeclarations);
	}


	private TimedAutomata createTimedAutomata(Token token, TimedArcPetriNet model) {
		TimedAutomata ta = new TimedAutomata();
		
		for(TAPNPlace p : model.getPlaces()){
			Location l = new Location(p.getName(), convertInvariant(p.getInvariant()));
			l.setUrgent(p.isUrgent());
			ta.addLocation(l);
		}
		
		for(TAPNTransition t : model.getTransitions()){
			
		}
		
		return ta;
	}


	private String convertInvariant(String invariant) {
		String inv = "";
		if(!invariant.equals("<inf")){
			inv = invariant.replace("<", "&lt;");
		}
		
		return inv;
	}

}
