package dk.aau.cs.verification;

import dk.aau.cs.TCTL.visitors.HasDeadlockVisitor;
import dk.aau.cs.model.tapn.TAPNQuery;

public class QueryResult {
	private final boolean satisfied;
    private final String quantitativeResult;
	private boolean approximationInconclusive = false;
	private final boolean discreteInclusion;
	private final TAPNQuery query;
	private final BoundednessAnalysisResult boundednessAnalysis;

    public boolean isSolvedUsingQuerySimplification() {
        return solvedUsingQuerySimplification;
    }

    public void setSolvedUsingQuerySimplification(boolean solvedUsingQuerySimplification) {
        this.solvedUsingQuerySimplification = solvedUsingQuerySimplification;
    }

    public boolean isSolvedUsingTraceAbstractRefinement() {
        return solvedUsingTraceAbstractRefinement;
    }

    public void setSolvedUsingTraceAbstractRefinement(boolean solvedUsingTraceAbstractRefinement) {
        this.solvedUsingTraceAbstractRefinement = solvedUsingTraceAbstractRefinement;
    }

    public boolean isSolvedUsingSiphonTrap() {
        return solvedUsingSiphonTrap;
    }

    public void setSolvedUsingSiphonTrap(boolean solvedUsingSiphonTrap) {
        this.solvedUsingSiphonTrap = solvedUsingSiphonTrap;
    }

    private boolean solvedUsingQuerySimplification;
    private boolean solvedUsingTraceAbstractRefinement;
    private boolean solvedUsingSiphonTrap;

	public boolean isCTL = false;
	public QueryResult(boolean satisfied, BoundednessAnalysisResult boundednessAnalysis, TAPNQuery query, boolean discreteInclusion){
		this.satisfied = satisfied;
		this.boundednessAnalysis = boundednessAnalysis;
		this.query = query;
		this.discreteInclusion = discreteInclusion;
        this.quantitativeResult = "";
	}

    public QueryResult(String quantitativeResult, BoundednessAnalysisResult boundednessAnalysis, TAPNQuery query, boolean discreteInclusion){
        this.satisfied = true;
        this.boundednessAnalysis = boundednessAnalysis;
        this.query = query;
        this.discreteInclusion = discreteInclusion;
        this.quantitativeResult = quantitativeResult;
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
		StringBuilder buffer = new StringBuilder();
		if(approximationInconclusive)
			buffer.append(getInconclusiveString());
		else if(this.quantitativeResult.isEmpty()) {
			buffer.append("Property is ");
			buffer.append(satisfied ? "satisfied." : "not satisfied.");
		} else {
            buffer.append("Verification result: ").append(quantitativeResult);
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
        || (queryType().equals(QueryType.A))
        || (queryType().equals(QueryType.E))
        || (isSMC())
		|| (hasDeadlock() && 
				(!isQuerySatisfied() && queryType().equals(QueryType.EF)) || 
				(isQuerySatisfied() && queryType().equals(QueryType.AG))
                || (hasDeadlock() && boundednessAnalysis.isUPPAAL()) );
	}
	
	protected String getExplanationString(){
        if(isSMC()) {
            return  "<br/>SMC Settings : " +
                    "<br/>Semantics used: " + query.getSMCSettings().semantics.toString() +
                    "<br/>Runs bound: " + query.getSMCSettings().boundType.toString() + " &lt; " + query.getSMCSettings().boundValue +
                    "<br/>Default rate : " + query.getSMCSettings().defaultRate +
                ((isQuantitative()) ?
                    "<br/>Confidence: " + (query.getSMCSettings().confidence * 100) + "%" :
                        "<br/>Probability of false positive: " + query.getSMCSettings().falsePositives +
                        "<br/>Probability of false negative: " + query.getSMCSettings().falseNegatives +
                        "<br/>Indifference region: [" + (query.getSMCSettings().geqThan - query.getSMCSettings().indifferenceWidth) +
                        ";" +  (query.getSMCSettings().geqThan + query.getSMCSettings().indifferenceWidth) + "]"
                    );
        } else {
            return boundednessAnalysis.toString();
        }
	}
	
	protected String getInconclusiveString(){
        return "The result of the approximation was inconclusive.";
	}

	public BoundednessAnalysisResult boundednessAnalysis() {
		return boundednessAnalysis;
	}

    public TAPNQuery getQuery() {
        return query;
    }

    public boolean isSMC() {
        return query.getCategory() == net.tapaal.gui.petrinet.verification.TAPNQuery.QueryCategory.SMC;
    }

    public boolean isQuantitative() {
        return !quantitativeResult.isEmpty();
    }
}
