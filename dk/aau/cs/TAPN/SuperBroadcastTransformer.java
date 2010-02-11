package dk.aau.cs.TAPN;

import java.util.List;

import dk.aau.cs.TA.Edge;
import dk.aau.cs.TA.Location;
import dk.aau.cs.TA.TimedAutomaton;
import dk.aau.cs.TAPN.Pairing.ArcType;
import dk.aau.cs.petrinet.TAPNTransition;
import dk.aau.cs.petrinet.TimedArcPetriNet;

public class SuperBroadcastTransformer extends TAPNToNTABroadcastTransformer {

	public SuperBroadcastTransformer(int extraTokens, boolean useSymmetry) {
		super(extraTokens, useSymmetry);
		// TODO Auto-generated constructor stub
	}

	protected void createTransitionSimulations(TimedAutomaton control, Location lock,
			TimedArcPetriNet model) {

		for(TAPNTransition transition : model.getTransitions()){
			String invariant = createInvariantForControl(transition);

			Location tempLoc = new Location("",invariant);
			tempLoc.setCommitted(true);
			control.addLocation(tempLoc);
			
			Edge testEdge = new Edge(lock,
					tempLoc,
					"",
					String.format(TEST_CHANNEL_NAME, transition.getName(), "!"),
					"");
			control.addTransition(testEdge);

			Edge tau = new Edge(tempLoc,
					lock,
					"",
					"",
					createResetExpressionForControl(transition));
			control.addTransition(tau);
		}
	}
	
	protected void createStructureForPairing(TimedAutomaton ta, TAPNTransition t,
			List<Pairing> pairing) {
		int i = 0;
		for(Pairing pair : pairing){
			String inputPlaceName = pair.getInput().getName();
			
			String counter = String.format(COUNTER_NAME, i);
			arcsToCounters.put(pair.getInputArc(), counter);

			String resetExpr = String.format(COUNTER_UPDATE, counter, "++");
			String expr = createResetExpressionIfNormalArc(pair.getArcType());
			if(!expr.isEmpty()){
				resetExpr = resetExpr + ", " + expr;
			}
			
			Edge outputEdge = new Edge(getLocationByName(inputPlaceName), 
					getLocationByName(pair.getOutput().getName()), 
					createTransitionGuard(pair.getInterval()),
					String.format(TEST_CHANNEL_NAME, t.getName(), "?"),
					resetExpr);
			ta.addTransition(outputEdge);

			Edge selfloop = new Edge(getLocationByName(inputPlaceName),
					getLocationByName(inputPlaceName),
					"",
					String.format(TEST_CHANNEL_NAME, t.getName(), "?"),
					"");
			ta.addTransition(selfloop);

			i++;
		}

		createStructureForInhibitorArcs(ta, t, i);
	}
	
	protected String createInvariantForControl(TAPNTransition transition) {
		return createBooleanExpressionForControl(transition, "==", "==",1);
	}
}
