package dk.aau.cs.verification.batchProcessing;

import pipe.dataLayer.TAPNQuery;
import dk.aau.cs.verification.NullStats;
import dk.aau.cs.verification.Stats;

public class BatchProcessingVerificationResult {
	private String file;
	private TAPNQuery query;
	private long verificationTimeInMs;
	private String verificationMemory;
	private String verificationResult;
	private Stats stats;
	
	public BatchProcessingVerificationResult(String file, TAPNQuery query, String verificationResult, long verificationTime, String verificationMemory, Stats stats) {
		this.file = file;
		this.query = query;
		this.verificationResult = verificationResult;
		verificationTimeInMs = verificationTime;
		this.verificationMemory = verificationMemory;
		this.stats = stats;
	}
	
	
	public String modelFile() {
		return file;
	}
	
	public String queryName() {
		return query != null ? query.getName() : "";
	}
	
	public TAPNQuery query() {
		return query;
	}
	
	public String verificationResult() {
		return verificationResult;
	}
	
	public long verificationTimeInMs() {
		return verificationTimeInMs;
	}
	
	public String verificationMemory() {
		return verificationMemory;
	}
	
	public Stats stats() {
		return stats;
	}


	public boolean hasStats() {
		return !(stats instanceof NullStats);
	}
	
	
}
