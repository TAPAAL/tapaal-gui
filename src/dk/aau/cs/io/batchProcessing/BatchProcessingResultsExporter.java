package dk.aau.cs.io.batchProcessing;

import java.io.File;
import java.io.PrintStream;

import pipe.dataLayer.TAPNQuery;
import pipe.dataLayer.TAPNQuery.SearchOption;
import dk.aau.cs.translations.ReductionOption;
import dk.aau.cs.verification.batchProcessing.BatchProcessingVerificationResult;

public class BatchProcessingResultsExporter {
	private static final String name_verifyTAPN = "A: TAPAAL Engine";
	private static final String name_verifyTAPN_discreteInclusion = "B: TAPAAL Engine w. Discrete Inclusion";
	private static final String name_verifyTAPNDiscreteVerificationTimeDartPTrie = "C: TAPAAL Engine - Discrete Verification, Time darts and PTrie";
	private static final String name_verifyTAPNDiscreteVerificationTimeDart = "D: TAPAAL Engine - Discrete Verification, Time darts";
	private static final String name_verifyTAPNDiscreteVerificationPTrie = "E: TAPAAL Engine - Discrete Verification, PTries";
	private static final String name_verifyTAPNDiscreteVerificationNone = "F: TAPAAL Engine - Discrete Verification, No optimizations";
	private static final String name_STANDARD = "G: UPPAAL: Standard Reduction";
	private static final String name_OPTIMIZEDSTANDARD = "H: UPPAAL: Optimised Standard Reduction";
	private static final String name_BROADCAST = "I: UPPAAL: Broadcast Reduction";
	private static final String name_BROADCASTDEG2 = "J: UPPAAL: Broadcast Degree 2 Reduction";
	private static final String name_UNTIMED = "K: VerifyPN: Untimed engine";
	private static final String name_UNTIMEDAPPROX = "L: VerifyPN: Untimed engine, with over-approximation";	
	private static final String name_BFS = "Breadth First Search";
	private static final String name_DFS = "Depth First Search";
	private static final String name_RandomDFS = "Random Depth First Search";
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
				       "Search Order");
		
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

	private Object getSearchOrder(TAPNQuery query) {
		ReductionOption reduction = query.getReductionOption();
		
		if(reduction == ReductionOption.STANDARD)
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
		}
			return name_BROADCAST;
	}
}
