package dk.aau.cs.verification;

import dk.aau.cs.TCTL.visitors.HasDeadlockVisitor;
import dk.aau.cs.model.tapn.TAPNQuery;

public class QueryResult {
	private boolean satisfied = false;
	private boolean approximationInconclusive = false;
	private boolean discreteInclusion = false;
	private TAPNQuery query;
	private BoundednessAnalysisResult boundednessAnalysis;

	public boolean isCTL = false;
	public QueryResult(boolean satisfied, BoundednessAnalysisResult boundednessAnalysis, TAPNQuery query, boolean discreteInclusion){
		this.satisfied = satisfied;
		this.boundednessAnalysis = boundednessAnalysis;
		this.query = query;
		this.discreteInclusion = discreteInclusion;
	}
	
	public boolean isQuerySatisfied() {
		return satisfied;
	}
	
	public boolean isApproximationInconclusive() {
		return approximationInconclusive;
	}
	
	public void setApproximationInconclusive(boolean result) {
		approximationInconclusive = result;
	}
	
	public boolean isDiscreteIncludion() {
		return discreteInclusion;
	}
	
	public boolean hasDeadlock(){
		return new HasDeadlockVisitor().hasDeadLock(query.getProperty());
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		if(approximationInconclusive)
			buffer.append(getInconclusiveString());
		else {
			buffer.append("Property is ");
			buffer.append(satisfied ? "satisfied." : "not satisfied.");
		}
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
		|| (hasDeadlock() && 
				(!isQuerySatisfied() && queryType().equals(QueryType.EF)) || 
				(isQuerySatisfied() && queryType().equals(QueryType.AG))
                || (hasDeadlock() && boundednessAnalysis.isUPPAAL()) );
	}
	
	protected String getExplanationString(){
		return boundednessAnalysis.toString();
	}
	
	protected String getInconclusiveString(){
		StringBuffer buffer = new StringBuffer();
		buffer.append("The result of the approximation was inconclusive.");
		return buffer.toString();
	}
	
	public TAPNQuery getQuery() {
		return query;
	}

	public BoundednessAnalysisResult boundednessAnalysis() {
		return boundednessAnalysis;
	}
	
	public void flipResult() {
		this.satisfied = !this.satisfied;
	}
}
