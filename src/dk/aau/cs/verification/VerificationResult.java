package dk.aau.cs.verification;

public class VerificationResult<TTrace> {
	private QueryResult queryResult;
	private TTrace trace;
	private String errorMessage = null;
	private long verificationTime;
	private Stats stats;

	public boolean isQuerySatisfied() {
		return queryResult.isQuerySatisfied();
	}

	public VerificationResult(QueryResult queryResult, TTrace trace, long verificationTime, Stats stats){
		this.queryResult = queryResult;
		this.trace = trace;
		this.verificationTime = verificationTime;
		this.stats = stats;
	}

	public VerificationResult(QueryResult queryResult, TTrace trace, long verificationTime) {
		this(queryResult, trace, verificationTime, new NullStats());
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
	
	public Stats stats(){
		return stats;
	}

	public boolean error() {
		return errorMessage != null;
	}

	public long verificationTime() {

		return verificationTime;
	}

	public Object getSummaryString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(queryResult.toString());
		buffer.append(System.getProperty("line.separator"));
		buffer.append(System.getProperty("line.separator"));
		buffer.append(stats.toString());
		buffer.append(String.format("Estimated verification time: %1$.2fs", verificationTime() / 1000.0));
		return buffer.toString();
	}
}
