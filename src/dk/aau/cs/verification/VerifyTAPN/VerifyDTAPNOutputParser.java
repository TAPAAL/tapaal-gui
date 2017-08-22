package dk.aau.cs.verification.VerifyTAPN;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dk.aau.cs.model.tapn.NetworkMarking;
import dk.aau.cs.model.tapn.TAPNQuery;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.model.tapn.TimedToken;
import dk.aau.cs.util.Tuple;
import dk.aau.cs.verification.BoundednessAnalysisResult;
import dk.aau.cs.verification.NameMapping;
import dk.aau.cs.verification.QueryResult;
import dk.aau.cs.verification.QueryType;
import dk.aau.cs.verification.Stats;

public class VerifyDTAPNOutputParser {
	private static final String Query_IS_NOT_SATISFIED_STRING = "Query is NOT satisfied";
	private static final String Query_IS_SATISFIED_STRING = "Query is satisfied";
	private static final String DISCRETE_INCLUSION = "discrete inclusion";

	private static final Pattern discoveredPattern = Pattern.compile("\\s*discovered markings:\\s*(\\d+)\\s*");
	private static final Pattern exploredPattern = Pattern.compile("\\s*explored markings:\\s*(\\d+)\\s*");
	private static final Pattern storedPattern = Pattern.compile("\\s*stored markings:\\s*(\\d+)\\s*");
	private static final Pattern maxUsedTokensPattern = Pattern.compile("\\s*Max number of tokens found in any reachable marking:\\s*(>)?(\\d+)\\s*");
	private static final Pattern transitionStatsPattern = Pattern.compile("<([^:\\s]+):(\\d+)>");
        private static final Pattern placeBoundPattern = Pattern.compile("<([^;\\s]+);(\\d+)>");
        private static final Pattern placeBoundPatternUnknown = Pattern.compile("<([^;\\s]+);\\?>");
        
	private static final Pattern wfMinExecutionPattern = Pattern.compile("Minimum execution time: (-?\\d*)");
	private static final Pattern wfMaxExecutionPattern = Pattern.compile("Maximum execution time: (-?\\d*)");
	private static final Pattern wfCoveredMarkingPattern = Pattern.compile("Covered marking: (.*)");
	private final int totalTokens;
	private final TAPNQuery query;
	private final int extraTokens;
	private List<Tuple<String,Integer>> transitionStats = new ArrayList<Tuple<String,Integer>>();
	private List<Tuple<String,Integer>> placeBoundStats = new ArrayList<Tuple<String,Integer>>();
        
	public VerifyDTAPNOutputParser(int totalTokens, int extraTokens, TAPNQuery query){
		this.totalTokens = totalTokens;
		this.extraTokens = extraTokens;
		this.query = query;
	}
	
	public Tuple<QueryResult, Stats> parseOutput(String output) {
		int discovered = 0;
		int explored = 0;
		int stored = 0;
		int WFminExecutionTime = -1;
		int WFmaxExecutionTime = -1;
		ArrayList<Tuple<String, Tuple<BigDecimal, Integer>>> coveredMarking = null;
		boolean result = false;
		int maxUsedTokens = 0;
		boolean foundResult = false;
		boolean discreteInclusion = false;
		String[] lines = output.split(System.getProperty("line.separator"));
		try {			
			Matcher matcher = transitionStatsPattern.matcher(output);
			while (matcher.find()) {
				transitionStats.add(new Tuple<String,Integer>(matcher.group(1), Integer.parseInt(matcher.group(2))));
			}
			matcher = placeBoundPattern.matcher(output);
			while (matcher.find()) {
				placeBoundStats.add(new Tuple<String, Integer>(matcher.group(1), Integer.parseInt(matcher.group(2))));
			}
			matcher = placeBoundPatternUnknown.matcher(output);
			while (matcher.find()) {
				placeBoundStats.add(new Tuple<String, Integer>(matcher.group(1), -1));
			}
			for (int i = 0; i < lines.length; i++) {
				String line = lines[i];
				if (line.contains(DISCRETE_INCLUSION)) { discreteInclusion = true; }
				if (line.contains(Query_IS_SATISFIED_STRING)) {
					result = true;
					foundResult = true;
				} else if (line.contains(Query_IS_NOT_SATISFIED_STRING)) {
					result = false;
					foundResult = true;
				} else {
					matcher = discoveredPattern.matcher(line);
					if(matcher.find()){
						discovered = Integer.valueOf(matcher.group(1));
					}
					
					matcher = exploredPattern.matcher(line);
					if(matcher.find()){
						explored = Integer.valueOf(matcher.group(1));
					}
					
					matcher = storedPattern.matcher(line);
					if(matcher.find()){
						stored = Integer.valueOf(matcher.group(1));
					}
					
					matcher = maxUsedTokensPattern.matcher(line);
					if(matcher.find()){
						maxUsedTokens = Integer.valueOf(matcher.group(2));
						String operator = matcher.group(1) == null ? "" : matcher.group(1);
						if(operator.equals(">")) maxUsedTokens += 1; // Indicate non-k-boundedness by encoding that an extra token was used.
					}
					
					matcher = wfMinExecutionPattern.matcher(line);
					if(matcher.find()){
						WFminExecutionTime = Integer.valueOf(matcher.group(1));
					}
					
					matcher = wfMaxExecutionPattern.matcher(line);
					if(matcher.find()){
						WFmaxExecutionTime = Integer.valueOf(matcher.group(1));
					}
					
					matcher = wfCoveredMarkingPattern.matcher(line);
					if(matcher.find()){
						coveredMarking = new ArrayList<Tuple<String,Tuple<BigDecimal,Integer>>>();
						Pattern pattern = Pattern.compile("\\(([^,]*), (\\d), (\\d)\\)?");
						for(String s : matcher.group(1).split("\\), ")){
							Matcher m = pattern.matcher(s);
							if(m.matches()){
								for(int ii = 0; ii < Integer.parseInt(m.group(3)); ii++){
									coveredMarking.add(new Tuple<String, Tuple<BigDecimal,Integer>>(m.group(1), new Tuple<BigDecimal, Integer>(new BigDecimal(m.group(2)), Integer.parseInt(m.group(3)))));
								}
							}
						}
					}
				}
			}
			
			if(!foundResult) return null;
			
			BoundednessAnalysisResult boundedAnalysis = new BoundednessAnalysisResult(totalTokens, maxUsedTokens, extraTokens);
			Tuple<QueryResult, Stats> value = new Tuple<QueryResult, Stats>(new QueryResult(result, boundedAnalysis, query, discreteInclusion), new Stats(discovered, explored, stored, transitionStats, placeBoundStats, WFminExecutionTime, WFmaxExecutionTime, coveredMarking));
			return value; 
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
