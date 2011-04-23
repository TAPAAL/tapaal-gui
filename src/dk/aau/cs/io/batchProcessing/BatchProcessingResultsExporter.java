package dk.aau.cs.io.batchProcessing;

import java.io.File;
import java.io.PrintStream;

import dk.aau.cs.verification.batchProcessing.BatchProcessingVerificationResult;

public class BatchProcessingResultsExporter {
	public void exportToCSV(Iterable<BatchProcessingVerificationResult> results, File outputFile) throws Exception {
		PrintStream writer = new PrintStream(outputFile);
		
		writer.println("Model, Query, Result, Verification Time");
		
		for(BatchProcessingVerificationResult result : results) {
			writer.print(result.modelFile());
			writer.print(",");
			writer.print(result.queryName());
			writer.print(",");
			writer.print(result.verificationResult());
			writer.print(",");
			writer.print((result.verificationTimeInMs() / 1000.0) + " s\n");
		}
	}
}
