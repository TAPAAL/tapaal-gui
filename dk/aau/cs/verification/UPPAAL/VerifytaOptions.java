package dk.aau.cs.verification.UPPAAL;

import java.util.HashMap;
import java.util.Map;

import pipe.dataLayer.TAPNQuery.SearchOption;
import pipe.dataLayer.TAPNQuery.TraceOption;
import dk.aau.cs.translations.ReductionOption;
import dk.aau.cs.verification.VerificationOptions;

public class VerifytaOptions implements VerificationOptions {
	private TraceOption traceOption;
	private SearchOption searchOption;
	private boolean untimedTrace;
	private ReductionOption reduction;
	
	private static final Map<TraceOption, String> traceMap = createTraceOptionsMap();
	private static final Map<SearchOption, String> searchMap = createSearchOptionsMap();
	
	public VerifytaOptions(TraceOption trace, SearchOption search, boolean untimedTrace, ReductionOption reduction){
		this.traceOption = trace;
		this.searchOption = search;
		this.untimedTrace = untimedTrace;
		this.reduction = reduction;
	}
	
	public TraceOption trace(){
		return traceOption;
	}
		
	@Override
	public String toString(){
		StringBuffer result = new StringBuffer();
		
		if (untimedTrace){
			result.append("-Y ");
		}
		
		result.append(traceMap.get(traceOption));
		result.append(searchMap.get(searchOption));
	
		return result.toString();
	}
	
	public static final Map<TraceOption, String> createTraceOptionsMap(){
		HashMap<TraceOption, String> map = new HashMap<TraceOption, String>();
		map.put(TraceOption.SOME, "-t0");
		map.put(TraceOption.FASTEST, "-t2");
		map.put(TraceOption.NONE, "");
		
		return map;
	}
	
	private static final Map<SearchOption, String> createSearchOptionsMap() {
		HashMap<SearchOption, String> map = new HashMap<SearchOption, String>();
		map.put(SearchOption.BFS, "-o0");
		map.put(SearchOption.DFS, "-o1");
		map.put(SearchOption.RDFS, "-o2");
		map.put(SearchOption.CLOSE_TO_TARGET_FIRST, "-o6");
		
		return map;
	}

	public ReductionOption getReduction() {
		return reduction;
	}
	
	public String getOption(String option){
		if(option.equalsIgnoreCase("trace")) return traceOption.toString();
		if(option.equalsIgnoreCase("search-strategy")) return searchOption.toString();
		if(option.equalsIgnoreCase("untimed-trace")) return String.valueOf(untimedTrace);

		return null;
	}
}
