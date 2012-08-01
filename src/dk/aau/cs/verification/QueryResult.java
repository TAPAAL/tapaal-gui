package dk.aau.cs.verification;

public class QueryResult {
	private boolean satisfied = false;
	private boolean discreteInclusion = false;
	protected QueryType queryType;
	private BoundednessAnalysisResult boundednessAnalysis;

	public QueryResult(boolean satisfied, BoundednessAnalysisResult boundednessAnalysis, QueryType queryType, boolean discreteInclusion){
		this.satisfied = satisfied;
		this.boundednessAnalysis = boundednessAnalysis;
		this.queryType = queryType;
		this.discreteInclusion = discreteInclusion;
	}
	
	public boolean isQuerySatisfied() {
		return satisfied;
	}
	
	public boolean isDiscreteIncludion() {
		return discreteInclusion;
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer( "Property is ");
		buffer.append(satisfied ? "satisfied." : "not satisfied.");
		if(shouldAddExplanation())
			buffer.append(getExplanationString());
		return buffer.toString();
	}
	
	public QueryType queryType(){
		return queryType;
	}
	
	private boolean shouldAddExplanation() {
		return (queryType.equals(QueryType.EF) && !isQuerySatisfied()) 
		|| (queryType.equals(QueryType.EG) && !isQuerySatisfied()) 
		|| (queryType.equals(QueryType.AF) && isQuerySatisfied())
		|| (queryType.equals(QueryType.AG) && isQuerySatisfied());
	}
	
	protected String getExplanationString(){
		return boundednessAnalysis.toString();
	}

	public BoundednessAnalysisResult boundednessAnalysis() {
		return boundednessAnalysis;
	}
}
