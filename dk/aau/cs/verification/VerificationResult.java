package dk.aau.cs.verification;

import dk.aau.cs.petrinet.trace.TAPNTrace;

public class VerificationResult {
	private QueryResult queryResult;
	private TAPNTrace trace;
	private String errorMessage = null;

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

	public VerificationResult(String outputMessage) {
		this.errorMessage = outputMessage;
	}

	public QueryResult getQueryResult() {
		return queryResult;
	}		
	
	public TAPNTrace getTrace(){
		return trace;
	}
	
	public String errorMessage(){
		return errorMessage;
	}
	
	public boolean error(){
		return errorMessage != null;
	}
}
