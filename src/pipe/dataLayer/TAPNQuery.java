package pipe.dataLayer;

import pipe.gui.widgets.InclusionPlaces;
import dk.aau.cs.TCTL.TCTLAFNode;
import dk.aau.cs.TCTL.TCTLAbstractProperty;
import dk.aau.cs.TCTL.TCTLEFNode;
import dk.aau.cs.TCTL.TCTLEGNode;
import dk.aau.cs.translations.ReductionOption;
import dk.aau.cs.verification.QueryType;

public class TAPNQuery {
	public enum TraceOption {
		SOME, NONE
	};

	public enum SearchOption {
		BFS, DFS, RANDOM, BatchProcessingKeepQueryOption, HEURISTIC, OVERAPPROXIMATE
	};

	public enum HashTableSize {
		MB_4, MB_16, MB_64, MB_256, MB_512
	};

	public enum ExtrapolationOption {
		AUTOMATIC, NONE, DIFF, LOCAL, LOW_UP
	};
	
	public enum WorkflowMode{
		NOT_WORKFLOW, WORKFLOW_SOUNDNESS, WORKFLOW_STRONG_SOUNDNESS
	}

	private String name;
	private int capacity;
	private TraceOption traceOption;
	private SearchOption searchOption;
	private ReductionOption reductionOption;
	private boolean symmetry;
	private boolean gcd;
	private boolean pTrie;
	private boolean timeDart;
	private boolean overApproximation;
	private HashTableSize hashTableSize;
	private ExtrapolationOption extrapolationOption;
	private InclusionPlaces inclusionPlaces;
	private WorkflowMode workflow;
	private boolean useReduction;
	
	private boolean enableOverApproximation = false;
	private boolean enableUnderApproximation = false;
	private int denominator = 2;
	
	private boolean discreteInclusion = false; // Only for VerifyTAPN

	private TCTLAbstractProperty property = null;
	private boolean isActive = true;

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	public boolean isOverApproximationEnabled() {
		return this.enableOverApproximation;
	}
	
	public boolean isUnderApproximationEnabled() {
		return this.enableUnderApproximation;
	}

	
	public int approximationDenominator() {
		return this.denominator;
	}

	public void setDiscreteInclusion(boolean value){
		discreteInclusion = value;
	}
	
	public boolean discreteInclusion(){
		return discreteInclusion;
	}
	
	/**
	 * @param capacity
	 *            the capacity to set
	 */
	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	/**
	 * @return the capacity
	 */
	public int getCapacity() {
		return capacity;
	}

	public String getQuery() {
		return property.toString();
	}

	/**
	 * @return the query
	 */
	public TCTLAbstractProperty getProperty() {
		return property;
	}

	/**
	 * @param traceOption
	 *            the traceOption to set
	 */
	public void setTraceOption(TraceOption traceOption) {
		this.traceOption = traceOption;
	}

	/**
	 * @return the traceOption
	 */
	public TraceOption getTraceOption() {
		return traceOption;
	}

	/**
	 * @param searchOption
	 *            the searchOption to set
	 */
	public void setSearchOption(SearchOption searchOption) {
		this.searchOption = searchOption;
	}

	/**
	 * @return the searchOption
	 */
	public SearchOption getSearchOption() {
		return searchOption;
	}

	/**
	 * @param reductionOption
	 *            the reductionOption to set
	 */
	public void setReductionOption(ReductionOption reductionOption) {
		this.reductionOption = reductionOption;
	}

	/**
	 * @return the reductionOption
	 */
	public ReductionOption getReductionOption() {
		return reductionOption;
	}
	
	public boolean useSymmetry() {
		return symmetry;
	}
	
	public boolean useGCD(){
		return gcd;
	}
	
	public void setUseGCD(boolean useGCD){
		gcd = useGCD;
	}
	
	public boolean useTimeDarts(){
		return timeDart;
	}
	
	public void setUseTimeDarts(boolean useTimeDarts){
		timeDart = useTimeDarts;
	}
	
	public boolean usePTrie(){
		return pTrie;
	}
	
	public void setUsePTrie(boolean usePTrie){
		pTrie = usePTrie;
	}
	
	public void setUseOverApproximation(boolean useOverApproximation){
		overApproximation = useOverApproximation;
	}
	
	public void setUseReduction(boolean useReduction){
		this.useReduction = useReduction;
	}
	
	public boolean useReduction(){
		return useReduction;
	}
	
	public boolean useOverApproximation(){
		return overApproximation;
	}

	/**
	 * @param hashTableSize
	 *            the hashTableSize to set
	 */
	public void setHashTableSize(HashTableSize hashTableSize) {
		this.hashTableSize = hashTableSize;
	}

	/**
	 * @return the hashTableSize
	 */
	public HashTableSize getHashTableSize() {
		return hashTableSize;
	}

	/**
	 * @param extrapolationOption
	 *            the extrapolationOption to set
	 */
	public void setExtrapolationOption(ExtrapolationOption extrapolationOption) {
		this.extrapolationOption = extrapolationOption;
	}

	/**
	 * @return the extrapolationOption
	 */
	public ExtrapolationOption getExtrapolationOption() {
		return extrapolationOption;
	}

	public TAPNQuery(String name, int capacity, TCTLAbstractProperty property,
			TraceOption traceOption, SearchOption searchOption,
			ReductionOption reductionOption, boolean symmetry,boolean gcd,  boolean timeDart, boolean pTrie, boolean overApproximation, HashTableSize hashTabelSize,
			ExtrapolationOption extrapolationOption, WorkflowMode workflow) {
		this(name, capacity, property, traceOption, searchOption, reductionOption, symmetry, gcd, timeDart, pTrie, overApproximation, false, hashTabelSize, extrapolationOption, new InclusionPlaces());
		this.setWorkflowMode(workflow);
	}
	
	public TAPNQuery(String name, int capacity, TCTLAbstractProperty property,
			TraceOption traceOption, SearchOption searchOption,
			ReductionOption reductionOption, boolean symmetry, boolean gcd, boolean timeDart, boolean pTrie, HashTableSize hashTabelSize,
			ExtrapolationOption extrapolationOption) {
		this(name, capacity, property, traceOption, searchOption, reductionOption, symmetry, gcd, timeDart, pTrie, false, false, hashTabelSize, extrapolationOption, new InclusionPlaces());
	}
	
	public TAPNQuery(String name, int capacity, TCTLAbstractProperty property,
			TraceOption traceOption, SearchOption searchOption,
			ReductionOption reductionOption, boolean symmetry, boolean gcd, boolean timeDart, boolean pTrie, boolean overApproximation, boolean reduction, HashTableSize hashTabelSize,
			ExtrapolationOption extrapolationOption, InclusionPlaces inclusionPlaces) {
		this(name, capacity, property, traceOption, searchOption, reductionOption, symmetry, gcd, timeDart, pTrie, overApproximation, reduction, hashTabelSize, extrapolationOption, new InclusionPlaces(), false, false, 0);
	}
	
	public TAPNQuery(String name, int capacity, TCTLAbstractProperty property,
			TraceOption traceOption, SearchOption searchOption,
			ReductionOption reductionOption, boolean symmetry, boolean gcd, boolean timeDart, boolean pTrie, boolean overApproximation, boolean reduction, HashTableSize hashTabelSize,
			ExtrapolationOption extrapolationOption, InclusionPlaces inclusionPlaces, boolean enableOverApproximation, boolean enableUnderApproximation, 
			int approximationDenominator) {
		this.setName(name);
		this.setCapacity(capacity);
		this.property = property;
		this.setTraceOption(traceOption);
		this.setSearchOption(searchOption);
		this.setReductionOption(reductionOption);
		this.symmetry = symmetry;
		this.gcd = gcd;
		this.timeDart = timeDart;
		this.pTrie = pTrie;
		this.overApproximation = overApproximation;
		this.setHashTableSize(hashTabelSize);
		this.setExtrapolationOption(extrapolationOption);
		this.inclusionPlaces = inclusionPlaces;
		this.useReduction = reduction;
		this.enableOverApproximation = enableOverApproximation;
		this.enableUnderApproximation = enableUnderApproximation;
		this.denominator = approximationDenominator;
	}


	@Override
	public String toString() {
		return getName();
	}

	public void set(TAPNQuery newQuery) {
		name = newQuery.getName();
		capacity = newQuery.getCapacity();
		property = newQuery.getProperty();
		traceOption = newQuery.getTraceOption();
		searchOption = newQuery.getSearchOption();
		reductionOption = newQuery.getReductionOption();
		symmetry = newQuery.useSymmetry();
		hashTableSize = newQuery.getHashTableSize();
		extrapolationOption = newQuery.getExtrapolationOption();
		discreteInclusion = newQuery.discreteInclusion();
		inclusionPlaces = newQuery.inclusionPlaces();
	}

	public InclusionPlaces inclusionPlaces() {
		return inclusionPlaces;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;	
	}
	
	public boolean isActive() {
		return isActive;
	}

	public TAPNQuery copy() {
		TAPNQuery copy = new TAPNQuery(name, capacity, property.copy(), traceOption, searchOption, reductionOption, symmetry, gcd, timeDart, pTrie, overApproximation, useReduction, hashTableSize, extrapolationOption, inclusionPlaces, enableOverApproximation, enableUnderApproximation, denominator);
		copy.setDiscreteInclusion(discreteInclusion);
		copy.setActive(isActive);
		
		return copy;
	}
	
	public QueryType queryType(){
		if(property instanceof TCTLEFNode) return QueryType.EF;
		else if(property instanceof TCTLEGNode) return QueryType.EG;
		else if(property instanceof TCTLAFNode) return QueryType.AF;
		else return QueryType.AG;
	}

	public WorkflowMode getWorkflowMode() {
		return workflow;
	}

	public void setWorkflowMode(WorkflowMode workflow) {
		this.workflow = workflow;
	}
	
}
