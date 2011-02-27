package dk.aau.cs.verification.VerifyTAPN;

import java.util.HashMap;
import java.util.Map;

import pipe.dataLayer.TAPNQuery.SearchOption;
import pipe.dataLayer.TAPNQuery.TraceOption;
import pipe.gui.KBoundAnalyzer;
import dk.aau.cs.verification.VerificationOptions;

public class VerifyTAPNOptions implements VerificationOptions{
	private TraceOption traceOption;
	private SearchOption searchOption;
	private int kBound;

	private static final Map<TraceOption, String> traceMap = createTraceOptionsMap();
	private static final Map<SearchOption, String> searchMap = createSearchOptionsMap();

	public VerifyTAPNOptions(int kBound, TraceOption traceOption, SearchOption search) {
		this.kBound = kBound;
		this.traceOption = traceOption;
		this.searchOption = search;
	}

	public TraceOption trace() {
		return traceOption;
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();

		result.append("-k " + kBound + " ");
		result.append(traceMap.get(traceOption));
		result.append(searchMap.get(searchOption));
		
		return result.toString();
	}

	public static final Map<TraceOption, String> createTraceOptionsMap() {
		HashMap<TraceOption, String> map = new HashMap<TraceOption, String>();
		map.put(TraceOption.SOME, "-t1");
		map.put(TraceOption.NONE, "");

		return map;
	}

	private static final Map<SearchOption, String> createSearchOptionsMap() {
		HashMap<SearchOption, String> map = new HashMap<SearchOption, String>();
		map.put(SearchOption.BFS, "-o0");
		map.put(SearchOption.DFS, "-o1");

		return map;
	}
}
