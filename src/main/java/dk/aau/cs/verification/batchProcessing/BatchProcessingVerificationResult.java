package dk.aau.cs.verification.batchProcessing;

import net.tapaal.gui.petrinet.verification.TAPNQuery;
import dk.aau.cs.verification.NullStats;
import dk.aau.cs.verification.Stats;

public class BatchProcessingVerificationResult {
	private final String file;
	private final TAPNQuery query;
	private final long verificationTimeInMs;
	private final String verificationMemory;
	private final String verificationResult;
    private final Stats stats;
    private final int optionNumber;

    public BatchProcessingVerificationResult(String file, TAPNQuery query, String verificationResult, long verificationTime, String verificationMemory, Stats stats) {
        this(file, query, verificationResult, verificationTime, verificationMemory, stats, -1);
    }

	public BatchProcessingVerificationResult(String file, TAPNQuery query, String verificationResult, long verificationTime, String verificationMemory, Stats stats, int optionNumber) {
		this.file = file;
		this.query = query;
		this.verificationResult = verificationResult;
		verificationTimeInMs = verificationTime;
		this.verificationMemory = verificationMemory;
		this.stats = stats;
		this.optionNumber = optionNumber;
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

    public int getOptionNumber() {
        return optionNumber;
    }
}
