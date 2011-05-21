package dk.aau.cs.verification;


public class QueryResult {
	private boolean satisfied = false;
	protected QueryType queryType;

	public QueryResult(boolean satisfied, QueryType queryType){
		this.satisfied = satisfied;
		this.queryType = queryType;
	}
	
	public boolean isQuerySatisfied() {
		return satisfied;
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer( "Property is ");
		buffer.append(satisfied ? "satisfied." : "not satisfied.");
		if(shouldAddExplanation())
			buffer.append(getExplanationString());
		return buffer.toString();
	}
	
	
	private boolean shouldAddExplanation() {
		return (queryType.equals(QueryType.EF) && !isQuerySatisfied()) 
		|| (queryType.equals(QueryType.EG) && isQuerySatisfied()) 
		|| (queryType.equals(QueryType.AF) && !isQuerySatisfied())
		|| (queryType.equals(QueryType.AG) && isQuerySatisfied());
	}
	
	protected String getExplanationString(){
		return "\nThe answer is conclusive only if the net is bounded\nfor the given number of extra tokens.";
	}

	// TODO: delete me
	public int integerResult() {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean isConclusive() {
		return true;
	}
}
