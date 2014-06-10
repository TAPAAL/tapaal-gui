package dk.aau.cs.verification.UPPAAL;


import javax.swing.ImageIcon;

import dk.aau.cs.model.tapn.simulation.TAPNNetworkTrace;
import dk.aau.cs.verification.IconSelector;
import dk.aau.cs.verification.QueryResult;
import dk.aau.cs.verification.VerificationResult;


public class UppaalIconSelector extends IconSelector {	
	@Override
	public ImageIcon getIconFor(VerificationResult<?> result){
		if (result.getQueryResult().isApproximationInconclusive()) return rerunIcon;
		if(result.isOverApproximationResult())	return result.isQuerySatisfied()? satisfiedIcon:notSatisfiedIcon;	// If we got a result from over-approximation, this is always conclusive.
		if(result.getQueryResult().hasDeadlock()) return inconclusiveIcon;
		
		switch(result.getQueryResult().queryType())
		{
		case EF:
			if(result.isQuerySatisfied()) return satisfiedIcon;
			break;
		case AG:
			if(!result.isQuerySatisfied()) return notSatisfiedIcon;
			break;
		case AF: return inconclusiveIcon;
		case EG: return inconclusiveIcon;
		default:
			return null;
		}

		return inconclusiveIcon;
		
	}
}
