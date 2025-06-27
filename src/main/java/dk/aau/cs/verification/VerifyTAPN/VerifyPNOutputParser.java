package dk.aau.cs.verification.VerifyTAPN;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dk.aau.cs.model.tapn.TAPNQuery;
import dk.aau.cs.util.Tuple;
import dk.aau.cs.verification.BoundednessAnalysisResult;
import dk.aau.cs.verification.QueryResult;
import dk.aau.cs.verification.ReductionStats;
import dk.aau.cs.verification.Stats;

public class VerifyPNOutputParser extends VerifyTAPNOutputParser{
	private static final String Query_IS_NOT_SATISFIED_STRING = "Query is NOT satisfied";
	private static final String Query_IS_SATISFIED_STRING = "Query is satisfied";
    private static final String Query_IS_MAYBE_SATISFIED_STRING = "Query is MAYBE satisfied";

	private static final Pattern discoveredPattern = Pattern.compile("\\s*discovered states:\\s*(\\d+)\\s*");
	private static final Pattern exploredPattern = Pattern.compile("\\s*explored states:\\s*(\\d+)\\s*");
	private static final Pattern maxUsedTokensPattern = Pattern.compile("\\s*max tokens:\\s*(\\d+)\\s*");
	private static final Pattern transitionStatsPattern = Pattern.compile("<([^:\\s]+):(\\d+)>");
	private static final Pattern transitionStatsPatternUnknown = Pattern.compile("<([^:\\s]+):\\?>");
	private static final Pattern placeBoundPattern = Pattern.compile("<([^;\\s]+);(\\d+)>");
	private static final Pattern placeBoundPatternUnknown = Pattern.compile("<([^;\\s]+);\\?>");
        
	/* Reductions */
	private static final Pattern reductionsUsedPattern = Pattern.compile("\\s*Net reduction is enabled.\\s*");
	private static final Pattern removedTransitionsPattern = Pattern.compile("\\s*Removed transitions:\\s*(\\d+)\\s*");
	private static final Pattern removedPlacesPattern = Pattern.compile("\\s*Removed places:\\s*(\\d+)\\s*");

	public VerifyPNOutputParser(int totalTokens, int extraTokens, TAPNQuery queryType) {
		super(totalTokens, extraTokens, queryType);
	}
	
	public Tuple<QueryResult, Stats> parseOutput(String output) {
		int discovered = 0;
		int explored = 0;
		int maxUsedTokens = 0;
		int removedTransitions = 0;
		int removedPlaces = 0;

		boolean reductionUsed = false;
		boolean result = false;
		boolean foundResult = false;
        boolean isInconclusive = false;
		String[] lines = output.split(System.getProperty("line.separator"));
            try {
                Matcher matcher = transitionStatsPattern.matcher(output);
                while (matcher.find()) {
                    transitionStats.add(new Tuple<>(matcher.group(1), Integer.parseInt(matcher.group(2))));
                }

                matcher = transitionStatsPatternUnknown.matcher(output);
                while (matcher.find()) {
                    transitionStats.add(new Tuple<>(matcher.group(1), -1));
                }
                matcher = placeBoundPattern.matcher(output);
                while (matcher.find()) {
                    placeBoundStats.add(new Tuple<>(matcher.group(1), Integer.parseInt(matcher.group(2))));
                }
                matcher = placeBoundPatternUnknown.matcher(output);
                while (matcher.find()) {
                    placeBoundStats.add(new Tuple<>(matcher.group(1), -1));
                }

			for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                if (line.contains(Query_IS_SATISFIED_STRING)) {
                    result = true;
                    foundResult = true;
                    isInconclusive = false;
                } else if (line.contains(Query_IS_MAYBE_SATISFIED_STRING)) {
                    result = true;
                    foundResult = true;
                    isInconclusive = true;
                } else if (line.contains(Query_IS_NOT_SATISFIED_STRING)) {
                    result = false;
                    foundResult = true;
                    isInconclusive = false;
                } else {
                    parseSolvedMethod(line);

                    matcher = discoveredPattern.matcher(line);
                    if (matcher.find()) {
                        discovered = Integer.parseInt(matcher.group(1));
                    }

                    matcher = exploredPattern.matcher(line);
                    if (matcher.find()) {
                        explored = Integer.parseInt(matcher.group(1));
                    }

                    matcher = maxUsedTokensPattern.matcher(line);
                    if (matcher.find()) {
                        maxUsedTokens = Integer.parseInt(matcher.group(1));
                        String operator = matcher.group(1) == null ? "" : matcher.group(1);
                        if (operator.equals(">"))
                            maxUsedTokens += 1; // Indicate non-k-boundedness by encoding that an extra token was used.
                    }

                    matcher = reductionsUsedPattern.matcher(line);
                    if (matcher.find()) {
                        reductionUsed = true;
                    }

                    matcher = removedTransitionsPattern.matcher(line);
                    if (matcher.find()) {
                        removedTransitions = Integer.parseInt(matcher.group(1));
                    }

                    matcher = removedPlacesPattern.matcher(line);
                    if (matcher.find()) {
                        removedPlaces = Integer.parseInt(matcher.group(1));
                    }
                }
            }
			
			if(!foundResult) return null;
			BoundednessAnalysisResult boundedAnalysis = new BoundednessAnalysisResult(totalTokens, maxUsedTokens, extraTokens);
			ReductionStats reductionStats = reductionUsed? new ReductionStats(removedTransitions, removedPlaces) : null;

            var qr = new QueryResult(result, boundedAnalysis, query, false);
            qr.setApproximationInconclusive(isInconclusive);
            qr.setSolvedUsingQuerySimplification(solvedUsingQuerySimplification);
            qr.setSolvedUsingTraceAbstractRefinement(solvedUsingTraceAbstractRefinement);
            qr.setSolvedUsingSiphonTrap(solvedUsingSiphonTrap);
			return new Tuple<>(qr, new Stats(discovered, explored, explored, transitionStats, placeBoundStats, reductionStats));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
