package dk.aau.cs.TAPN;

import java.util.List;

import dk.aau.cs.TA.Edge;
import dk.aau.cs.TA.Location;
import dk.aau.cs.TA.TimedAutomaton;
import dk.aau.cs.TAPN.Pairing.ArcType;
import dk.aau.cs.petrinet.TAPNInhibitorArc;
import dk.aau.cs.petrinet.TAPNTransition;

public class AdvancedBroadcastTransformer extends TAPNToNTABroadcastTransformer {

	public AdvancedBroadcastTransformer(int extraTokens, boolean useSymmetry) {
		super(extraTokens, useSymmetry);
		
	}
	
	protected void createStructureForPairing(TimedAutomaton ta, TAPNTransition t,
			List<Pairing> pairing) {
		int i = 0;
		for(Pairing pair : pairing){
			String inputPlaceName = pair.getInput().getName();
			String locationName = String.format(TOKEN_INTERMEDIATE_PLACE, inputPlaceName, t.getName(), i);

			String counter = String.format(COUNTER_NAME, i);
			arcsToCounters.put(pair.getInputArc(), counter);
			
			String inv = counter + "==1";
			
			Location intermediate = new Location(locationName, inv);
			intermediate.setCommitted(true);
			ta.addLocation(intermediate);
			addLocationMapping(locationName, intermediate);

			Edge testEdge = new Edge(getLocationByName(inputPlaceName), 
					intermediate, 
					createTransitionGuard(pair.getInterval(), pair.getOutput(), pair.getArcType() == ArcType.TARC),
					String.format(TEST_CHANNEL_NAME, t.getName(), "?"),
					String.format(COUNTER_UPDATE, counter, "++"));
			ta.addTransition(testEdge);

			Edge fireEdge = new Edge(intermediate,
					getLocationByName(pair.getOutput().getName()),
					"", //String.format(COUNTER_UPDATE, i, "==1"),
					String.format(FIRE_CHANNEL_NAME, t.getName(), "?"),
					createResetExpressionIfNormalArc(pair.getArcType()));
			ta.addTransition(fireEdge);

			Location l = new Location("",counter + "==1");
			l.setCommitted(true);
			ta.addLocation(l);
			
			Edge testEdge2 = new Edge(getLocationByName(inputPlaceName),
					l,
					createTransitionGuard(pair.getInterval(), pair.getOutput(), pair.getArcType() == ArcType.TARC),
					String.format(TEST_CHANNEL_NAME, t.getName(), "?"),
					"");
			ta.addTransition(testEdge2);
			
			Edge fireEdge2 = new Edge(l,
					getLocationByName(inputPlaceName),
					"",
					String.format(FIRE_CHANNEL_NAME, t.getName(), "?"),
					"");
			ta.addTransition(fireEdge2);
			i++;
		}

		createStructureForInhibitorArcs(ta, t, i);
	}

}
