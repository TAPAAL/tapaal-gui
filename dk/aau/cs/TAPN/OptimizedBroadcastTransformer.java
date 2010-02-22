package dk.aau.cs.TAPN;

import java.util.List;

import dk.aau.cs.TA.Edge;
import dk.aau.cs.TA.Location;
import dk.aau.cs.TA.TimedAutomaton;
import dk.aau.cs.TAPN.Pairing.ArcType;
import dk.aau.cs.petrinet.TAPNTransition;
import dk.aau.cs.petrinet.TimedArcPetriNet;

public class OptimizedBroadcastTransformer extends
		TAPNToNTABroadcastTransformer {

	public OptimizedBroadcastTransformer(int extraTokens, boolean useSymmetry) {
		super(extraTokens, useSymmetry);
		
	}
	
	protected void createTransitionSimulations(TimedAutomaton control, Location lock,
			TimedArcPetriNet model) {

		for(TAPNTransition transition : model.getTransitions()){
			String invariant = createInvariantForControl(transition);

			Location tempLoc = new Location("",invariant);
			tempLoc.setCommitted(true);
			control.addLocation(tempLoc);

			Location tempLoc2 = new Location("","");
			tempLoc2.setCommitted(true);
			control.addLocation(tempLoc2);
			
			Edge testEdge = new Edge(lock,
					tempLoc,
					"",
					String.format(TEST_CHANNEL_NAME, transition.getName(), "!"),
					"");
			control.addTransition(testEdge);

			Edge tau = new Edge(tempLoc,
					tempLoc2,
					createGuardForControl(transition, 1),
					"",
					"");
			control.addTransition(tau);
			
			Edge fireEdge = new Edge(tempLoc2,
					lock,
					createGuardForControl(transition, 0),
					"",
					"");
			control.addTransition(fireEdge);
		}
	}
	
	protected void createStructureForPairing(TimedAutomaton ta, TAPNTransition t,
			List<Pairing> pairing) {
		int i = 0;
		for(Pairing pair : pairing){
			String inputPlaceName = pair.getInput().getName();
			String locationName = String.format(TOKEN_INTERMEDIATE_PLACE, inputPlaceName, t.getName(), i);

				
			Location intermediate = new Location(locationName, "");
			intermediate.setCommitted(true);
			ta.addLocation(intermediate);
			addLocationMapping(locationName, intermediate);

			String counter = String.format(COUNTER_NAME, i);
			arcsToCounters.put(pair.getInputArc(), counter);

			Edge testEdge = new Edge(getLocationByName(inputPlaceName), 
					intermediate, 
					createTransitionGuard(pair.getInterval(), pair.getOutput(), pair.getArcType() == ArcType.TARC),
					String.format(TEST_CHANNEL_NAME, t.getName(), "?"),
					String.format(COUNTER_UPDATE, counter, "++"));
			ta.addTransition(testEdge);

			String resetExpr = createResetExpressionIfNormalArc(pair.getArcType());
			if(!resetExpr.isEmpty()){
				resetExpr = ", " + resetExpr;
			}
			Edge fireEdge = new Edge(intermediate,
					getLocationByName(pair.getOutput().getName()),
					String.format(COUNTER_UPDATE, counter, "==1"),
					"",//String.format(FIRE_CHANNEL_NAME, t.getName(), "?"),
					counter + "--" + resetExpr);
			ta.addTransition(fireEdge);

			Edge backEdge = new Edge(intermediate,
					getLocationByName(inputPlaceName),
					String.format(COUNTER_UPDATE, counter,">1"),
					"",
					String.format(COUNTER_UPDATE, counter, "--"));
			ta.addTransition(backEdge);

			i++;
		}

		createStructureForInhibitorArcs(ta, t, i);
	}
	
	
	private String createGuardForControl(TAPNTransition transition, int number){
		return createBooleanExpressionForControl(transition, "==", "==", number);
	}

}
