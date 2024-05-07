package dk.aau.cs.verification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.tapn.*;
import dk.aau.cs.model.tapn.simulation.TimedArcPetriNetTrace;
import dk.aau.cs.util.Tuple;
import pipe.gui.petrinet.PetriNetTab;

public class VerificationResult<TTrace> {
	private QueryResult queryResult;
	private TTrace trace;
	private String errorMessage = null;
	private String rawOutput = null;
	private long verificationTime = 0;
	private Stats stats;
	private NameMapping nameMapping;

	private TTrace secondaryTrace;
	private Map<String, TTrace> traceMap;
	private int bound = -1;

	private Tuple<TimedArcPetriNet, NameMapping> unfoldedModel;
    private PetriNetTab unfoldedTab;

    private boolean resolvedUsingSkeletonPreprocessor = false;

    public boolean isSolvedUsingQuerySimplification() {
        return queryResult.isSolvedUsingQuerySimplification();
    }

    public boolean isSolvedUsingTraceAbstractRefinement() {
        return queryResult.isSolvedUsingTraceAbstractRefinement();
    }

    public boolean isSolvedUsingSiphonTrap() {
        return queryResult.isSolvedUsingSiphonTrap();
    }

    public boolean isQuerySatisfied() {
		return queryResult.isQuerySatisfied();
	}

	public VerificationResult(QueryResult queryResult, TTrace trace, long verificationTime, Stats stats, String rawOutput){
		this.queryResult = queryResult;
		this.trace = trace;
		this.verificationTime = verificationTime;
		this.stats = stats;
		this.rawOutput = rawOutput;

        if (rawOutput != null) {
            String[] lines = rawOutput.split(System.getProperty("line.separator"));
            for (String line : lines) {
                Matcher matcher = Pattern.compile("\\s*--k-bound\\s*(\\d+)\\s*").matcher(line);
                if (matcher.find()) {
                    this.bound = Integer.parseInt(matcher.group(1));
                    break;
                }
            }
        }
	}

    public VerificationResult(QueryResult queryResult, Map<String, TTrace> traceMap, long verificationTime, Stats stats, boolean isSolvedUsingStateEquation, String rawOutput, Tuple<TimedArcPetriNet, NameMapping> unfoldedModel, PetriNetTab unfoldedTab){
        this.queryResult = queryResult;
        this.stats = stats;
        this.rawOutput = rawOutput;
        this.verificationTime = verificationTime;
        this.traceMap = traceMap;
        this.unfoldedModel = unfoldedModel;
        this.unfoldedTab = unfoldedTab;

        // Workaround for now
        if (traceMap != null) {
            this.trace = traceMap.get(traceMap.keySet().toArray()[0]);
        } else {
            this.trace = null;
        }
    }

	public VerificationResult(QueryResult queryResult, TTrace trace, long verificationTime, Stats stats, boolean isSolvedUsingStateEquation, String rawOutput, Tuple<TimedArcPetriNet, NameMapping> unfoldedModel, PetriNetTab unfoldedTab){
		this(queryResult, trace, verificationTime, stats, rawOutput);
		//this.solvedUsingStateEquation = isSolvedUsingStateEquation; // This was the old value using for both state equation and untimed skeleton check
		this.unfoldedModel = unfoldedModel;
        this.unfoldedTab = unfoldedTab;
	}

	public VerificationResult(QueryResult queryResult, TTrace trace, long verificationTime, String rawOutput) {
		this(queryResult, trace, verificationTime, new NullStats(), rawOutput);
	}

	public VerificationResult(String outputMessage, long verificationTime) {
		errorMessage = outputMessage;
		rawOutput = errorMessage;
		this.verificationTime = verificationTime;
	}
	
	public VerificationResult(QueryResult value1,
			TTrace tapnTrace,
			TTrace secondaryTrace2, long runningTime,
			Stats value2,
			boolean isSolvedUsingStateEquation,
            Tuple<TimedArcPetriNet, NameMapping> unfoldedModel) {
		this(value1, tapnTrace, runningTime, value2, isSolvedUsingStateEquation, null, unfoldedModel, null);
		this.secondaryTrace = secondaryTrace2;
	}

    public VerificationResult(QueryResult value1,
                              TTrace tapnTrace,
                              TTrace secondaryTrace2, long runningTime,
                              Stats value2,
                              boolean isSolvedUsingStateEquation, String rawOutput, Tuple<TimedArcPetriNet, NameMapping> unfoldedModel, PetriNetTab unfoldedTab) {
        this(value1, tapnTrace, runningTime, value2, isSolvedUsingStateEquation, rawOutput, unfoldedModel, unfoldedTab);
        this.secondaryTrace = secondaryTrace2;
    }

    public VerificationResult(QueryResult value1,
                              Map<String, TTrace> traceMap,
                              TTrace tapnTrace,
                              TTrace secondaryTrace2, long runningTime,
                              Stats value2,
                              boolean isSolvedUsingStateEquation, String rawOutput, Tuple<TimedArcPetriNet, NameMapping> unfoldedModel, PetriNetTab unfoldedTab) {
        this(value1, tapnTrace, runningTime, value2, isSolvedUsingStateEquation, rawOutput, unfoldedModel, unfoldedTab);
        this.traceMap = traceMap;
        this.secondaryTrace = secondaryTrace2;
    }

    public Map<String, TTrace> getTraceMap() {
        return this.traceMap;
    }

    public void setTraceMap(Map<String, TTrace> traceMap) {
        this.traceMap = traceMap;
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
			//String transitionName = nameMapping.map(element.value1()).value1()+ "." + nameMapping.map(element.value1()).value2();
            String transitionName = element.value1().replace("_", ".");
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
			//String placeName = nameMapping.map(element.value1()).value1()+ "." + nameMapping.map(element.value1()).value2();
            String placeName = element.value1().replace("_", ".");
			if(placeName.charAt(0) == '.'){
			    placeName = "Shared"+placeName;
			}
			Integer placeBound = element.value2();
			returnList.add(new Tuple<String, Integer>(placeName, placeBound));
		}
		returnList.sort(new transitionTupleComparator());
		return returnList;
	}

    public boolean isResolvedUsingSkeletonPreprocessor() {
        return resolvedUsingSkeletonPreprocessor;
    }
    public boolean setResolvedUsingSkeletonAnalysisPreprocessor(boolean b) {
        return this.resolvedUsingSkeletonPreprocessor = b;
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

    public Tuple<TimedArcPetriNet, NameMapping> getUnfoldedModel() { return unfoldedModel;}

    public PetriNetTab getUnfoldedTab() { return unfoldedTab;}

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
				m.add(new TimedToken(p, token.value2().value1(), ColorType.COLORTYPE_DOT.getFirstColor()));
			}
		}
		
		return m;
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

    public int getBound() {
        return bound;
    }
}
