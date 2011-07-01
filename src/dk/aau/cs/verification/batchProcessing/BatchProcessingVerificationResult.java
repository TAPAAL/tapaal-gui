package dk.aau.cs.verification.batchProcessing;

import pipe.dataLayer.TAPNQuery;
import dk.aau.cs.verification.NullStats;
import dk.aau.cs.verification.Stats;

public class BatchProcessingVerificationResult {
	private String file;
	private TAPNQuery query;
	private long verificationTimeInMs;
	private String verificationResult;
	private Stats stats;
	
	public BatchProcessingVerificationResult(String file, TAPNQuery query, String verificationResult, long verificationTime, Stats stats) {
		this.file = file;
		this.query = query;
		this.verificationResult = verificationResult;
		this.verificationTimeInMs = verificationTime;
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
	
	public Stats stats() {
		return stats;
	}


	public boolean hasStats() {
		if(stats instanceof NullStats)
			return false;
		else
			return true;
	}
	
	
}
