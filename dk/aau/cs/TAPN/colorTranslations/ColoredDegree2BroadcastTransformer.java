package dk.aau.cs.TAPN.colorTranslations;

import dk.aau.cs.TAPN.Degree2BroadcastTransformer;
import dk.aau.cs.petrinet.Arc;
import dk.aau.cs.petrinet.Degree2Converter;
import dk.aau.cs.petrinet.TAPNArc;
import dk.aau.cs.petrinet.TAPNPlace;
import dk.aau.cs.petrinet.TimedArcPetriNet;
import dk.aau.cs.petrinet.Token;
import dk.aau.cs.petrinet.colors.ColorSet;
import dk.aau.cs.petrinet.colors.ColoredInhibitorArc;
import dk.aau.cs.petrinet.colors.ColoredInputArc;
import dk.aau.cs.petrinet.colors.ColoredInterval;
import dk.aau.cs.petrinet.colors.ColoredOutputArc;
import dk.aau.cs.petrinet.colors.ColoredPlace;
import dk.aau.cs.petrinet.colors.ColoredTimedArcPetriNet;
import dk.aau.cs.petrinet.colors.ColoredToken;
import dk.aau.cs.petrinet.colors.ColoredTransportArc;
import dk.aau.cs.petrinet.colors.Preservation;
import dk.aau.cs.petrinet.degree2converters.ColoredInhibDegree2Converter;

public class ColoredDegree2BroadcastTransformer extends
Degree2BroadcastTransformer {
	private static final String VALUE_VAR_NAME = "value";

	public ColoredDegree2BroadcastTransformer(int extraTokens,
			boolean useSymmetry) {
		super(extraTokens, useSymmetry);
	}


	protected Degree2Converter getDegree2Converter() {
		return new ColoredInhibDegree2Converter();
	}

	protected String createLocalDeclarations(TimedArcPetriNet model) {
		String decl = super.createLocalDeclarations(model);

		ColoredTimedArcPetriNet ctapn = (ColoredTimedArcPetriNet)model;
		decl += String.format("\nint[%1$d,%2$d] %3$s;", ctapn.getLowerBoundForColor(), ctapn.getUpperBoundForColor(), VALUE_VAR_NAME);
		return decl;
	}

	protected String CreateResetExpressionIfNormalArc(Arc arc) {
		String clockReset = String.format("%1$s := 0", CLOCK_NAME);

		if(arc instanceof ColoredOutputArc){
			int value = ((ColoredOutputArc)arc).getOutputValue();
			String valueReset = String.format("%1$s := %2$d", VALUE_VAR_NAME, value);
			return clockReset + ", " + valueReset;
		}else {
			ColoredTransportArc tarc = (ColoredTransportArc)arc;
			if(tarc.getPreservation().equals(Preservation.Age)){
				int value = tarc.getOutputValue();
				String valueReset = String.format("%1$s := %2$d", VALUE_VAR_NAME, value);
				return valueReset;
			}else if(tarc.getPreservation().equals(Preservation.Value)){
				return clockReset;
			}else{
				return "";
			}
		}
	}

	@Override
	protected String createTransitionGuard(TAPNArc arc, TAPNPlace target,
			boolean isTransportArc) {
		ColoredInterval timeGuard = null;
		ColorSet colorGuard = null;
		if(arc instanceof ColoredTransportArc){
			ColoredTransportArc cta = (ColoredTransportArc)arc;
			timeGuard = cta.getTimeGuard();
			colorGuard = cta.getColorGuard();
		}else if(arc instanceof ColoredInputArc){
			ColoredInputArc cia = (ColoredInputArc)arc;
			timeGuard = cia.getTimeGuard();
			colorGuard = cia.getColorGuard();		
		}else if(arc instanceof ColoredInhibitorArc){
			ColoredInhibitorArc cia = (ColoredInhibitorArc)arc;
			timeGuard = cia.getTimeGuard();
			colorGuard = cia.getColorGuard();	
		}else{
			throw new IllegalArgumentException("unknown arc type");
		}
		
		String guard = timeGuard.convertToTAGuardString(CLOCK_NAME, VALUE_VAR_NAME);
		String colorGuardString = colorGuard.convertToTAGuardString(VALUE_VAR_NAME);
		if(!guard.isEmpty() && !colorGuardString.isEmpty()){
			guard += " && ";
		}
		
		guard += colorGuardString;
		
		return guard;
	}
	
	protected String createUpdateExpressionForTokenInitialization(Token token) {
		ColoredToken ct = (ColoredToken)token;
		
		return String.format("%1$s := %2$d", VALUE_VAR_NAME, ct.getColor());
	}
	
	
	protected String convertInvariant(TAPNPlace place) {
		ColoredPlace cp = (ColoredPlace)place;
		String invariant = cp.getTimeInvariant().convertToTAInvariantString(CLOCK_NAME, VALUE_VAR_NAME);
		String colorInvariant = cp.getColorInvariant().convertToTAGuardString(VALUE_VAR_NAME);
		
		if(!invariant.isEmpty() && !colorInvariant.isEmpty()){
			invariant += " && ";
		}
		
		invariant += colorInvariant;
		
		return invariant;
	}
	
	
}
