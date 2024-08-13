package dk.aau.cs.verification.VerifyTAPN;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dk.aau.cs.TCTL.LTLFNode;
import dk.aau.cs.model.tapn.TAPNQuery;
import dk.aau.cs.util.Tuple;
import dk.aau.cs.verification.*;

public class VerifyDTAPNOutputParser {
	private static final String Query_IS_NOT_SATISFIED_STRING = "Query is NOT satisfied";
	private static final String Query_IS_SATISFIED_STRING = "Query is satisfied";
	private static final String DISCRETE_INCLUSION = "discrete inclusion";
    private static final String SMC_Verification_INDICATOR_STRING = "SMC Verification";
    private static final String SMC_Hypothesis_IS_SATISFIED_STRING = "Hypothesis is satisfied";
    private static final String SMC_Hypothesis_IS_NOT_SATISFIED_STRING = "Hypothesis is NOT satisfied";

	private static final Pattern discoveredPattern = Pattern.compile("\\s*discovered markings:\\s*(\\d+)\\s*");
	private static final Pattern exploredPattern = Pattern.compile("\\s*explored markings:\\s*(\\d+)\\s*");
	private static final Pattern storedPattern = Pattern.compile("\\s*stored markings:\\s*(\\d+)\\s*");
	private static final Pattern maxUsedTokensPattern = Pattern.compile("\\s*Max number of tokens found in any reachable marking:\\s*(>)?(\\d+)\\s*");
	private static final Pattern transitionStatsPattern = Pattern.compile("<([^:\\s]+):(\\d+)>");
	private static final Pattern placeBoundPattern = Pattern.compile("<([^;\\s]+);(\\d+)>");
    private static final Pattern placeBoundPatternUnknown = Pattern.compile("<([^;\\s]+);\\?>");

    private static final Pattern smcEstimationPattern = Pattern.compile("\\s*P = ([^±]+) ± (.+)\\s*");
    private static final Pattern smcExecutedRunsPattern = Pattern.compile("\\s*runs executed:\\s*(\\d+)\\s*");
    private static final Pattern smcValidRunsPattern = Pattern.compile("\\s*valid runs:\\s*(\\d+)\\s*");
    private static final Pattern smcAverageTimePattern = Pattern.compile("\\s*average run time:\\s*([\\d\\.]+)\\s*");
    private static final Pattern smcAverageLengthPattern = Pattern.compile("\\s*average run length:\\s*([\\d\\.]+)\\s*");
    private static final Pattern smcVerificationTimePattern = Pattern.compile("\\s*verification time:\\s*([\\d\\.]+)\\s*");
    private static final Pattern smcAverageValidTimePattern = Pattern.compile("\\s*average time of a valid run:\\s*([\\d\\.]+)\\s*");
    private static final Pattern smcAverageValidLengthPattern = Pattern.compile("\\s*average length of a valid run:\\s*([\\d\\.]+)\\s*");
    private static final Pattern smcValidTimeStdDevPattern = Pattern.compile("\\s*valid runs time standard deviation:\\s*([\\d\\.]+)\\s*");
    private static final Pattern smcValidLengthStdDevPattern = Pattern.compile("\\s*valid runs length standard deviation:\\s*([\\d\\.]+)\\s*");

	private static final Pattern wfMinExecutionPattern = Pattern.compile("Minimum execution time: (-?\\d*)");
	private static final Pattern wfMaxExecutionPattern = Pattern.compile("Maximum execution time: (-?\\d*)");
	private static final Pattern wfCoveredMarkingPattern = Pattern.compile("Covered marking: (.*)");
	private final int totalTokens;
	private final TAPNQuery query;
	private final int extraTokens;
	private final List<Tuple<String,Integer>> transitionStats = new ArrayList<Tuple<String,Integer>>();
	private final List<Tuple<String,Integer>> placeBoundStats = new ArrayList<Tuple<String,Integer>>();
        
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
        int smcExecutedRuns = -1;
        int smcValidRuns = -1;
        float smcAverageTime = -1.0f;
        float smcAverageLength = -1.0f;
        float smcVerificationTime = -1.0f;
        float smcAverageValidTime = -1.0f;
        float smcAverageValidLength = -1.0f;
        float smcValidTimeStdDev = -1.0f;
        float smcValidLengthStdDev = -1.0f;
		ArrayList<Tuple<String, Tuple<BigDecimal, Integer>>> coveredMarking = null;
		boolean result = false;
		int maxUsedTokens = 0;
		boolean foundResult = false;
		boolean discreteInclusion = false;
        boolean isSmc = false;
        boolean isQuantitative = false;
        QuantitativeResult quantitativeResult = null;
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
                if (line.contains(SMC_Verification_INDICATOR_STRING)) isSmc = true;
				if (line.contains(DISCRETE_INCLUSION)) { discreteInclusion = true; }
				if (line.contains(Query_IS_SATISFIED_STRING) || line.contains(SMC_Hypothesis_IS_SATISFIED_STRING)) {
					result = true;
					foundResult = true;
				} else if (line.contains(Query_IS_NOT_SATISFIED_STRING) || line.contains(SMC_Hypothesis_IS_NOT_SATISFIED_STRING)) {
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
					
					matcher = wfMinExecutionPattern.matcher(line);
					if(matcher.find()){
						WFminExecutionTime = Integer.parseInt(matcher.group(1));
					}
					
					matcher = wfMaxExecutionPattern.matcher(line);
					if(matcher.find()){
						WFmaxExecutionTime = Integer.parseInt(matcher.group(1));
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

                    matcher = smcEstimationPattern.matcher(line);
                    if(matcher.find()) {
                        foundResult = true;
                        result = true;
                        isQuantitative = true;
                        float smcEstimation = Float.parseFloat(matcher.group(1));
                        float smcEstimationWidth = Float.parseFloat(matcher.group(2));
                        quantitativeResult = new QuantitativeResult(smcEstimation, smcEstimationWidth);
                    }

                    matcher = smcExecutedRunsPattern.matcher(line);
                    if(matcher.find()) {
                        smcExecutedRuns = Integer.parseInt(matcher.group(1));
                    }

                    matcher = smcValidRunsPattern.matcher(line);
                    if(matcher.find()) {
                        smcValidRuns = Integer.parseInt(matcher.group(1));
                    }

                    matcher = smcAverageTimePattern.matcher(line);
                    if(matcher.find()) {
                        smcAverageTime = Float.parseFloat(matcher.group(1));
                    }

                    matcher = smcAverageLengthPattern.matcher(line);
                    if(matcher.find()) {
                        smcAverageLength = Float.parseFloat(matcher.group(1));
                    }

                    matcher = smcVerificationTimePattern.matcher(line);
                    if(matcher.find()) {
                        smcVerificationTime = Float.parseFloat(matcher.group(1));
                    }

                    matcher = smcAverageValidTimePattern.matcher(line);
                    if(matcher.find()) {
                        smcAverageValidTime = Float.parseFloat(matcher.group(1));
                    }

                    matcher = smcAverageValidLengthPattern.matcher(line);
                    if(matcher.find()) {
                        smcAverageValidLength = Float.parseFloat(matcher.group(1));
                    }

                    matcher = smcValidTimeStdDevPattern.matcher(line);
                    if(matcher.find()) {
                        smcValidTimeStdDev = Float.parseFloat(matcher.group(1));
                    }

                    matcher = smcValidLengthStdDevPattern.matcher(line);
                    if(matcher.find()) {
                        smcValidLengthStdDev = Float.parseFloat(matcher.group(1));
                    }

				}
			}
			
			if(!foundResult) return null;
			
			BoundednessAnalysisResult boundedAnalysis = new BoundednessAnalysisResult(totalTokens, maxUsedTokens, extraTokens);
            Stats verifStats;
            QueryResult queryRes;
            if(isSmc && isQuantitative) {
                verifStats = new SMCStats(smcExecutedRuns, smcValidRuns, smcAverageTime, smcAverageLength, smcVerificationTime);
                ((SMCStats) verifStats).setAverageValidRunTime(smcAverageValidTime);
                ((SMCStats) verifStats).setAverageValidRunLength(smcAverageValidLength);
                ((SMCStats) verifStats).setValidRunTimeStdDev(smcValidTimeStdDev);
                ((SMCStats) verifStats).setValidRunLengthStdDev(smcValidLengthStdDev);
            }
            else if(isSmc)
                verifStats = new SMCStats(smcExecutedRuns, smcValidRuns, smcAverageTime, smcAverageLength, smcVerificationTime);
            else
                verifStats = new Stats(discovered, explored, stored, transitionStats, placeBoundStats, WFminExecutionTime, WFmaxExecutionTime, coveredMarking);
            if(isQuantitative) {
                queryRes = new QueryResult(quantitativeResult, boundedAnalysis, query, discreteInclusion);
            }
            else
                queryRes = new QueryResult(result, boundedAnalysis, query, discreteInclusion);
            return new Tuple<QueryResult, Stats>(queryRes, verifStats);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
