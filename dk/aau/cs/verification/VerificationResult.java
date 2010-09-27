package dk.aau.cs.verification;

public class VerificationResult {
	private QueryResult[] queryResults;
	private Trace[] traces;

	public boolean isQuerySatisfied(int index) {
		return queryResults[index].isQuerySatisfied();
	}
		
	public VerificationResult(QueryResult[] queryResults, Trace[] traces){
		this.queryResults = queryResults;
	}

	public QueryResult getQueryResult(int index) {
		return queryResults[index];
	}		
	
	public Trace getTrace(int index){
		if(traces == null || index >= traces.length){
			return null;
		}else{
			return traces[index];
		}
	}
}
