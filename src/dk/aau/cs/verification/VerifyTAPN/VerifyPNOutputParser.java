package dk.aau.cs.verification.VerifyTAPN;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dk.aau.cs.model.tapn.TAPNQuery;
import dk.aau.cs.util.Tuple;
import dk.aau.cs.verification.BoundednessAnalysisResult;
import dk.aau.cs.verification.QueryResult;
import dk.aau.cs.verification.QueryType;
import dk.aau.cs.verification.Stats;

public class VerifyPNOutputParser extends VerifyTAPNOutputParser{
	private static final String Query_IS_NOT_SATISFIED_STRING = "Query is NOT satisfied";
	private static final String Query_IS_SATISFIED_STRING = "Query is satisfied";

	private static final Pattern discoveredPattern = Pattern.compile("\\s*explored states:\\s*(\\d+)\\s*");
	private static final Pattern exploredPattern = Pattern.compile("\\s*expanded states:\\s*(\\d+)\\s*");
	
	public VerifyPNOutputParser(int totalTokens, int extraTokens, TAPNQuery queryType) {
		super(totalTokens, extraTokens, queryType);
	}
	
	public Tuple<QueryResult, Stats> parseOutput(String output) {
		int discovered = 0;
		int explored = 0;
		boolean result = false;
		boolean foundResult = false;
		String[] lines = output.split(System.getProperty("line.separator"));
		try {			
			Matcher matcher;
			for (int i = 0; i < lines.length; i++) {
				String line = lines[i];
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
				}
			}
			
			if(!foundResult) return null;
			BoundednessAnalysisResult boundedAnalysis = new BoundednessAnalysisResult(totalTokens, totalTokens + extraTokens + (foundResult? 0:1), extraTokens);
			Tuple<QueryResult, Stats> value = new Tuple<QueryResult, Stats>(new QueryResult(result, boundedAnalysis, query, false), new Stats(discovered, explored, explored, transitionStats));
			return value; 
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
