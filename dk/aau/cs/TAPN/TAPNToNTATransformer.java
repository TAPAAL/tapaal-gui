package dk.aau.cs.TAPN;

import java.util.ArrayList;
import java.util.Hashtable;

import dk.aau.cs.TA.Edge;
import dk.aau.cs.TA.Location;
import dk.aau.cs.TA.NTA;
import dk.aau.cs.TA.TimedAutomata;
import dk.aau.cs.petrinet.TAPNArc;
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
		Hashtable<TAPNPlace, Location> placesToLocations = new Hashtable<TAPNPlace, Location>();
		
		for(TAPNPlace p : model.getPlaces()){
			Location l = new Location(p.getName(), convertInvariant(p.getInvariant()));
			l.setUrgent(p.isUrgent());
			
			ta.addLocation(l);
			placesToLocations.put(p, l);
		}
		
		for(TAPNArc arc : model.getArcs()){
			Location source = placesToLocations.get(arc.getSource());
			Location destination = placesToLocations.get(arc.getTarget());
			
			String guard = "";
			String sync = "";
			String update = "";
			
			Edge e = new Edge(source, destination, guard, sync, update);
			ta.addTransition(e);
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
