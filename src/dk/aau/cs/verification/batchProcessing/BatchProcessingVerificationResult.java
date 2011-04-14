package dk.aau.cs.verification.batchProcessing;

import pipe.dataLayer.TAPNQuery;
import dk.aau.cs.model.tapn.simulation.TimedArcPetriNetTrace;
import dk.aau.cs.verification.QueryResult;
import dk.aau.cs.verification.VerificationResult;

public class BatchProcessingVerificationResult extends VerificationResult<TimedArcPetriNetTrace> {
	private String file;
	private TAPNQuery query;
	
	public BatchProcessingVerificationResult(String file, TAPNQuery query, QueryResult queryResult, TimedArcPetriNetTrace trace, long verificationTime) {
		super(queryResult, trace, verificationTime);
		this.file = file;
		this.query = query;
	}
	
	public BatchProcessingVerificationResult(String file, TAPNQuery query, VerificationResult<TimedArcPetriNetTrace> result) {
		super(result.getQueryResult(), result.getTrace(), result.verificationTime());
		this.file = file;
		this.query = query;
	}
	
	
	public String modelFile() {
		return file;
	}
	
	public String queryName() {
		return query.getName();
	}
	
	
}
