package dk.aau.cs.translations.coloredtapn;

import dk.aau.cs.petrinet.Arc;
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
import dk.aau.cs.petrinet.colors.ColoredTimeInvariant;
import dk.aau.cs.petrinet.colors.ColoredTimedArcPetriNet;
import dk.aau.cs.petrinet.colors.ColoredToken;
import dk.aau.cs.petrinet.colors.ColoredTransportArc;
import dk.aau.cs.petrinet.colors.Preservation;
import dk.aau.cs.translations.ColoredTranslationNamingScheme;
import dk.aau.cs.translations.tapn.TAPNToNTABroadcastTransformer;

public class ColoredBroadcastTransformer extends TAPNToNTABroadcastTransformer {
	private static final String VALUE_VAR_NAME = "value";

	public ColoredBroadcastTransformer(int extraTokens, boolean useSymmetry) {
		super(extraTokens, useSymmetry);
	}

	@Override
	protected String createLocalDeclarations(TimedArcPetriNet model){
		String decl = super.createLocalDeclarations(model);

		ColoredTimedArcPetriNet ctapn = (ColoredTimedArcPetriNet)model;
		decl += String.format("\nint[%1$d,%2$d] %3$s;", ctapn.getLowerBoundForColor(), ctapn.getUpperBoundForColor(), VALUE_VAR_NAME);
		return decl;
	}

	@Override
	protected String createTransitionGuard(TAPNArc inputArc, Arc outputArc, TAPNPlace target,
			boolean isTransportArc) {

		if(inputArc instanceof ColoredTransportArc){
			return createTransitionGuardForTransportArc((ColoredTransportArc)inputArc);
		}else if(inputArc instanceof ColoredInputArc){
			return createTransitionGuardForInputArc((ColoredInputArc)inputArc, (ColoredOutputArc)outputArc);
		}else if(inputArc instanceof ColoredInhibitorArc){
			return createTransitionGuardForInhibitorArc((ColoredInhibitorArc)inputArc);
		}else{
			throw new IllegalArgumentException("unknown arc type");
		}
	}

	private String createTransitionGuardForInputArc(ColoredInputArc inputArc,
			ColoredOutputArc outputArc) {
		StringBuilder builder = new StringBuilder();

		ColorSet colorInvariant = ((ColoredPlace)outputArc.getTarget()).getColorInvariant();
		if(!colorInvariant.contains(outputArc.getOutputValue())){
			builder.append("false");
		}else{
			ColoredInterval timeGuard = inputArc.getTimeGuard();
			builder.append(timeGuard.convertToTAGuardString(TOKEN_CLOCK_NAME, VALUE_VAR_NAME));

			ColorSet colorGuard = inputArc.getColorGuard();
			String colorGuardString = colorGuard.convertToTAGuardString(VALUE_VAR_NAME);
			if(builder.length() > 0 && !colorGuardString.isEmpty()){
				builder.append(" && ");
			}

			builder.append(colorGuardString);
		}

		return builder.toString();
	}

	private String createTransitionGuardForInhibitorArc(
			ColoredInhibitorArc inputArc) {
		StringBuilder builder = new StringBuilder();

		ColoredInterval timeGuard = inputArc.getTimeGuard();
		builder.append(timeGuard.convertToTAGuardString(TOKEN_CLOCK_NAME, VALUE_VAR_NAME));

		ColorSet colorGuard = inputArc.getColorGuard();
		String colorGuardString = colorGuard.convertToTAGuardString(VALUE_VAR_NAME);
		if(builder.length() > 0 && !colorGuardString.isEmpty()){
			builder.append(" && ");
		}

		builder.append(colorGuardString);


		return builder.toString();
	}

	private String createTransitionGuardForTransportArc(
			ColoredTransportArc inputArc) {
		StringBuilder guard = new StringBuilder();

		ColoredPlace target = (ColoredPlace)inputArc.getTarget();
		ColorSet colorInvariant = target.getColorInvariant();
		if(inputArc.getPreservation().equals(Preservation.Age) && !colorInvariant.contains(inputArc.getOutputValue())){
			guard.append("false");
		}else{
			ColoredInterval timeGuard = inputArc.getTimeGuard();
			ColoredTimeInvariant targetTimeInvariant = target.getTimeInvariant();

			guard.append(timeGuard.convertToTAGuardString(TOKEN_CLOCK_NAME, VALUE_VAR_NAME));

			if(!inputArc.getPreservation().equals(Preservation.Value)){
				String invString = targetTimeInvariant.convertToTAInvariantString(TOKEN_CLOCK_NAME, VALUE_VAR_NAME);
				if(guard.length() > 0 && !invString.isEmpty()){
					guard.append(" && ");
				}		
				guard.append(invString);
			}

			ColorSet colorGuard = inputArc.getColorGuard();

			ColorSet combined = null;

			if(inputArc.getPreservation().equals(Preservation.AgeAndValue) || inputArc.getPreservation().equals(Preservation.Value)){
				combined = colorGuard.intersect(colorInvariant);
			}else{
				combined = colorGuard;
			}

			String colorGuardString = combined.convertToTAGuardString(VALUE_VAR_NAME);
			if(guard.length() > 0 && !colorGuardString.isEmpty()){
				guard.append(" && ");
			}

			guard.append(colorGuardString);
		}
		return guard.toString();
	}


	@Override
	protected String createResetExpressionIfNormalArc(Arc arc) {
		String clockReset = String.format("%1$s := 0", TOKEN_CLOCK_NAME);

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
	protected String convertInvariant(TAPNPlace place) {
		ColoredPlace cp = (ColoredPlace)place;

		String timeInvariant = cp.getTimeInvariant().convertToTAInvariantString(TOKEN_CLOCK_NAME, VALUE_VAR_NAME);
		String colorInvariant = cp.getColorInvariant().convertToTAGuardString(VALUE_VAR_NAME);

		String invariant = timeInvariant;
		if(!invariant.isEmpty() && !colorInvariant.isEmpty()){
			invariant += " && ";
		}
		invariant += colorInvariant;

		return invariant;
	}


	@Override
	protected String createUpdateExpressionForTokenInitialization(Token token) {
		ColoredToken ct = (ColoredToken)token;

		return String.format("%1$s := %2$d", VALUE_VAR_NAME, ct.getColor());
	}
	
	@Override
	public ColoredTranslationNamingScheme namingScheme() {
		return new ColoredBroadcastNamingScheme();
	}
	
	private class ColoredBroadcastNamingScheme extends BroadcastNamingScheme
		implements ColoredTranslationNamingScheme
	{
		public String colorVariableName() {
			return VALUE_VAR_NAME;
		}
	}
}
