package dk.aau.cs.verification.VerifyTAPN;

import javax.swing.ImageIcon;

import dk.aau.cs.verification.Boundedness;
import dk.aau.cs.verification.IconSelector;
import dk.aau.cs.verification.QueryResult;

public class VerifyTAPNIconSelector extends IconSelector {

	@Override
	public ImageIcon getIconFor(QueryResult result) {
		switch(result.queryType())
		{
		case EF:
			if(result.isQuerySatisfied()){
				return satisfiedIcon;
			}else if(!result.isQuerySatisfied() && result.boundednessAnalysis().boundednessResult().equals(Boundedness.Bounded)){
				return notSatisfiedIcon;
			}
			break;
		case AG:
			if(!result.isQuerySatisfied()) return notSatisfiedIcon;
			else if(result.isQuerySatisfied() && result.boundednessAnalysis().boundednessResult().equals(Boundedness.Bounded)) return satisfiedIcon;
			break;
		case AF:
			if(!result.isQuerySatisfied() && result.boundednessAnalysis().boundednessResult().equals(Boundedness.Bounded)){
				return notSatisfiedIcon;
			} else if(result.isQuerySatisfied() && result.boundednessAnalysis().boundednessResult().equals(Boundedness.Bounded)){
				return satisfiedIcon;
			}
			break;
		case EG:
			if(result.isQuerySatisfied() && result.boundednessAnalysis().boundednessResult().equals(Boundedness.Bounded)) return satisfiedIcon;
			else if(!result.isQuerySatisfied() && result.boundednessAnalysis().boundednessResult().equals(Boundedness.Bounded)) return notSatisfiedIcon;
			break;
		default:
			return null;
		}
		
		if (result.isDiscreteIncludion()) {return rerunIcon;} 
		else { return inconclusiveIcon;}
	}
}
