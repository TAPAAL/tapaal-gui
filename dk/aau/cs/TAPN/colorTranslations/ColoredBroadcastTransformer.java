package dk.aau.cs.TAPN.colorTranslations;

import dk.aau.cs.TAPN.TAPNToNTABroadcastTransformer;
import dk.aau.cs.petrinet.TAPNArc;
import dk.aau.cs.petrinet.TAPNPlace;
import dk.aau.cs.petrinet.TimedArcPetriNet;
import dk.aau.cs.petrinet.colors.ColorSet;
import dk.aau.cs.petrinet.colors.ColoredInhibitorArc;
import dk.aau.cs.petrinet.colors.ColoredInputArc;
import dk.aau.cs.petrinet.colors.ColoredInterval;
import dk.aau.cs.petrinet.colors.ColoredPlace;
import dk.aau.cs.petrinet.colors.ColoredTimeInvariant;
import dk.aau.cs.petrinet.colors.ColoredTimedArcPetriNet;
import dk.aau.cs.petrinet.colors.ColoredTransportArc;

public class ColoredBroadcastTransformer extends TAPNToNTABroadcastTransformer {
	private static final String VALUE_VAR_NAME = "value";
	
	public ColoredBroadcastTransformer(int extraTokens, boolean useSymmetry) {
		super(extraTokens, useSymmetry);
	}
	
	protected String createLocalDeclarations(TimedArcPetriNet model){
		String decl = super.createLocalDeclarations(model);

		ColoredTimedArcPetriNet ctapn = (ColoredTimedArcPetriNet)model;
		decl += String.format("\nint[%1$d,%2$d] %3$s;", ctapn.getLowerBoundForColor(), ctapn.getUpperBoundForColor(), VALUE_VAR_NAME);
		return decl;
	}
	
	protected String createTransitionGuard(TAPNArc arc, TAPNPlace target,
			boolean isTransportArc) {
		String guard = "";
		
		ColoredInterval timeGuard = null;
		ColoredTimeInvariant targetTimeInvariant = null;
		ColorSet colorGuard = null;
		ColorSet colorInvariant = null;
		if(arc instanceof ColoredTransportArc){
			ColoredTransportArc tarc = (ColoredTransportArc)arc;
			timeGuard = tarc.getTimeGuard();
			targetTimeInvariant = ((ColoredPlace)tarc.getTarget()).getTimeInvariant();
			colorGuard = tarc.getColorGuard();
			colorInvariant = ((ColoredPlace)tarc.getTarget()).getColorInvariant();
		}else if(arc instanceof ColoredInputArc){
			ColoredInputArc cia = (ColoredInputArc)arc;
			timeGuard = cia.getTimeGuard();
			colorGuard = cia.getColorGuard();
			colorInvariant = ((ColoredPlace)arc.getTarget()).getColorInvariant();
		}else if(arc instanceof ColoredInhibitorArc){
			ColoredInhibitorArc cia = (ColoredInhibitorArc)arc;
			timeGuard = cia.getTimeGuard();
			colorGuard = cia.getColorGuard();
		}
		
		guard += timeGuard.convertToTAGuardString(TOKEN_CLOCK_NAME, VALUE_VAR_NAME);
		
		if(targetTimeInvariant != null){
			String targetTimeInvString = targetTimeInvariant.convertToTAInvariantString(TOKEN_CLOCK_NAME, VALUE_VAR_NAME);
			if(!guard.isEmpty() && !targetTimeInvString.isEmpty()){
				guard += " && ";
			}
			
			guard += targetTimeInvString;
		}
		
		// TODO: INTERSECTION OF COLOR GUARD AND COLOR INVARIANT MISSING HERE
		return guard;
	}
}
