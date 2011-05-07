package dk.aau.cs.verification.VerifyTAPN;

import java.util.HashMap;
import java.util.Map;

import pipe.dataLayer.TAPNQuery.SearchOption;
import pipe.dataLayer.TAPNQuery.TraceOption;
import dk.aau.cs.verification.VerificationOptions;

public class VerifyTAPNOptions implements VerificationOptions{
	private TraceOption traceOption;
	private SearchOption searchOption;
	private int extraTokens;
	private int tokensInModel;
	private boolean symmetry;

	private static final Map<TraceOption, String> traceMap = createTraceOptionsMap();
	private static final Map<SearchOption, String> searchMap = createSearchOptionsMap();

	public VerifyTAPNOptions(int extraTokens, TraceOption traceOption, SearchOption search, boolean symmetry) {
		this.extraTokens = extraTokens;
		this.traceOption = traceOption;
		this.searchOption = search;
		this.symmetry = symmetry;
	}

	public TraceOption trace() {
		return traceOption;
	}
	
	public boolean symmetry() {
		return symmetry;
	}
	
	public void setTokensInModel(int tokens){ // TODO: Get rid of this method when verifytapn refactored
		this.tokensInModel = tokens;
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();

		result.append("-k ");
		result.append(extraTokens+tokensInModel);
		result.append(" ");
		result.append(traceMap.get(traceOption));
		result.append(" ");
		result.append(searchMap.get(searchOption));
		result.append(symmetry ? "" : "-s"); // symmetry is on by default in verifyTAPN so "-s" disables it
		
		return result.toString();
	}

	public static final Map<TraceOption, String> createTraceOptionsMap() {
		HashMap<TraceOption, String> map = new HashMap<TraceOption, String>();
		map.put(TraceOption.SOME, "-t 1 -x");
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
