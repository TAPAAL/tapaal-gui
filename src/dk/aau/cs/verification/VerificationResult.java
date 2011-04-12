package dk.aau.cs.verification;

public class VerificationResult<TTrace> {
	private QueryResult queryResult;
	private TTrace trace;
	private String errorMessage = null;
	private long verificationTime;

	public boolean isQuerySatisfied() {
		return queryResult.isQuerySatisfied();
	}


	public VerificationResult(QueryResult queryResult, TTrace trace,
			long verificationTime) {

		this.queryResult = queryResult;
		this.trace = trace;
		this.verificationTime = verificationTime;
	}

	public VerificationResult(String outputMessage) {
		this.errorMessage = outputMessage;
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

	public boolean error() {
		return errorMessage != null;
	}

	public long verificationTime() {

		return verificationTime;
	}
}
