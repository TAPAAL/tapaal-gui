package dk.aau.cs.verification.UPPAAL;

import java.util.HashMap;
import java.util.Map;

import pipe.dataLayer.TAPNQuery.SearchOption;
import pipe.dataLayer.TAPNQuery.TraceOption;
import dk.aau.cs.translations.ReductionOption;
import dk.aau.cs.verification.VerificationOptions;

public class VerifytaOptions extends VerificationOptions {

	private final boolean untimedTrace;
	private final ReductionOption reduction;
	private boolean symmetry;


	private static final Map<TraceOption, String> traceMap = createTraceOptionsMap();
	private static final Map<SearchOption, String> searchMap = createSearchOptionsMap();

	public VerifytaOptions(TraceOption trace, SearchOption search,
			boolean untimedTrace, ReductionOption reduction, boolean symmetry, boolean useStateequationCheck, boolean enableOverApproximation, boolean enableUnderApproximation, int approximationDenominator) {
		traceOption = trace;
		searchOption = search;
		this.untimedTrace = untimedTrace;
		this.reduction = reduction;
		this.symmetry = symmetry;
		this.useStateequationCheck = useStateequationCheck;
		this.enabledOverApproximation = enableOverApproximation;
		this.enabledUnderApproximation = enableUnderApproximation;
		this.approximationDenominator = approximationDenominator;
	}

	public boolean symmetry() {
		return symmetry;
	}
	
	public void setSymmetry(boolean newOption) {
		this.symmetry = newOption;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();

		if (untimedTrace) {
			result.append("-Y ");
		}

		result.append(traceMap.get(traceOption));
		result.append(searchMap.get(searchOption));

		return result.toString();
	}

	public static final Map<TraceOption, String> createTraceOptionsMap() {
		HashMap<TraceOption, String> map = new HashMap<TraceOption, String>();
		map.put(TraceOption.SOME, "-t0");
		map.put(TraceOption.NONE, "");

		return map;
	}

	private static final Map<SearchOption, String> createSearchOptionsMap() {
		HashMap<SearchOption, String> map = new HashMap<SearchOption, String>();
		map.put(SearchOption.BFS, "-o0");
		map.put(SearchOption.DFS, "-o1");
		map.put(SearchOption.RANDOM, "-o2");
		map.put(SearchOption.HEURISTIC, "-o0"); // Maybe -o6 for close-to-target search?

		return map;
	}

	public ReductionOption getReduction() {
		return reduction;
	}

	@Override
	public int extraTokens() {
		return 0;
	}

}
