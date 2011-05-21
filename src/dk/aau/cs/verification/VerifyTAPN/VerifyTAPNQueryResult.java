package dk.aau.cs.verification.VerifyTAPN;

import dk.aau.cs.verification.QueryResult;
import dk.aau.cs.verification.QueryType;

public class VerifyTAPNQueryResult extends QueryResult {
	private BoundednessAnalysisResult boundednessAnalysis;
	
	public VerifyTAPNQueryResult(boolean satisfied, BoundednessAnalysisResult boundednessAnalysis, QueryType queryType) {
		super(satisfied, queryType);
		this.boundednessAnalysis = boundednessAnalysis;
	}
	
	
	@Override
	protected String getExplanationString() {
		return boundednessAnalysis.toString();
	}
	
	public boolean isConclusive(){
		return boundednessAnalysis.isConclusive();
	}
}
