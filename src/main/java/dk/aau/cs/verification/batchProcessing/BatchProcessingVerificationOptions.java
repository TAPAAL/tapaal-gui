package dk.aau.cs.verification.batchProcessing;

import java.util.List;
import net.tapaal.gui.petrinet.verification.TAPNQuery.SearchOption;
import dk.aau.cs.translations.ReductionOption;
import dk.aau.cs.util.Require;

public class BatchProcessingVerificationOptions {
	private final List<ReductionOption> reductionOptions;
	private final ReductionOption reductionOption;
	private final SearchOption searchOption;
	private int approximationDenominator = 0;
	private final int capacity;
	private boolean discreteInclusion = false; // only for VerifyTAPN
	private boolean useTimeDartPTrie = false;
	private boolean useTimeDart = false;
	private boolean usePTrie = false;
	
	public BatchProcessingVerificationOptions(
        int capacity,
        SearchOption searchOption,
        ReductionOption reductionOption,
        boolean discreteInclusion,
        boolean useTimeDartPTrie,
        boolean useTimeDart,
        boolean usePTrie,
        int approximationDenominator,
        List<ReductionOption> reductionOptions
    ) {
		Require.that(!(reductionOptions == null && reductionOption == ReductionOption.BatchProcessingUserDefinedReductions), "ReductionOption was given as userdefined but a list of reductionoptions was not given");
		this.searchOption = searchOption;
		this.reductionOption = reductionOption;
		this.capacity = capacity;
		this.discreteInclusion = discreteInclusion;
		this.useTimeDartPTrie = useTimeDartPTrie;
		this.useTimeDart = useTimeDart;
		this.usePTrie = usePTrie;
		this.reductionOptions = reductionOptions;
		this.approximationDenominator = approximationDenominator;
	}

	public List<ReductionOption> reductionOptions() {
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
	
	public int capacity() {
		return capacity;
	}
	
	public int approximationDenominator() {
		return approximationDenominator;
	}
}
