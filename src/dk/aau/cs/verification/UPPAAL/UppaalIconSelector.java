package dk.aau.cs.verification.UPPAAL;


import javax.swing.ImageIcon;

import dk.aau.cs.verification.IconSelector;
import dk.aau.cs.verification.QueryResult;


public class UppaalIconSelector extends IconSelector {	
	@Override
	public ImageIcon getIconFor(QueryResult result){
		switch(result.queryType())
		{
		case AF:
		case EF:
			if(result.isQuerySatisfied()) return satisfiedIcon;
			break;
		case EG:
		case AG:
			if(!result.isQuerySatisfied()) return notSatisfiedIcon;
			break;
		default:
			return null;
		}
		
		return inconclusiveIcon;
	}
}
