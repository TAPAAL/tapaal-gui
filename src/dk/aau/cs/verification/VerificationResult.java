package dk.aau.cs.verification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import dk.aau.cs.model.tapn.NetworkMarking;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedToken;
import dk.aau.cs.util.Tuple;

public class VerificationResult<TTrace> {
	private QueryResult queryResult;
	private TTrace trace;
	private String errorMessage = null;
	private String rawOutput = null;
	private long verificationTime = 0;
	private Stats stats;
	private NameMapping nameMapping;
	private TTrace secondaryTrace;
	private boolean isSolvedUsingStateEquation = false;
	
	public boolean isQuerySatisfied() {
		return queryResult.isQuerySatisfied();
	}

	public VerificationResult(QueryResult queryResult, TTrace trace, long verificationTime, Stats stats, String rawOutput){
		this.queryResult = queryResult;
		this.trace = trace;
		this.verificationTime = verificationTime;
		this.stats = stats;
		this.rawOutput = rawOutput;
	}

    public VerificationResult(QueryResult queryResult, TTrace trace, long verificationTime, Stats stats, boolean isSolvedUsingStateEquation){
        this(queryResult, trace, verificationTime, stats, null);
        this.isSolvedUsingStateEquation = isSolvedUsingStateEquation;
    }
	
	public VerificationResult(QueryResult queryResult, TTrace trace, long verificationTime, Stats stats, boolean isSolvedUsingStateEquation, String rawOutput){
		this(queryResult, trace, verificationTime, stats, rawOutput);
		this.isSolvedUsingStateEquation = isSolvedUsingStateEquation;
	}

	public VerificationResult(QueryResult queryResult, TTrace trace, long verificationTime, String rawOutput) {
		this(queryResult, trace, verificationTime, new NullStats(), rawOutput);
	}

	public VerificationResult(String outputMessage, long verificationTime) {
		errorMessage = outputMessage;
		this.verificationTime = verificationTime;
	}
	
	public VerificationResult(QueryResult value1,
			TTrace tapnTrace,
			TTrace secondaryTrace2, long runningTime,
			Stats value2,
			boolean isSolvedUsingStateEquation) {
		this(value1, tapnTrace, runningTime, value2, isSolvedUsingStateEquation, null);
		this.secondaryTrace = secondaryTrace2;
	}

    public VerificationResult(QueryResult value1,
                              TTrace tapnTrace,
                              TTrace secondaryTrace2, long runningTime,
                              Stats value2,
                              boolean isSolvedUsingStateEquation, String rawOutput) {
        this(value1, tapnTrace, runningTime, value2, isSolvedUsingStateEquation, rawOutput);
        this.secondaryTrace = secondaryTrace2;
    }

	public NameMapping getNameMapping() {
		return nameMapping;
	}
	
	public void setNameMapping(NameMapping nameMapping) {
		this.nameMapping = nameMapping;
	}
	
	public List<Tuple<String,Integer>> getTransitionStatistics() {
		List<Tuple<String,Integer>> returnList = new ArrayList<Tuple<String,Integer>>();
		for (int i = 0; i < stats.transitionsCount();i++) {
			Tuple<String,Integer> element = stats.getTransitionStats(i);
			String transitionName = nameMapping.map(element.value1()).value1()+ "." + nameMapping.map(element.value1()).value2();
			if(transitionName.charAt(0) == '.'){
				transitionName = "Shared"+transitionName;
			}
			Integer transitionFired = element.value2();
			returnList.add(new Tuple<String, Integer>(transitionName, transitionFired));
		}
		returnList.sort(new transitionTupleComparator());
		return returnList;
	}
        
	public List<Tuple<String,Integer>> getPlaceBoundStatistics() {
		List<Tuple<String,Integer>> returnList = new ArrayList<Tuple<String,Integer>>();
		for (int i = 0; i < stats.placeBoundCount();i++) {
			Tuple<String,Integer> element = stats.getPlaceBoundStats(i);
			String placeName = nameMapping.map(element.value1()).value1()+ "." + nameMapping.map(element.value1()).value2();
			if(placeName.charAt(0) == '.'){
                            placeName = "Shared"+placeName;
			}
			Integer placeBound = element.value2();
			returnList.add(new Tuple<String, Integer>(placeName, placeBound));
		}
		returnList.sort(new transitionTupleComparator());
		return returnList;
	}
	
	public static class transitionTupleComparator implements Comparator<Tuple<String,Integer>> {
		
		public int compare(Tuple<String,Integer> tuple1,Tuple<String,Integer> tuple2) {
            return tuple2.value2() - tuple1.value2();
		}
	}

	public QueryResult getQueryResult() {
		return queryResult;
	}

	public void setTrace(TTrace newTrace){
		trace = newTrace;
	}
	
	public void setSecondaryTrace(TTrace newTrace){
		secondaryTrace = newTrace;
	}
	
	public TTrace getTrace() {
		return trace;
	}
	
	public TTrace getSecondaryTrace() {
		return secondaryTrace;
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

	public String getVerificationTimeString() {
		return String.format("Estimated verification time: %1$.2fs", verificationTime() / 1000.0);
	}
	
	public String getStatsAsString(){
		return stats.toString();
	}

	public boolean isBounded() {
		return queryResult.boundednessAnalysis().boundednessResult().equals(Boundedness.Bounded);
	}
	
	public String getResultString() {
		if (queryResult.isDiscreteIncludion() && !queryResult.boundednessAnalysis().boundednessResult().equals(Boundedness.Bounded) &&
				((!queryResult.isQuerySatisfied() && queryResult.queryType().equals(QueryType.EF) 
			       ||			
			    (queryResult.isQuerySatisfied() && queryResult.queryType().equals(QueryType.AG)))))
	 {return "Verification is inconclusive.\nDisable discrete inclusion or add extra tokens and try again.";  }
		return queryResult.toString();
	}
	
	public String getReductionResultAsString(){
		ReductionStats reductionStats = stats.getReductionStats();
		if(reductionStats == null){
			return "";
		}
		return reductionStats.toString();
	}

	public boolean reductionRulesApplied(){
        ReductionStats reductionStats = stats.getReductionStats();
        return (reductionStats.getRemovedPlaces() + reductionStats.getRemovedTransitions()) > 0;
    }
	
	public NetworkMarking getCoveredMarking(TimedArcPetriNetNetwork model){
		
		if(stats.getCoveredMarking() == null)	return null;
		
		NetworkMarking m = model.marking().clone();
		
		m.clear();
		
		for(Tuple<String, Tuple<BigDecimal, Integer>> token : stats.getCoveredMarking()){
			Tuple<String, String> originalName = nameMapping.map(token.value1());
			TimedPlace p = (originalName.value1() == null || originalName.value1().isEmpty()) ? model.getSharedPlaceByName(originalName.value2()) : model.getTAPNByName(originalName.value1()).getPlaceByName(originalName.value2());
			for(int i = 0; i < token.value2().value2(); i++){
				m.add(new TimedToken(p, token.value2().value1()));
			}
		}
		
		return m;
	}
	
	public boolean isSolvedUsingStateEquation(){
		return isSolvedUsingStateEquation;
	}
	
	public void addTime(long timeToAdd) {
		verificationTime += timeToAdd;
	}

	public String getCTLStatsAsString(){
		StringBuilder buffer = new StringBuilder();
		buffer.append("Explored configurations: ");
		buffer.append(stats.getConfigurations());
		buffer.append(System.getProperty("line.separator"));

		buffer.append("Explored markings: ");
		buffer.append(stats.getMarkings());
		buffer.append(System.getProperty("line.separator"));

		buffer.append("Explored hyper-edges: ");
		buffer.append(stats.getEdges());
		return buffer.toString();
	}

	public String getRawOutput() {
	    return rawOutput;
    }
}
