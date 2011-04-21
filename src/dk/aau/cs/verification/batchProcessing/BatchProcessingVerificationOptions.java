package dk.aau.cs.verification.batchProcessing;

import pipe.dataLayer.TAPNQuery.SearchOption;
import dk.aau.cs.translations.ReductionOption;

public class BatchProcessingVerificationOptions {
	private ReductionOption reductionOption;
	private SearchOption searchOption;
	
	public BatchProcessingVerificationOptions(SearchOption searchOption, ReductionOption reductionOption) {
		this.searchOption = searchOption;
		this.reductionOption = reductionOption;
	}
	
	public ReductionOption reductionOption() {
		return reductionOption;
	}
	
	public SearchOption searchOption() {
		return searchOption;
	}
}
