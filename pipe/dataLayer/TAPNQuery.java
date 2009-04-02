package pipe.dataLayer;

import java.util.Enumeration;

public class TAPNQuery {
	public enum TraceOption {SOME, FASTEST, NONE};
	public enum SearchOption {BFS, DFS, RDFS, CLOSE_TO_TARGET_FIRST};
	public enum ReductionOption {NAIVE, NAIVE_UPPAAL_SYM, ADV_UPPAAL_SYM};
	
	public String name;
	public int capacity;
	public String query;
	public TraceOption traceOption;
	public SearchOption searchOption;
	public ReductionOption reductionOption;
	
	public TAPNQuery(String name, int capacity, String query, TraceOption traceOption, SearchOption searchOption, ReductionOption reductionOption) {
		this.name = name;
		this.capacity = capacity;
		this.query = query;
		this.traceOption = traceOption;
		this.searchOption = searchOption;
		this.reductionOption = reductionOption;
	}
}
