package pipe.dataLayer;

import dk.aau.cs.TCTL.TCTLAbstractProperty;
import dk.aau.cs.translations.ReductionOption;

public class TAPNQuery {
	public enum TraceOption {
		SOME, FASTEST, NONE
	};

	public enum SearchOption {
		BFS, DFS, RDFS, CLOSE_TO_TARGET_FIRST
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
	private HashTableSize hashTableSize;
	private ExtrapolationOption extrapolationOption;

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
			ReductionOption reductionOption, HashTableSize hashTabelSize,
			ExtrapolationOption extrapolationOption) {
		this.setName(name);
		this.setCapacity(capacity);
		this.property = property;
		this.setTraceOption(traceOption);
		this.setSearchOption(searchOption);
		this.setReductionOption(reductionOption);
		this.setHashTableSize(hashTabelSize);
		this.setExtrapolationOption(extrapolationOption);
	}

	@Override
	public String toString() {
		return getName();
	}

	public void set(TAPNQuery newQuery) {
		this.name = newQuery.getName();
		this.capacity = newQuery.getCapacity();
		this.property = newQuery.getProperty();
		this.traceOption = newQuery.getTraceOption();
		this.searchOption = newQuery.getSearchOption();
		this.reductionOption = newQuery.getReductionOption();
		this.hashTableSize = newQuery.getHashTableSize();
		this.extrapolationOption = newQuery.getExtrapolationOption();
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;	
	}
	
	public boolean isActive() {
		return isActive;
	}
}
