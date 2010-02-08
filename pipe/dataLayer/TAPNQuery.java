package pipe.dataLayer;


public class TAPNQuery {
	public enum TraceOption {SOME, FASTEST, NONE};
	public enum SearchOption {BFS, DFS, RDFS, CLOSE_TO_TARGET_FIRST};
	public enum ReductionOption {NAIVE, NAIVE_UPPAAL_SYM, ADV_UPPAAL_SYM, ADV_NOSYM, INHIB_TO_PRIO_STANDARD, INHIB_TO_PRIO_SYM, BROADCAST_STANDARD, BROADCAST_SYM, BROADCAST_DEG2_SYM};
	public enum	HashTableSize {MB_4, MB_16, MB_64, MB_256, MB_512};
	public enum	ExtrapolationOption {AUTOMATIC, NONE, DIFF, LOCAL, LOW_UP};
	
	public String name;
	public int capacity;
	public String query;
	public TraceOption traceOption;
	public SearchOption searchOption;
	public ReductionOption reductionOption;
	public HashTableSize hashTableSize;
	public ExtrapolationOption extrapolationOption;
	
	public TAPNQuery(String name, int capacity, String query, TraceOption traceOption, SearchOption searchOption, ReductionOption reductionOption, HashTableSize hashTabelSize, ExtrapolationOption extrapolationOption) {
		this.name = name;
		this.capacity = capacity;
		this.query = query;
		this.traceOption = traceOption;
		this.searchOption = searchOption;
		this.reductionOption = reductionOption;
		this.hashTableSize = hashTabelSize;
		this.extrapolationOption = extrapolationOption;
	}
	
	@Override
	public String toString()
	{
		return name;
	}

	public void set(TAPNQuery newQuery) {
		this.name = newQuery.name;
		this.capacity = newQuery.capacity;
		this.query = newQuery.query;
		this.traceOption = newQuery.traceOption;
		this.searchOption = newQuery.searchOption;
		this.reductionOption = newQuery.reductionOption;
		this.hashTableSize = newQuery.hashTableSize;
		this.extrapolationOption = newQuery.extrapolationOption;		
	}
}
