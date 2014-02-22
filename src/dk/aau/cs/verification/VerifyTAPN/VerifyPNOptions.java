package dk.aau.cs.verification.VerifyTAPN;

import java.util.HashMap;
import java.util.Map;

import pipe.dataLayer.TAPNQuery.SearchOption;
import pipe.dataLayer.TAPNQuery.TraceOption;
import pipe.gui.widgets.InclusionPlaces;

public class VerifyPNOptions extends VerifyTAPNOptions{
	private static final Map<TraceOption, String> traceMap = createTraceOptionsMap();
	private static final Map<SearchOption, String> searchMap = createSearchOptionsMap();
	private boolean useReduction;
	
	public VerifyPNOptions(int extraTokens, TraceOption traceOption, SearchOption search, boolean useOverApproximation, boolean useReduction) {
		super(extraTokens, traceOption, search, true, useOverApproximation, false, new InclusionPlaces());
		this.useReduction = useReduction;
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();

		result.append("-k ");
		result.append(extraTokens+tokensInModel);
		result.append(traceMap.get(traceOption));
		result.append(" -m 0 ");	// Disable memory limit to ensure similar behaviour to other engines
		result.append(searchMap.get(searchOption));
		result.append(useOverApproximation()? "":" -d ");	// Disable over-approximation if disabled
		result.append(useReduction()? " -r ":"");
		return result.toString();
	}

	public static Map<TraceOption, String> createTraceOptionsMap() {
		HashMap<TraceOption, String> map = new HashMap<TraceOption, String>();
		map.put(TraceOption.SOME, " -t");
		map.put(TraceOption.NONE, "");

		return map;
	}

	private static final Map<SearchOption, String> createSearchOptionsMap() {
		HashMap<SearchOption, String> map = new HashMap<SearchOption, String>();
		map.put(SearchOption.BFS, "-s BFS");
		map.put(SearchOption.DFS, "-s DFS");
		map.put(SearchOption.RANDOM, "-s RDFS");
		map.put(SearchOption.HEURISTIC, "-s BestFS");
		map.put(SearchOption.OVERAPPROXIMATE, "-s OverApprox");

		return map;
	}
	
	public boolean useReduction(){
		return useReduction;
	}
}
