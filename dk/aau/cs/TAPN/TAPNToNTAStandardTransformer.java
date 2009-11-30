package dk.aau.cs.TAPN;

import java.util.ArrayList;
import java.util.Hashtable;

import dk.aau.cs.TA.Edge;
import dk.aau.cs.TA.Location;
import dk.aau.cs.TA.NTA;
import dk.aau.cs.TA.TimedAutomata;
import dk.aau.cs.petrinet.Arc;
import dk.aau.cs.petrinet.TAPNArc;
import dk.aau.cs.petrinet.TAPNPlace;
import dk.aau.cs.petrinet.TAPNTransition;
import dk.aau.cs.petrinet.TAPNTransportArc;
import dk.aau.cs.petrinet.TimedArcPetriNet;
import dk.aau.cs.petrinet.Token;

public class TAPNToNTAStandardTransformer implements ModelTransformer<TimedArcPetriNet, NTA> {

	private Hashtable<TAPNPlace, Location> placesToLocations = new Hashtable<TAPNPlace, Location>();
	public TAPNToNTAStandardTransformer(){

	}


	public NTA transform(TimedArcPetriNet model) throws Exception {
		if(!model.isDegree2()) throw new IllegalArgumentException("model must be degree 2.");

		ArrayList<TimedAutomata> tas = new ArrayList<TimedAutomata>();

		for(Token token : model.getTokens()){
			placesToLocations.clear(); // TODO: Only need to create this once, no need to clear it every time
			TimedAutomata ta = createTimedAutomata(token, model);
			tas.add(ta);
		}

		String systemDeclarations = "";
		String globalDeclarations = "";

		return new NTA(tas, systemDeclarations, globalDeclarations);
	}


	private TimedAutomata createTimedAutomata(Token token, TimedArcPetriNet model) {
		TimedAutomata ta = new TimedAutomata();

		createLocations(model, ta);
		createTransitions(model, ta);
		createPriorities(model, ta);

		return ta;
	}


	private void createPriorities(TimedArcPetriNet model, TimedAutomata ta) {
		// TODO Auto-generated method stub

	}


	private void createTransitions(TimedArcPetriNet model, TimedAutomata ta) {
		for(TAPNTransition transition : model.getTransitions()){
			for(Arc presetArc : transition.getPreset()){
				Arc usedPostSetArc = null;

				for(Arc postsetArc : transition.getPostset()){
					if(presetArc instanceof TAPNTransportArc){
						if(postsetArc instanceof TAPNTransportArc && !(usedPostSetArc == postsetArc)){
							Edge e = createEdge(transition,(TAPNArc)presetArc, postsetArc);
							ta.addTransition(e);
							usedPostSetArc = postsetArc;
							break;
						}
					}else{
						if(postsetArc != usedPostSetArc){
							Edge e = createEdge(transition,(TAPNArc)presetArc, postsetArc);
							ta.addTransition(e);
							usedPostSetArc = postsetArc;
							break;
						}
					}
				}
			}	
		}
	}


	private Edge createEdge(TAPNTransition transition, TAPNArc sourceArc, Arc destinationArc) {
		Location source = placesToLocations.get(sourceArc.getSource());
		Location destination = placesToLocations.get(destinationArc.getSource());

		String guard = createTransitionGuard(sourceArc.getGuard());
		String sync = createSyncExpression(transition);
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

	private String createSyncExpression(TAPNTransition transition) {
		return transition.getName() + "?";
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
