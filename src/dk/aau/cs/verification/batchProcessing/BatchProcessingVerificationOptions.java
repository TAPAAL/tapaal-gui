package dk.aau.cs.verification.batchProcessing;

import pipe.dataLayer.TAPNQuery.SearchOption;
import dk.aau.cs.translations.ReductionOption;

public class BatchProcessingVerificationOptions {
	
	public enum QueryPropertyOption {
		KeepQueryOption, SearchWholeStateSpace
	};
	
	private ReductionOption reductionOption;
	private SearchOption searchOption;
	
	private QueryPropertyOption queryPropertyOption;
	
	public BatchProcessingVerificationOptions(QueryPropertyOption queryPropertyOption, SearchOption searchOption, ReductionOption reductionOption) {
		this.searchOption = searchOption;
		this.reductionOption = reductionOption;
		this.queryPropertyOption = queryPropertyOption;
	}
	
	public ReductionOption reductionOption() {
		return reductionOption;
	}
	
	public SearchOption searchOption() {
		return searchOption;
	}
	
	public QueryPropertyOption queryPropertyOption() {
		return queryPropertyOption;
	}
}
