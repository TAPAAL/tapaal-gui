package dk.aau.cs.verification.batchProcessing;

import pipe.dataLayer.TAPNQuery.SearchOption;
import dk.aau.cs.translations.ReductionOption;

public class BatchProcessingVerificationOptions {
	
	public enum QueryPropertyOption {
		KeepQueryOption, SearchWholeStateSpace
	};
	
	public enum SymmetryOption {
		KeepQueryOption, Yes, No
	};
	
	private ReductionOption reductionOption;
	private SearchOption searchOption;
	private QueryPropertyOption queryPropertyOption;
	private SymmetryOption symmetryOption;
	private boolean keepQueryCapacity;
	private int capacity;
	
	public BatchProcessingVerificationOptions(QueryPropertyOption queryPropertyOption, boolean keepQueryCapacity, int capacity, SearchOption searchOption, SymmetryOption symmetryOption, ReductionOption reductionOption) {
		this.searchOption = searchOption;
		this.reductionOption = reductionOption;
		this.queryPropertyOption = queryPropertyOption;
		this.symmetryOption = symmetryOption;
		this.keepQueryCapacity = keepQueryCapacity;
		this.capacity = capacity;
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
	
	public SymmetryOption symmetry() {
		return symmetryOption;
	}
	
	public boolean KeepCapacityFromQuery() {
		return keepQueryCapacity;
	}
	
	public int capacity() {
		return capacity;
	}
}
