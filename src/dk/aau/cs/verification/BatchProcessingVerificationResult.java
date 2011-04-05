package dk.aau.cs.verification;

import pipe.dataLayer.TAPNQuery;
import dk.aau.cs.model.tapn.simulation.TimedArcPetriNetTrace;

public class BatchProcessingVerificationResult extends VerificationResult<TimedArcPetriNetTrace> {
	private String fileName;
	private TAPNQuery query;
	
	public BatchProcessingVerificationResult(String filename, TAPNQuery query, QueryResult queryResult, TimedArcPetriNetTrace trace, long verificationTime) {
		super(queryResult, trace, verificationTime);
		this.fileName = filename;
		this.query = query;
	}
	
	public String modelFileName() {
		return fileName;
	}
	
	public String queryName() {
		return query.getName();
	}
	
	
}
