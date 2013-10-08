package dk.aau.cs.verification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import dk.aau.cs.TCTL.visitors.HasDeadlockVisitor;
import dk.aau.cs.model.tapn.NetworkMarking;
import dk.aau.cs.model.tapn.SharedPlace;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedToken;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.util.Tuple;

public class VerificationResult<TTrace> {
	private QueryResult queryResult;
	private TTrace trace;
	private String errorMessage = null;
	private long verificationTime = 0;
	private Stats stats;
	private NameMapping nameMapping;
	
	public boolean isQuerySatisfied() {
		return queryResult.isQuerySatisfied();
	}

	public VerificationResult(QueryResult queryResult, TTrace trace, long verificationTime, Stats stats){
		this.queryResult = queryResult;
		this.trace = trace;
		this.verificationTime = verificationTime;
		this.stats = stats;
	}

	public VerificationResult(QueryResult queryResult, TTrace trace, long verificationTime) {
		this(queryResult, trace, verificationTime, new NullStats());
	}

	public VerificationResult(String outputMessage, long verificationTime) {
		errorMessage = outputMessage;
		this.verificationTime = verificationTime;
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
		Collections.sort(returnList,new transitionTupleComparator());
		return returnList;
	}
	
	public class transitionTupleComparator implements Comparator<Tuple<String,Integer>> {
		
		public int compare(Tuple<String,Integer> tuple1,Tuple<String,Integer> tuple2) {
			return (tuple1.value2() > tuple2.value2() ? -1 : (tuple1.value2() == tuple2.value2() ? 0 : 1));
		}
	}

	public QueryResult getQueryResult() {
		return queryResult;
	}

	public TTrace getTrace() {
		return trace;
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
}
