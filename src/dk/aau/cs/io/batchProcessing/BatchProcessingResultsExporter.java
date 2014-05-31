package dk.aau.cs.io.batchProcessing;

import java.io.File;
import java.io.PrintStream;

import pipe.dataLayer.TAPNQuery;
import pipe.dataLayer.TAPNQuery.SearchOption;
import dk.aau.cs.translations.ReductionOption;
import dk.aau.cs.verification.batchProcessing.BatchProcessingVerificationResult;

public class BatchProcessingResultsExporter {
	private static final String name_verifyTAPN = "A: TAPAAL Continuous Engine";
	private static final String name_verifyTAPN_discreteInclusion = "B: TAPAAL Continuous Engine w. Discrete Inclusion";
	private static final String name_verifyTAPNDiscreteVerificationTimeDartPTrie = "C: TAPAAL Discrete Engine w. Time Darts and PTrie";
	private static final String name_verifyTAPNDiscreteVerificationTimeDart = "D: TAPAAL Discrete Engine w. Time Darts";
	private static final String name_verifyTAPNDiscreteVerificationPTrie = "E: TAPAAL Discrete Engine Engine w. PTries";
	private static final String name_verifyTAPNDiscreteVerificationNone = "F: TAPAAL Discrete Engine w. no Optimizations";
	private static final String name_COMBI = "G: UPPAAL: Optimized Broadcast Reduction";
	private static final String name_STANDARD = "H: UPPAAL: Standard Reduction";
	private static final String name_OPTIMIZEDSTANDARD = "I: UPPAAL: Optimised Standard Reduction";
	private static final String name_BROADCAST = "J: UPPAAL: Broadcast Reduction";
	private static final String name_BROADCASTDEG2 = "K: UPPAAL: Broadcast Degree 2 Reduction";
	private static final String name_UNTIMED = "L: TAPAAL Untimed Engine";
	private static final String name_UNTIMEDAPPROX = "M: TAPAAL Untimed Engine, State-Equations Check Only";	
	private static final String name_UNTIMEDREDUCE = "N: TAPAAL Untimed engine w. Net Reductions";
	private static final String name_BFS = "Breadth First Search";
	private static final String name_DFS = "Depth First Search";
	private static final String name_RandomDFS = "Random Depth First Search";
	private static final String name_NONE_APPROXIMATION = "None";
	private static final String name_OVER_APPROXIMATION = "Over-approximation";
	private static final String name_UNDER_APPROXIMATION = "Under-approximation";
	private static final String DELIMITER = ";";	
	
	public void exportToCSV(Iterable<BatchProcessingVerificationResult> results, File outputFile) throws Exception {
		PrintStream writer = new PrintStream(outputFile);
		
		writer.println("Model" + DELIMITER + 
				       "Query" + DELIMITER + 
				       "Result" + DELIMITER + 
				       "Verification Time" + DELIMITER +
				       "Memory Usage" + DELIMITER +
				       "Discovered States" + DELIMITER + 
				       "Explored States" + DELIMITER + 
				       "Stored States" + DELIMITER + 
				       "Query Property" + DELIMITER + 
				       "Extra Tokens" + DELIMITER + 
				       "Verification Method" + DELIMITER + 
				       "Symmetry" + DELIMITER + 
				       "Search Order" + DELIMITER + 
				       "Approximation Method" + DELIMITER +
				       "Approximation Constant");
		
		for(BatchProcessingVerificationResult result : results) {
			TAPNQuery query = result.query();
			
			StringBuilder s = new StringBuilder();
			
			
			
			s.append(result.modelFile());
			s.append(DELIMITER);
			s.append((query != null) ? query.getName() : "");
			s.append(DELIMITER);
			s.append(result.verificationResult());
			s.append(DELIMITER);
			s.append((result.verificationTimeInMs() / 1000.0) + " s");
			s.append(DELIMITER);
			s.append(result.verificationMemory());
			s.append(DELIMITER);
			s.append(result.hasStats() ? result.stats().discoveredStates() : "");
			s.append(DELIMITER);
			s.append(result.hasStats() ? result.stats().exploredStates() : "");
			s.append(DELIMITER);
			s.append(result.hasStats() ? result.stats().storedStates() : "");
			s.append(DELIMITER);
			s.append((query != null) ? query.getProperty().toString() : "");
			s.append(DELIMITER);
			s.append((query != null) ? query.getCapacity() : "");
			s.append(DELIMITER);
			s.append((query != null) ? getSearchOrder(query) : "");
			s.append(DELIMITER);
			s.append((query != null) ? (query.useSymmetry() ? "Yes" : "No") : "");
			s.append(DELIMITER);
			s.append((query != null) ? getVerificationMethod(query) : "");
			s.append(DELIMITER);
			s.append((query != null) ? getApproximationMethod(query) : "");
			s.append(DELIMITER);
			s.append((query != null) ? query.approximationDenominator() : "");
			
			writer.println(s.toString());
		}
	}

	private String getVerificationMethod(TAPNQuery query) {
		SearchOption search = query.getSearchOption();
		
		if(search == SearchOption.DFS)
			return name_DFS;
		else if(search == SearchOption.RANDOM)
			return name_RandomDFS;
		else 
			return name_BFS;
		
	}
	
	private String getApproximationMethod(TAPNQuery query) {
		if (query.isOverApproximationEnabled()) {
			return name_OVER_APPROXIMATION;
		} else if (query.isUnderApproximationEnabled()) {
			return name_UNDER_APPROXIMATION;
		}
		return name_NONE_APPROXIMATION;
	}

	private Object getSearchOrder(TAPNQuery query) {
		ReductionOption reduction = query.getReductionOption();
		
		if(reduction == ReductionOption.COMBI)
			return name_COMBI;
		else if(reduction == ReductionOption.STANDARD)
			return name_STANDARD;
		else if(reduction == ReductionOption.OPTIMIZEDSTANDARD)
			return name_OPTIMIZEDSTANDARD;
		else if(reduction == ReductionOption.DEGREE2BROADCAST)
			return name_BROADCASTDEG2;
		else if(reduction == ReductionOption.VerifyTAPN){
			if(query.discreteInclusion()){
				return name_verifyTAPN_discreteInclusion;
			}else
				return name_verifyTAPN;
		}else if(reduction == ReductionOption.VerifyTAPNdiscreteVerification){
			if(query.useTimeDarts() && query.usePTrie()){
				return name_verifyTAPNDiscreteVerificationTimeDartPTrie;
			} else if(query.useTimeDarts()){
				return name_verifyTAPNDiscreteVerificationTimeDart;
			} else if(query.usePTrie()){
				return name_verifyTAPNDiscreteVerificationPTrie;
			} else {
				return name_verifyTAPNDiscreteVerificationNone;
			}
		}else if(reduction == ReductionOption.VerifyPN){
			return name_UNTIMED;
		}else if(reduction == ReductionOption.VerifyPNApprox){
			return name_UNTIMEDAPPROX;
		}else if(reduction == ReductionOption.VerifyPNReduce){
			return name_UNTIMEDREDUCE;
		}
			return name_BROADCAST;
	}
}
