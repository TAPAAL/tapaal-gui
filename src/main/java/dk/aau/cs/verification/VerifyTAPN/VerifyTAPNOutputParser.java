package dk.aau.cs.verification.VerifyTAPN;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dk.aau.cs.model.tapn.TAPNQuery;
import dk.aau.cs.util.Tuple;
import dk.aau.cs.verification.BoundednessAnalysisResult;
import dk.aau.cs.verification.QueryResult;
import dk.aau.cs.verification.Stats;

public class VerifyTAPNOutputParser {
	private static final String Query_IS_NOT_SATISFIED_STRING = "Query is NOT satisfied";
	private static final String Query_IS_SATISFIED_STRING = "Query is satisfied";
	private static final String DISCRETE_INCLUSION = "discrete inclusion";

	private static final Pattern discoveredPattern = Pattern.compile("\\s*discovered markings:\\s*(\\d+)\\s*");
	private static final Pattern exploredPattern = Pattern.compile("\\s*explored markings:\\s*(\\d+)\\s*");
	private static final Pattern storedPattern = Pattern.compile("\\s*stored markings:\\s*(\\d+)\\s*");
	private static final Pattern maxUsedTokensPattern = Pattern.compile("\\s*Max number of tokens found in any reachable marking:\\s*(>)?(\\d+)\\s*");
	private static final Pattern transitionStatsPattern = Pattern.compile("<([^:\\s]+):(\\d+)>");

	protected final int totalTokens;
	protected final TAPNQuery query;
	protected final int extraTokens;
	protected final List<Tuple<String,Number>> transitionStats = new ArrayList<>();
    protected final List<Tuple<String,Number>> placeBoundStats = new ArrayList<>();

	public VerifyTAPNOutputParser(int totalTokens, int extraTokens, TAPNQuery query){
		this.totalTokens = totalTokens;
		this.extraTokens = extraTokens;
		this.query = query;
	}

    private static final String SolvedUsingQuerySimplification_STRING = "Query solved by Query Simplification.";
    boolean solvedUsingQuerySimplification = false;
    private static final String solvedUsingTraceAbstractRefinement_STRING = "Solved using Trace Abstraction Refinement";
    boolean solvedUsingTraceAbstractRefinement = false;
    private static final String solvedUsingSiphonTrap_STRING = "Query solved by Siphon-Trap Analysis.";
    boolean solvedUsingSiphonTrap  = false;

    void parseSolvedMethod(String line) {
        if (line.contains(SolvedUsingQuerySimplification_STRING)) {
            solvedUsingQuerySimplification = true;
        } else if (line.contains(solvedUsingTraceAbstractRefinement_STRING)) {
            solvedUsingTraceAbstractRefinement = true;
        } else if (line.contains(solvedUsingSiphonTrap_STRING)) {
            solvedUsingSiphonTrap = true;
        }
    }
	
	public Tuple<QueryResult, Stats> parseOutput(String output) {
		int discovered = 0;
		int explored = 0;
		int stored = 0;
		boolean result = false;
		int maxUsedTokens = 0;
		boolean foundResult = false;
		boolean discreteInclusion = false;
		String[] lines = output.split(System.getProperty("line.separator"));
		try {			
			Matcher matcher = transitionStatsPattern.matcher(output);
			while (matcher.find()) {
				transitionStats.add(new Tuple<String,Number>(matcher.group(1), Integer.parseInt(matcher.group(2))));
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
						discovered = Integer.parseInt(matcher.group(1));
					}
					
					matcher = exploredPattern.matcher(line);
					if(matcher.find()){
						explored = Integer.parseInt(matcher.group(1));
					}
					
					matcher = storedPattern.matcher(line);
					if(matcher.find()){
						stored = Integer.parseInt(matcher.group(1));
					}
					
					matcher = maxUsedTokensPattern.matcher(line);
					if(matcher.find()){
						maxUsedTokens = Integer.parseInt(matcher.group(2));
						String operator = matcher.group(1) == null ? "" : matcher.group(1);
						if(operator.equals(">")) maxUsedTokens += 1; // Indicate non-k-boundedness by encoding that an extra token was used.
					}
				}
			}
			
			if(!foundResult) return null;
			
			BoundednessAnalysisResult boundedAnalysis = new BoundednessAnalysisResult(totalTokens, maxUsedTokens, extraTokens);
			return new Tuple<QueryResult, Stats>(new QueryResult(result, boundedAnalysis, query, discreteInclusion), new Stats(discovered, explored, stored, transitionStats, placeBoundStats));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
