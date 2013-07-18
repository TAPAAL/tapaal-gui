package dk.aau.cs.verification;

import dk.aau.cs.TCTL.visitors.HasDeadlockVisitor;
import dk.aau.cs.model.tapn.TAPNQuery;

public class QueryResult {
	private boolean satisfied = false;
	private boolean discreteInclusion = false;
	private TAPNQuery query;
	private BoundednessAnalysisResult boundednessAnalysis;

	public QueryResult(boolean satisfied, BoundednessAnalysisResult boundednessAnalysis, TAPNQuery query, boolean discreteInclusion){
		this.satisfied = satisfied;
		this.boundednessAnalysis = boundednessAnalysis;
		this.query = query;
		this.discreteInclusion = discreteInclusion;
	}
	
	public boolean isQuerySatisfied() {
		return satisfied;
	}
	
	public boolean isDiscreteIncludion() {
		return discreteInclusion;
	}
	
	public boolean hasDeadlock(){
		return new HasDeadlockVisitor().hasDeadLock(query.getProperty());
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
		return query.queryType();
	}
	
	private boolean shouldAddExplanation() {
		return (queryType().equals(QueryType.EF) && !isQuerySatisfied()) 
		|| (queryType().equals(QueryType.EG)) // && !isQuerySatisfied()) 
		|| (queryType().equals(QueryType.AF)) // && isQuerySatisfied())
		|| (queryType().equals(QueryType.AG) && isQuerySatisfied())
		|| hasDeadlock();
	}
	
	protected String getExplanationString(){
		return boundednessAnalysis.toString();
	}

	public BoundednessAnalysisResult boundednessAnalysis() {
		return boundednessAnalysis;
	}
}
