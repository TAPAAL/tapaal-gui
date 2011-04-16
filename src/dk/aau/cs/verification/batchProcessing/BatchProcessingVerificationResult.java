package dk.aau.cs.verification.batchProcessing;

import pipe.dataLayer.TAPNQuery;

public class BatchProcessingVerificationResult {
	private String file;
	private TAPNQuery query;
	private long verificationTimeInMs;
	private String verificationResult;
	
	public BatchProcessingVerificationResult(String file, TAPNQuery query, String verificationResult, long verificationTime) {
		this.file = file;
		this.query = query;
		this.verificationResult = verificationResult;
		this.verificationTimeInMs = verificationTime;
	}
	
	
	public String modelFile() {
		return file;
	}
	
	public String queryName() {
		return query != null ? query.getName() : "";
	}
	
	public String verificationResult() {
		return verificationResult;
	}
	
	public long verificationTimeInMs() {
		return verificationTimeInMs;
	}
	
	
}
