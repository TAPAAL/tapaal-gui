package dk.aau.cs.verification.VerifyTAPN;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dk.aau.cs.model.tapn.TAPNQuery;
import dk.aau.cs.util.Tuple;
import dk.aau.cs.verification.*;
import pipe.gui.graph.GraphPoint;

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
    private static final Pattern smcTransitionStatsPattern = Pattern.compile("<([^:\\s]+):([\\d]+(\\.[\\d]+)?)>");
    private static final Pattern smcPlaceBoundPattern = Pattern.compile("<([^:\\s]+);([\\d]+(\\.[\\d]+)?)>");
    private static final Pattern smcPlaceBoundPatternUnknown = Pattern.compile("<([^;\\s]+);\\?>");

    private static final Pattern smcEstimationPattern = Pattern.compile("\\s*P = ([^±]+) ± (.+)\\s*");
    private static final Pattern smcExecutedRunsPattern = Pattern.compile("\\s*runs executed:\\s*(\\d+)\\s*");
    private static final Pattern smcValidRunsPattern = Pattern.compile("\\s*number of valid runs:\\s*(\\d+)\\s*");
    private static final Pattern smcAverageTimePattern = Pattern.compile("\\s*average run duration:\\s*([\\d\\.]+)\\s*");
    private static final Pattern smcAverageLengthPattern = Pattern.compile("\\s*average run length:\\s*([\\d\\.]+)\\s*");
    private static final Pattern smcTimeStdDevPattern = Pattern.compile("\\s*run duration \\(std. dev.\\):\\s*([\\d\\.]+)\\s*");
    private static final Pattern smcLengthStdDevPattern = Pattern.compile("\\s*run length \\(std. dev.\\):\\s*([\\d\\.]+)\\s*");
    private static final Pattern smcVerificationTimePattern = Pattern.compile("\\s*verification time:\\s*([\\d\\.]+)\\s*");

    private static final Pattern smcCumulativeProbabilityStepPattern = Pattern.compile("\\s*cumulative probability / step :");
    private static final Pattern smcCumulativeProbabilityDelayPattern = Pattern.compile("\\s*cumulative probability / delay :");
    private static final Map<String, Pattern> smcObservationPatterns = new LinkedHashMap<String, Pattern>() {{
        put("avgStep", Pattern.compile("\\((\\w+(?: \\w+)*)\\) avg/step"));
        put("minStep", Pattern.compile("\\((\\w+(?: \\w+)*)\\) min/step"));
        put("maxStep", Pattern.compile("\\((\\w+(?: \\w+)*)\\) max/step"));
        put("avgTime", Pattern.compile("\\((\\w+(?: \\w+)*)\\) avg/time"));
        put("minTime", Pattern.compile("\\((\\w+(?: \\w+)*)\\) min/time"));
        put("maxTime", Pattern.compile("\\((\\w+(?: \\w+)*)\\) max/time"));
        put("valueStep", Pattern.compile("\\((\\w+(?: \\w+)*)\\) value/step"));
        put("valueTime", Pattern.compile("\\((\\w+(?: \\w+)*)\\) value/time"));
        put("globalAvgStep", Pattern.compile("\\((\\w+(?: \\w+)*)\\) Global steps avg.: ([0-9]*[.]?[0-9]+(?:[eE][-+]?[0-9]+)?)"));
        put("globalAvgTime", Pattern.compile("\\((\\w+(?: \\w+)*)\\) Global time avg.: ([0-9]*[.]?[0-9]+(?:[eE][-+]?[0-9]+)?)"));
    }};

    private static final Pattern smcAverageValidTimePattern = Pattern.compile("\\s*duration of a valid run \\(average\\):\\s*([\\d\\.]+)\\s*");
    private static final Pattern smcAverageValidLengthPattern = Pattern.compile("\\s*length of a valid run \\(average\\):\\s*([\\d\\.]+)\\s*");
    private static final Pattern smcValidTimeStdDevPattern = Pattern.compile("\\s*duration of a valid run \\(std. dev.\\):\\s*([\\d\\.]+)\\s*");
    private static final Pattern smcValidLengthStdDevPattern = Pattern.compile("\\s*length of a valid run \\(std. dev.\\):\\s*([\\d\\.]+)\\s*");
    private static final Pattern smcAverageViolatingTimePattern = Pattern.compile("\\s*duration of a violating run \\(average\\):\\s*([\\d\\.]+)\\s*");
    private static final Pattern smcAverageViolatingLengthPattern = Pattern.compile("\\s*length of a violating run \\(average\\):\\s*([\\d\\.]+)\\s*");
    private static final Pattern smcViolatingTimeStdDevPattern = Pattern.compile("\\s*duration of a violating run \\(std. dev.\\):\\s*([\\d\\.]+)\\s*");
    private static final Pattern smcViolatingLengthStdDevPattern = Pattern.compile("\\s*length of a violating run \\(std. dev.\\):\\s*([\\d\\.]+)\\s*");

    private static final Pattern smcNumberOfTracesPattern = Pattern.compile("\\s*Generated \\d+ random traces");

	private static final Pattern wfMinExecutionPattern = Pattern.compile("Minimum execution time: (-?\\d*)");
	private static final Pattern wfMaxExecutionPattern = Pattern.compile("Maximum execution time: (-?\\d*)");
	private static final Pattern wfCoveredMarkingPattern = Pattern.compile("Covered marking: (.*)");
	private final int totalTokens;
	private final TAPNQuery query;
	private final int extraTokens;
    private final List<Tuple<String,Number>> transitionStats = new ArrayList<Tuple<String,Number>>();
    private final List<Tuple<String,Number>> placeBoundStats = new ArrayList<Tuple<String,Number>>();

    private final Set<String> uniqueMatches = new HashSet<>();

    private int allMatches;

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
        int smcValidRuns = 0;
        float smcAverageTime = -1.0f;
        float smcAverageLength = -1.0f;
        float smcTimeStdDev = -1.0f;
        float smcLengthStdDev = -1.0f;
        float smcVerificationTime = -1.0f;
        float smcAverageValidTime = -1.0f;
        float smcAverageValidLength = -1.0f;
        float smcValidTimeStdDev = -1.0f;
        float smcValidLengthStdDev = -1.0f;
        float smcAverageViolatingTime = -1.0f;
        float smcAverageViolatingLength = -1.0f;
        float smcViolatingTimeStdDev = -1.0f;
        float smcViolatingLengthStdDev = -1.0f;
		ArrayList<Tuple<String, Tuple<BigDecimal, Integer>>> coveredMarking = null;
		boolean result = false;
		int maxUsedTokens = 0;
		boolean foundResult = false;
		boolean discreteInclusion = false;
        boolean isSmc = false;
        boolean isQuantitative = false;
        QuantitativeResult quantitativeResult = null;
		String[] lines = output.split(System.getProperty("line.separator"));

		List<GraphPoint> cumulativeStepPoints = new ArrayList<>();
		List<GraphPoint> cumulativeDelayPoints = new ArrayList<>();
        Map<String, ObservationData> observationDataMap = new HashMap<>();

        if (output.contains(SMC_Verification_INDICATOR_STRING)) {
            isSmc = true;
        }

		try {
            Matcher matcher;

            if (isSmc) {
                matcher = smcTransitionStatsPattern.matcher(output);
                while (matcher.find()) {
                    transitionStats.add(new Tuple<String,Number>(matcher.group(1), Float.parseFloat(matcher.group(2))));
                }

                matcher = smcPlaceBoundPattern.matcher(output);
                while (matcher.find()) {
                    placeBoundStats.add(new Tuple<String,Number>(matcher.group(1), Float.parseFloat(matcher.group(2))));
                }

                matcher = smcPlaceBoundPatternUnknown.matcher(output);
                while (matcher.find()) {
                    placeBoundStats.add(new Tuple<String,Number>(matcher.group(1), -1));
                }
            } else {
                matcher = transitionStatsPattern.matcher(output);
                while (matcher.find()) {
                    transitionStats.add(new Tuple<String,Number>(matcher.group(1), Integer.parseInt(matcher.group(2))));
                }
                matcher = placeBoundPattern.matcher(output);
                while (matcher.find()) {
                    placeBoundStats.add(new Tuple<String,Number>(matcher.group(1), Integer.parseInt(matcher.group(2))));
                }
                matcher = placeBoundPatternUnknown.matcher(output);
                while (matcher.find()) {
                    placeBoundStats.add(new Tuple<String,Number>(matcher.group(1), -1));
                }
            }

			for (int i = 0; i < lines.length; i++) {
				String line = lines[i];
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
                    matcher = smcTimeStdDevPattern.matcher(line);
                    if(matcher.find()) {
                        smcTimeStdDev = Float.parseFloat(matcher.group(1));
                    }
                    matcher = smcLengthStdDevPattern.matcher(line);
                    if(matcher.find()) {
                        smcLengthStdDev = Float.parseFloat(matcher.group(1));
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
                    matcher = smcAverageViolatingTimePattern.matcher(line);
                    if(matcher.find()) {
                        smcAverageViolatingTime = Float.parseFloat(matcher.group(1));
                    }
                    matcher = smcAverageViolatingLengthPattern.matcher(line);
                    if(matcher.find()) {
                        smcAverageViolatingLength = Float.parseFloat(matcher.group(1));
                    }
                    matcher = smcViolatingTimeStdDevPattern.matcher(line);
                    if(matcher.find()) {
                        smcViolatingTimeStdDev = Float.parseFloat(matcher.group(1));
                    }
                    matcher = smcViolatingLengthStdDevPattern.matcher(line);
                    if(matcher.find()) {
                        smcViolatingLengthStdDev = Float.parseFloat(matcher.group(1));
                    }

                    matcher = smcCumulativeProbabilityStepPattern.matcher(line);
                    if (matcher.find() && i < lines.length - 1) {
                        line = lines[i + 1];
                        String[] pointStrs = line.split(";");
                        for (String pointStr : pointStrs) {
                            String[] coordinates = pointStr.split(":");
                            GraphPoint point = new GraphPoint(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1]));
                            cumulativeStepPoints.add(point);
                        }
                    }

                    matcher = smcCumulativeProbabilityDelayPattern.matcher(line);
                    if (matcher.find() && i < lines.length - 1) {
                        line = lines[i + 1];
                        String[] pointStrs = line.split(";");
                        for (String pointStr : pointStrs) {
                            String[] coordinates = pointStr.split(":");
                            GraphPoint point = new GraphPoint(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1]));
                            cumulativeDelayPoints.add(point);
                        }
                    }

                    extractObservations(line, i, lines, observationDataMap);

                    matcher = smcNumberOfTracesPattern.matcher(line);
                    if (matcher.find()) {
                        foundResult = true;
                        result = true;
                    }
				}
			}
            
			if(!foundResult) return null;
			
			BoundednessAnalysisResult boundedAnalysis = new BoundednessAnalysisResult(totalTokens, maxUsedTokens, extraTokens);
            Stats verifStats;
            QueryResult queryRes;

            if(isSmc) {
                verifStats = new SMCStats(smcExecutedRuns, smcValidRuns, smcAverageTime, smcAverageLength, smcVerificationTime, cumulativeStepPoints, cumulativeDelayPoints, observationDataMap, transitionStats, placeBoundStats);
                ((SMCStats) verifStats).setRunTimeStdDev(smcTimeStdDev);
                ((SMCStats) verifStats).setRunLengthStdDev(smcLengthStdDev);
                ((SMCStats) verifStats).setValidRunAverageTime(smcAverageValidTime);
                ((SMCStats) verifStats).setValidRunAverageLength(smcAverageValidLength);
                ((SMCStats) verifStats).setValidRunTimeStdDev(smcValidTimeStdDev);
                ((SMCStats) verifStats).setValidRunLengthStdDev(smcValidLengthStdDev);
                ((SMCStats) verifStats).setViolatingRunAverageTime(smcAverageViolatingTime);
                ((SMCStats) verifStats).setViolatingRunAverageLength(smcAverageViolatingLength);
                ((SMCStats) verifStats).setViolatingRunTimeStdDev(smcViolatingTimeStdDev);
                ((SMCStats) verifStats).setViolatingRunLengthStdDev(smcViolatingLengthStdDev);
            } else
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

    private void extractObservations(String line, int i, String[] lines, Map<String, ObservationData> observationDataMap) {
        String startLine;
        if (query.isSimulate()) {
            startLine = line;
            for (Map.Entry<String, Pattern> entry : smcObservationPatterns.entrySet()) {
                Pattern pattern = entry.getValue();
                Matcher matcher = pattern.matcher(line);
                if (matcher.find() && i < lines.length - 1) {
                    uniqueMatches.add(matcher.group(0));
                }
            }

            line = startLine;
        }

        for (Map.Entry<String, Pattern> entry : smcObservationPatterns.entrySet()) {
            Pattern pattern = entry.getValue();
            Matcher matcher = pattern.matcher(line);
            String key = entry.getKey();
            if (key.contains("global") && matcher.find()) {
                String observationName = matcher.group(1);
                double value = Double.parseDouble(matcher.group(2));
                ObservationData observationData;
                if (observationDataMap.containsKey(observationName)) {
                    observationData = observationDataMap.get(observationName);
                } else {
                    observationData = new ObservationData();
                }

                switch (key) {
                    case "globalAvgStep":
                        observationData.setSmcGlobalAvgStep(value);
                        break;
                    case "globalAvgTime":
                        observationData.setSmcGlobalAvgTime(value);
                        break;
                }
            } else if (matcher.find() && i < lines.length - 1) {
                line = lines[i + 1];
                String[] pointStrs = line.split(";");
                List<GraphPoint> points = new ArrayList<>();
                for (String pointStr : pointStrs) {
                    String[] coordinates = pointStr.split(":");
                    GraphPoint point = new GraphPoint(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1]));
                    points.add(point);
                }

                String observationName = matcher.group(1);
                if (query.isSimulate()) {
                    observationName += " (" + ((allMatches++ + uniqueMatches.size()) / uniqueMatches.size()) + ")";
                }

                ObservationData observationData;
                if (observationDataMap.containsKey(observationName)) {
                    observationData = observationDataMap.get(observationName);
                } else {
                    observationData = new ObservationData();
                }

                observationData.setObservationData(points, key);
                if (!observationDataMap.containsKey(observationName)) {
                    observationDataMap.put(observationName, observationData);
                }
            }
        }
    }
}
