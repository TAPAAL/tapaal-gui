package dk.aau.cs.verification.batchProcessing;

import java.util.List;

import pipe.dataLayer.TAPNQuery.SearchOption;
import dk.aau.cs.translations.ReductionOption;
import dk.aau.cs.util.Require;

public class BatchProcessingVerificationOptions {
	
	public enum QueryPropertyOption {
		KeepQueryOption, SearchWholeStateSpace
	};
	
	public enum SymmetryOption {
		KeepQueryOption, Yes, No
	};
	
	public enum LocalConstantsOption{
		KeepQueryOption, Yes, No
	}
	
	private List<ReductionOption> reductionOptions;
	private ReductionOption reductionOption;
	private SearchOption searchOption;
	private QueryPropertyOption queryPropertyOption;
	private SymmetryOption symmetryOption;
	private LocalConstantsOption lcOption;
	private boolean keepQueryCapacity;
	private int capacity;
	private boolean discreteInclusion = false; // only for VerifyTAPN
	
	public BatchProcessingVerificationOptions(QueryPropertyOption queryPropertyOption, boolean keepQueryCapacity, int capacity, SearchOption searchOption, SymmetryOption symmetryOption, LocalConstantsOption lcOption, ReductionOption reductionOption, boolean discreteInclusion, List<ReductionOption> reductionOptions) {
		Require.that(!(reductionOptions == null && reductionOption == ReductionOption.BatchProcessingUserDefinedReductions), "ReductionOption was given as userdefined but a list of reductionoptions was not given");
		this.searchOption = searchOption;
		this.reductionOption = reductionOption;
		this.queryPropertyOption = queryPropertyOption;
		this.symmetryOption = symmetryOption;
		this.lcOption = lcOption;
		this.keepQueryCapacity = keepQueryCapacity;
		this.capacity = capacity;
		this.discreteInclusion = discreteInclusion;
		this.reductionOptions = reductionOptions;
	}
	
	public BatchProcessingVerificationOptions(QueryPropertyOption queryPropertyOption, boolean keepQueryCapacity, int capacity, SearchOption searchOption, SymmetryOption symmetryOption, LocalConstantsOption lcoption, ReductionOption reductionOption, boolean discreteInclusion) {
		this(queryPropertyOption, keepQueryCapacity, capacity, searchOption, symmetryOption, lcoption, reductionOption, discreteInclusion, null); 
		this.searchOption = searchOption;
		this.reductionOption = reductionOption;
		this.queryPropertyOption = queryPropertyOption;
		this.symmetryOption = symmetryOption;
		this.lcOption = lcoption;
		this.keepQueryCapacity = keepQueryCapacity;
		this.capacity = capacity;
		this.discreteInclusion = discreteInclusion;
	}
	
	public boolean isReductionOptionUserdefined(){
		return reductionOption == ReductionOption.BatchProcessingUserDefinedReductions;
	}
	
	public List<ReductionOption> reductionOptions() {
		Require.that(reductionOption == ReductionOption.BatchProcessingUserDefinedReductions, "Tried to get the userdefined reductionoptions, but the reductionoption is not userdefined");
		return reductionOptions;
	}
	
	public SearchOption searchOption() {
		return searchOption;
	}
	
	public boolean discreteInclusion() {
		return discreteInclusion;
	}
	
	public QueryPropertyOption queryPropertyOption() {
		return queryPropertyOption;
	}
	
	public SymmetryOption symmetry() {
		return symmetryOption;
	}
	
	public LocalConstantsOption localConstants(){
		return lcOption;
	}
	
	public boolean KeepCapacityFromQuery() {
		return keepQueryCapacity;
	}
	
	public int capacity() {
		return capacity;
	}
}
