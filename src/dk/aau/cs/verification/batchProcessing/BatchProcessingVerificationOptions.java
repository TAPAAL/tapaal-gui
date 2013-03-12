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
	
	private List<ReductionOption> reductionOptions;
	private ReductionOption reductionOption;
	private SearchOption searchOption;
	private QueryPropertyOption queryPropertyOption;
	private SymmetryOption symmetryOption;
	private boolean keepQueryCapacity;
	private int capacity;
	private boolean discreteInclusion = false; // only for VerifyTAPN
	private boolean useTimeDartPTrie = false;
	private boolean useTimeDart = false;
	private boolean usePTrie = false;
	
	public BatchProcessingVerificationOptions(QueryPropertyOption queryPropertyOption, boolean keepQueryCapacity, int capacity, SearchOption searchOption, SymmetryOption symmetryOption, ReductionOption reductionOption, boolean discreteInclusion,
			boolean useTimeDartPTrie, boolean useTimeDart, boolean usePTrie, List<ReductionOption> reductionOptions) {
		Require.that(!(reductionOptions == null && reductionOption == ReductionOption.BatchProcessingUserDefinedReductions), "ReductionOption was given as userdefined but a list of reductionoptions was not given");
		this.searchOption = searchOption;
		this.reductionOption = reductionOption;
		this.queryPropertyOption = queryPropertyOption;
		this.symmetryOption = symmetryOption;
		this.keepQueryCapacity = keepQueryCapacity;
		this.capacity = capacity;
		this.discreteInclusion = discreteInclusion;
		this.useTimeDartPTrie = useTimeDartPTrie;
		this.useTimeDart = useTimeDart;
		this.usePTrie = usePTrie;
		this.reductionOptions = reductionOptions;
	}
	
	public BatchProcessingVerificationOptions(QueryPropertyOption queryPropertyOption, boolean keepQueryCapacity, int capacity, SearchOption searchOption, SymmetryOption symmetryOption, ReductionOption reductionOption, boolean discreteInclusion,
			boolean useTimeDartPTrie, boolean useTimeDart, boolean usePTrie) {
		this(queryPropertyOption, keepQueryCapacity, capacity, searchOption, symmetryOption, reductionOption, discreteInclusion, useTimeDartPTrie, useTimeDart, usePTrie, null); 
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
	
	public boolean useTimeDartPTrie(){
		return useTimeDartPTrie;
	}
	
	public boolean useTimeDart(){
		return useTimeDart;
	}
	
	public boolean usePTrie(){
		return usePTrie;
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
