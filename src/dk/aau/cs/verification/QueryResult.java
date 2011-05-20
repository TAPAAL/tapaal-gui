package dk.aau.cs.verification;

public class QueryResult {
	private boolean satisfied = false;

	public QueryResult(boolean satisfied){
		this.satisfied = satisfied;
	}
	
	public boolean isQuerySatisfied() {
		return satisfied;
	}
	
	@Override
	public String toString() {
		return "Property is " + (satisfied ? "satisfied." : "not satisfied.");
	}
	
	// TODO: delete me
	public int integerResult() {
		// TODO Auto-generated method stub
		return 0;
	}
}
