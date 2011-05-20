package dk.aau.cs.verification.VerifyTAPN;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dk.aau.cs.util.Tuple;
import dk.aau.cs.verification.QueryResult;
import dk.aau.cs.verification.Stats;

public class VerifyTAPNOutputParser {
	private static final String Query_IS_NOT_SATISFIED_STRING = "Query is NOT satisfied";
	private static final String Query_IS_SATISFIED_STRING = "Query is satisfied";

	private static final Pattern discoveredPattern = Pattern.compile("^\\s*discovered markings:\\s*(\\d+)$");
	private static final Pattern exploredPattern = Pattern.compile("^\\s*explored markings:\\s*(\\d+)$");
	private static final Pattern storedPattern = Pattern.compile("^\\s*stored markings:\\s*(\\d+)$");
	private static final Pattern maxUsedTokensPattern = Pattern.compile("^\\s*Max used tokens:\\s*(\\d+)$");
	private final int totalTokens;
	private final QueryType queryType;
	
	public VerifyTAPNOutputParser(int totalTokens, QueryType queryType){
		this.totalTokens = totalTokens;
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
						maxUsedTokens = Integer.valueOf(matcher.group(1));
					}
				}
			}
			
			BoundednessAnalysisResult boundedAnalysis = new BoundednessAnalysisResult(totalTokens, maxUsedTokens);
			return new Tuple<QueryResult, Stats>(new VerifyTAPNQueryResult(result, boundedAnalysis, queryType), new Stats(discovered, explored, stored));
		} catch (Exception e) {
		}
		return null;
	}

}
