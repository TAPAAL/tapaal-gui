package dk.aau.cs.verification.batchProcessing;

import dk.aau.cs.translations.ReductionOption;

public class BatchProcessingVerificationOptions {
    private final int number;
    private final String options;
    private final boolean keepKBound;
    private final ReductionOption engine;

	
	public BatchProcessingVerificationOptions(int number, String options, boolean keepKBound, ReductionOption engine) {
		this.number = number;
		this.options = options;
		this.keepKBound = keepKBound;
		this.engine = engine;
	}

    public int getNumber() {
        return number;
    }

    public String getOptions() {
        return options;
    }

    public boolean keepKBound() {
	    return keepKBound;
    }

	public ReductionOption getEngine() {
	    return engine;
    }
}
