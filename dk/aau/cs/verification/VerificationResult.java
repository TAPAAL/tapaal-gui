package dk.aau.cs.verification;

public class VerificationResult {
	private QueryResult[] queryResults;
	// TODO: MJ -- add trace

	public boolean isQuerySatisfied(int index) {
		return queryResults[index].isQuerySatisfied();
	}
		
	public VerificationResult(QueryResult... queryResults){
		this.queryResults = queryResults;
	}

	public QueryResult getQueryResult(int index) {
		return queryResults[index];
	}		
}
