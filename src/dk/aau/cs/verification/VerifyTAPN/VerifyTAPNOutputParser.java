package dk.aau.cs.verification.VerifyTAPN;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dk.aau.cs.util.Tuple;
import dk.aau.cs.verification.BoundednessAnalysisResult;
import dk.aau.cs.verification.QueryResult;
import dk.aau.cs.verification.QueryType;
import dk.aau.cs.verification.Stats;

public class VerifyTAPNOutputParser {
	private static final String Query_IS_NOT_SATISFIED_STRING = "Query is NOT satisfied";
	private static final String Query_IS_SATISFIED_STRING = "Query is satisfied";

	private static final Pattern discoveredPattern = Pattern.compile("\\s*discovered markings:\\s*(\\d+)\\s*");
	private static final Pattern exploredPattern = Pattern.compile("\\s*explored markings:\\s*(\\d+)\\s*");
	private static final Pattern storedPattern = Pattern.compile("\\s*stored markings:\\s*(\\d+)\\s*");
	private static final Pattern maxUsedTokensPattern = Pattern.compile("\\s*Max number of tokens found in any reachable marking:\\s*(>)?(\\d+)\\s*");
	private final int totalTokens;
	private final QueryType queryType;
	private final int extraTokens;
	
	public VerifyTAPNOutputParser(int totalTokens, int extraTokens, QueryType queryType){
		this.totalTokens = totalTokens;
		this.extraTokens = extraTokens;
		this.queryType = queryType;
	}
	
	public Tuple<QueryResult, Stats> parseOutput(String output) {
		int discovered = 0;
		int explored = 0;
		int stored = 0;
		boolean result = false;
		int maxUsedTokens = 0;
		
		String[] lines = output.split(System.getProperty("line.separator"));
		try {
			for (int i = 0; i < lines.length; i++) {
				String line = lines[i];
				if (line.contains(Query_IS_SATISFIED_STRING)) {
					result = true;
				} else if (line.contains(Query_IS_NOT_SATISFIED_STRING)) {
					result = false;
				} else {
					Matcher matcher = discoveredPattern.matcher(line);
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
				}
			}
			
			BoundednessAnalysisResult boundedAnalysis = new BoundednessAnalysisResult(totalTokens, maxUsedTokens, extraTokens);
			return new Tuple<QueryResult, Stats>(new QueryResult(result, boundedAnalysis, queryType), new Stats(discovered, explored, stored));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
