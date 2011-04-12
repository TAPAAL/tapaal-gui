package dk.aau.cs.verification;

public class QueryResult {
	private boolean satisfied = false;
	private int result = -1;

	public QueryResult(boolean satisfied) {
		this.satisfied = satisfied;
	}

	public QueryResult(int integerResult) {
		this.result = integerResult;
		this.satisfied = true;
	}

	public boolean isQuerySatisfied() {
		return satisfied;
	}

	public int integerResult() {
		return result;
	}
}
