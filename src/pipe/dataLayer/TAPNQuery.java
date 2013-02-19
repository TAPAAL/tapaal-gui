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
		BFS, DFS, RANDOM, BatchProcessingKeepQueryOption, HEURISTIC
	};

	public enum HashTableSize {
		MB_4, MB_16, MB_64, MB_256, MB_512
	};

	public enum ExtrapolationOption {
		AUTOMATIC, NONE, DIFF, LOCAL, LOW_UP
	};

	private String name;
	private int capacity;
	private TraceOption traceOption;
	private SearchOption searchOption;
	private ReductionOption reductionOption;
	private boolean symmetry;
	private boolean pTrie;
	private boolean timeDart;
	private HashTableSize hashTableSize;
	private ExtrapolationOption extrapolationOption;
	private InclusionPlaces inclusionPlaces;
	
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
	
	public boolean useTimeDarts(){
		return timeDart;
	}
	
	public boolean usePTrie(){
		return pTrie;
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
			ReductionOption reductionOption, boolean symmetry, boolean timeDart, boolean pTrie, HashTableSize hashTabelSize,
			ExtrapolationOption extrapolationOption) {
		this(name, capacity, property, traceOption, searchOption, reductionOption, symmetry, timeDart, pTrie, hashTabelSize, extrapolationOption, new InclusionPlaces());
	}
	
	public TAPNQuery(String name, int capacity, TCTLAbstractProperty property,
			TraceOption traceOption, SearchOption searchOption,
			ReductionOption reductionOption, boolean symmetry, boolean timeDart, boolean pTrie, HashTableSize hashTabelSize,
			ExtrapolationOption extrapolationOption, InclusionPlaces inclusionPlaces) {
		this.setName(name);
		this.setCapacity(capacity);
		this.property = property;
		this.setTraceOption(traceOption);
		this.setSearchOption(searchOption);
		this.setReductionOption(reductionOption);
		this.symmetry = symmetry;
		this.timeDart = timeDart;
		this.pTrie = pTrie;
		this.setHashTableSize(hashTabelSize);
		this.setExtrapolationOption(extrapolationOption);
		this.inclusionPlaces = inclusionPlaces;
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
		TAPNQuery copy = new TAPNQuery(name, capacity, property.copy(), traceOption, searchOption, reductionOption, symmetry, timeDart, pTrie, hashTableSize, extrapolationOption, inclusionPlaces);
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
	
}
