package dk.aau.cs.verification;

public class VerificationResult<TTrace> {
	private QueryResult queryResult;
	private TTrace trace;
	private String errorMessage = null;
	private long verificationTime = 0;
	private Stats stats;
	private String humanTrace = null;
	
	public boolean isQuerySatisfied() {
		return queryResult.isQuerySatisfied();
	}

	public VerificationResult(QueryResult queryResult, TTrace trace, long verificationTime, String humanTrace, Stats stats){
		this.queryResult = queryResult;
		this.trace = trace;
		this.verificationTime = verificationTime;
		this.stats = stats;
		this.humanTrace = humanTrace;
	}

	public VerificationResult(QueryResult queryResult, TTrace trace, long verificationTime, String humanTrace) {
		this(queryResult, trace, verificationTime, humanTrace, new NullStats());
	}

	public VerificationResult(String outputMessage, long verificationTime) {
		errorMessage = outputMessage;
		this.verificationTime = verificationTime;
	}
	
	public String getHumanTrace(){
		return humanTrace;
	}

	public QueryResult getQueryResult() {
		return queryResult;
	}

	public TTrace getTrace() {
		return trace;
	}

	public String errorMessage() {
		return errorMessage;
	}
	
	public Stats stats(){
		return stats;
	}

	public boolean error() {
		return errorMessage != null;
	}

	public long verificationTime() {

		return verificationTime;
	}

	public String getVerificationTimeString() {
		return String.format("Estimated verification time: %1$.2fs", verificationTime() / 1000.0);
	}
	
	public String getStatsAsString(){
		return stats.toString();
	}

	public boolean isBounded() {
		return queryResult.boundednessAnalysis().boundednessResult().equals(Boundedness.Bounded);
	}
	
	public String getResultString() {
		if (queryResult.isDiscreteIncludion() && !queryResult.boundednessAnalysis().boundednessResult().equals(Boundedness.Bounded) &&
				((!queryResult.isQuerySatisfied() && queryResult.queryType.equals(QueryType.EF) 
			       ||			
			    (queryResult.isQuerySatisfied() && queryResult.queryType.equals(QueryType.AG)))))
	 {return "Verification is inconclusive.\nDisable discrete inclusion or add extra tokens and try again.";  }
		return queryResult.toString();
	}
}
