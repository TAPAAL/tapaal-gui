package dk.aau.cs.verification.VerifyTAPN;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dk.aau.cs.model.tapn.TAPNQuery;
import dk.aau.cs.util.Tuple;
import dk.aau.cs.verification.BoundednessAnalysisResult;
import dk.aau.cs.verification.QueryResult;
import dk.aau.cs.verification.Stats;

public class VerifyPNCTLOutputParser extends VerifyTAPNOutputParser{
	private static final String Query_IS_NOT_SATISFIED_STRING = "Query is NOT satisfied";
	private static final String Query_IS_SATISFIED_STRING = "Query is satisfied";

    private static final Pattern configurationsPattern = Pattern.compile("\\s*Configurations:\\s*(\\d+)\\s*");
    private static final Pattern markingsPattern = Pattern.compile("\\s*Markings:\\s*(\\d+)\\s*");
    private static final Pattern edgesPattern = Pattern.compile("\\t+Edges:\\s*(\\d+)\\s*");

    private static final Pattern processedEdgesPattern = Pattern.compile("\\s*Processed Edges:\\s*(\\d+)\\s*");
    private static final Pattern processedNEdgesPattern = Pattern.compile("\\s*Processed N. Edges:\\s*(\\d+)\\s*");
    private static final Pattern exploredConfigurationsPattern = Pattern.compile("\\s*Explored Configs:\\s*(\\d+)\\s*");

    private static final Pattern maxUsedTokensPattern = Pattern.compile("\\s*Max number of tokens found in any reachable marking:\\s*(>)?(\\d+)\\s*");

	public VerifyPNCTLOutputParser(int totalTokens, int extraTokens, TAPNQuery queryType) {
        super(totalTokens, extraTokens, queryType);
    }
	
	public Tuple<QueryResult, Stats> parseOutput(String output) {
        long configurtations = 0;
        long markings = 0;
        long edges = 0;
        long processedEdges = 0;
        long processedNEdges = 0;
        long exploredConfigurations = 0;
        int maxUsedTokens = 0;
	    boolean result = false;
		boolean foundResult = false;
		String[] lines = output.split(System.getProperty("line.separator"));
        try {
			for (int i = 0; i < lines.length; i++) {
				String line = lines[i];
                if (line.contains(Query_IS_SATISFIED_STRING)) {
                    result = true;
                    foundResult = true;
                } else if (line.contains(Query_IS_NOT_SATISFIED_STRING)) {
                    result = false;
                    foundResult = true;
                } else {
                    Matcher matcher = configurationsPattern.matcher(line);
                    if (matcher.find()) {
                        configurtations = Long.parseLong(matcher.group(1));
                    }
                    matcher = markingsPattern.matcher(line);
                    if (matcher.find()) {
                        markings = Long.parseLong(matcher.group(1));
                    }
                    matcher = edgesPattern.matcher(line);
                    if (matcher.find()) {
                        edges = Long.parseLong(matcher.group(1));
                    }

                    matcher = processedEdgesPattern.matcher(line);
                    if (matcher.find()) {
                        processedEdges = Long.parseLong(matcher.group(1));
                    }
                    matcher = processedNEdgesPattern.matcher(line);
                    if (matcher.find()) {
                        processedNEdges = Long.parseLong(matcher.group(1));
                    }
                    matcher = exploredConfigurationsPattern.matcher(line);
                    if (matcher.find()) {
                        exploredConfigurations = Long.parseLong(matcher.group(1));
                    }

                    matcher = maxUsedTokensPattern.matcher(line);
                    if(matcher.find()){
                        maxUsedTokens = Integer.parseInt(matcher.group(1));
                        String operator = matcher.group(1) == null ? "" : matcher.group(1);
                        if(operator.equals(">")) maxUsedTokens += 1; // Indicate non-k-boundedness by encoding that an extra token was used.
                    }
                }
			}
			
			if(!foundResult) return null;

            BoundednessAnalysisResult boundedAnalysis = new BoundednessAnalysisResult(totalTokens, maxUsedTokens, extraTokens);
            return new Tuple<QueryResult, Stats>(new QueryResult(result, boundedAnalysis, query, false), new Stats(configurtations, markings, edges, processedEdges, processedNEdges, exploredConfigurations));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
