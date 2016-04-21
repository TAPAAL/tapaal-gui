package dk.aau.cs.verification.VerifyTAPN;

import java.util.HashMap;
import java.util.Map;

import pipe.dataLayer.TAPNQuery.SearchOption;
import pipe.dataLayer.TAPNQuery.TraceOption;
import pipe.dataLayer.TAPNQuery.AlgorithmOption;
import pipe.dataLayer.TAPNQuery.QueryCategory;
import pipe.gui.widgets.InclusionPlaces;

public class VerifyPNOptions extends VerifyTAPNOptions{
	private static final Map<TraceOption, String> traceMap = createTraceOptionsMap();
	private static final Map<SearchOption, String> searchMap = createSearchOptionsMap();
	private ModelReduction modelReduction;
	private QueryCategory queryCategory;
	private AlgorithmOption algorithmOption;
	
	public VerifyPNOptions(int extraTokens, TraceOption traceOption, SearchOption search, boolean useOverApproximation, ModelReduction modelReduction, boolean enableOverApproximation, boolean enableUnderApproximation, int approximationDenominator, QueryCategory queryCategory, AlgorithmOption algorithmOption) {
		super(extraTokens, traceOption, search, true, useOverApproximation, false, new InclusionPlaces(), enableOverApproximation, enableUnderApproximation, approximationDenominator);
		this.modelReduction = modelReduction;
		this.queryCategory = queryCategory;
		this.algorithmOption = algorithmOption;
	}
	
	public VerifyPNOptions(int extraTokens, TraceOption traceOption, SearchOption search, boolean useOverApproximation, boolean useModelReduction, boolean enableOverApproximation, boolean enableUnderApproximation, int approximationDenominator, QueryCategory queryCategory, AlgorithmOption algorithmOption) {
		this(extraTokens, traceOption, search, useOverApproximation, useModelReduction? ModelReduction.AGGRESSIVE:ModelReduction.NO_REDUCTION, enableOverApproximation, enableUnderApproximation, approximationDenominator,queryCategory, algorithmOption);
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
		switch(getModelReduction()){
		case AGGRESSIVE:
			result.append(" -r 1 ");
			break;
		case NO_REDUCTION:
			break;
		case BOUNDPRESERVING:
			result.append(" -r 2 ");
			break;
		default:
			break;			
		}
		if (this.queryCategory == QueryCategory.CTL){
			result.append(" -ctl " + (getAlgorithmOption() == AlgorithmOption.CERTAIN_ZERO ? "czero" : "local"));
			result.append(" -x 1");
		}
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
	
	public ModelReduction getModelReduction(){
		return modelReduction;
	}
	
	public AlgorithmOption getAlgorithmOption(){
		return this.algorithmOption;
	}
}
