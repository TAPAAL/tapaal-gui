package dk.aau.cs.verification.VerifyTAPN;

import dk.aau.cs.verification.QueryResult;

public class VerifyTAPNQueryResult extends QueryResult {
	private BoundednessAnalysisResult boundednessAnalysis;
	private QueryType queryType;
	
	public VerifyTAPNQueryResult(boolean satisfied, BoundednessAnalysisResult boundednessAnalysis, QueryType queryType) {
		super(satisfied);
		this.boundednessAnalysis = boundednessAnalysis;
		this.queryType = queryType;
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer(super.toString());
		buffer.append(getExplanationString());
		return buffer.toString();
	}

	private String getExplanationString() {
		if(queryType.equals(QueryType.EF) && !isQuerySatisfied() || queryType.equals(QueryType.AG) && isQuerySatisfied()){
			return System.getProperty("line.separator") + boundednessAnalysis.toString();
		}else{
			return "";
		}
	}
}
