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
	private static final Pattern ruleAApplicationPattern = Pattern.compile("\\s*Applications of rule A:\\s*(\\d+)\\s*");
	private static final Pattern ruleBApplicationPattern = Pattern.compile("\\s*Applications of rule B:\\s*(\\d+)\\s*");
	private static final Pattern ruleCApplicationPattern = Pattern.compile("\\s*Applications of rule C:\\s*(\\d+)\\s*");
	private static final Pattern ruleDApplicationPattern = Pattern.compile("\\s*Applications of rule D:\\s*(\\d+)\\s*");
	private static final Pattern ruleEApplicationPattern = Pattern.compile("\\s*Applications of rule E:\\s*(\\d+)\\s*");
	private static final Pattern ruleFApplicationPattern = Pattern.compile("\\s*Applications of rule F:\\s*(\\d+)\\s*");
	private static final Pattern ruleGApplicationPattern = Pattern.compile("\\s*Applications of rule G:\\s*(\\d+)\\s*");
	private static final Pattern ruleHApplicationPattern = Pattern.compile("\\s*Applications of rule H:\\s*(\\d+)\\s*");
	private static final Pattern ruleIApplicationPattern = Pattern.compile("\\s*Applications of rule I:\\s*(\\d+)\\s*");

	
	
	public VerifyPNOutputParser(int totalTokens, int extraTokens, TAPNQuery queryType) {
		super(totalTokens, extraTokens, queryType);
	}
	
	public Tuple<QueryResult, Stats> parseOutput(String output) {
		int discovered = 0;
		int explored = 0;
		int maxUsedTokens = 0;
		int removedTransitions = 0;
		int removedPlaces = 0;
		int ruleA = 0;
		int ruleB = 0;
		int ruleC = 0;
		int ruleD = 0;
		int ruleE = 0;
		int ruleF = 0;
		int ruleG = 0;
		int ruleH = 0;
		int ruleI = 0;

		boolean reductionUsed = false;
		boolean result = false;
		boolean foundResult = false;
		String[] lines = output.split(System.getProperty("line.separator"));
            try {
                Matcher matcher = transitionStatsPattern.matcher(output);
                while (matcher.find()) {
                    transitionStats.add(new Tuple<String, Integer>(matcher.group(1), Integer.parseInt(matcher.group(2))));
                }

                matcher = transitionStatsPatternUnknown.matcher(output);
                while (matcher.find()) {
                    transitionStats.add(new Tuple<String, Integer>(matcher.group(1), -1));
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
					
					matcher = maxUsedTokensPattern.matcher(line);
					if(matcher.find()){
						maxUsedTokens = Integer.valueOf(matcher.group(1));
						String operator = matcher.group(1) == null ? "" : matcher.group(1);
						if(operator.equals(">")) maxUsedTokens += 1; // Indicate non-k-boundedness by encoding that an extra token was used.
					}
					
					matcher = reductionsUsedPattern.matcher(line);
					if(matcher.find()){
						reductionUsed = true;
					}
					
					matcher = removedTransitionsPattern.matcher(line);
					if(matcher.find()){
						removedTransitions = Integer.valueOf(matcher.group(1));
					}
					
					matcher = removedPlacesPattern.matcher(line);
					if(matcher.find()){
						removedPlaces = Integer.valueOf(matcher.group(1));
					}
					
					matcher = ruleAApplicationPattern.matcher(line);
					if(matcher.find()){
						ruleA = Integer.valueOf(matcher.group(1));
					}
					
					matcher = ruleBApplicationPattern.matcher(line);
					if(matcher.find()){
						ruleB = Integer.valueOf(matcher.group(1));
					}
					
					matcher = ruleCApplicationPattern.matcher(line);
					if(matcher.find()){
						ruleC = Integer.valueOf(matcher.group(1));
					}
					
					matcher = ruleDApplicationPattern.matcher(line);
					if(matcher.find()){
						ruleD = Integer.valueOf(matcher.group(1));
					}
					
					matcher = ruleEApplicationPattern.matcher(line);
					if(matcher.find()){
						ruleE = Integer.valueOf(matcher.group(1));
					}
					
					matcher = ruleFApplicationPattern.matcher(line);
					if(matcher.find()){
						ruleF = Integer.valueOf(matcher.group(1));
					}
					
					matcher = ruleGApplicationPattern.matcher(line);
					if(matcher.find()){
						ruleG = Integer.valueOf(matcher.group(1));
					}
					
					matcher = ruleHApplicationPattern.matcher(line);
					if(matcher.find()){
						ruleH = Integer.valueOf(matcher.group(1));
					}
					
					matcher = ruleIApplicationPattern.matcher(line);
					if(matcher.find()){
						ruleI = Integer.valueOf(matcher.group(1));
					}
				}
			}
			
			if(!foundResult) return null;
			BoundednessAnalysisResult boundedAnalysis = new BoundednessAnalysisResult(totalTokens, maxUsedTokens, extraTokens);
			ReductionStats reductionStats = reductionUsed? new ReductionStats(removedTransitions, removedPlaces, ruleA, ruleB, ruleC, ruleD, ruleE, ruleF, ruleG, ruleH, ruleI) : null;
			Tuple<QueryResult, Stats> value = new Tuple<QueryResult, Stats>(new QueryResult(result, boundedAnalysis, query, false), new Stats(discovered, explored, explored, transitionStats, placeBoundStats, reductionStats));
			return value; 
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
