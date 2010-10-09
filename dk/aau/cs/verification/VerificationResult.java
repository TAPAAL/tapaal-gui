package dk.aau.cs.verification;

import dk.aau.cs.petrinet.trace.TAPNTrace;

public class VerificationResult {
	private QueryResult queryResult;
	private TAPNTrace trace;

	public boolean isQuerySatisfied() {
		return queryResult.isQuerySatisfied();
	}
		
	public VerificationResult(QueryResult queryResult){
		this(queryResult, null);
	}
	
	public VerificationResult(QueryResult queryResult, TAPNTrace trace){
		this.queryResult = queryResult;
		this.trace = trace;
	}

	public QueryResult getQueryResult() {
		return queryResult;
	}		
	
	public TAPNTrace getTrace(){
		return trace;
	}
}
