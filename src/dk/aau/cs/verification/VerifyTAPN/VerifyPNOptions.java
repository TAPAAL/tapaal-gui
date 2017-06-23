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
	private boolean useSiphontrap = false; 
	private boolean useQueryReduction = true; 
	private boolean useStubbornReduction = true;
	
	public VerifyPNOptions(int extraTokens, TraceOption traceOption, SearchOption search, boolean useOverApproximation, ModelReduction modelReduction, 
		boolean enableOverApproximation, boolean enableUnderApproximation, int approximationDenominator, QueryCategory queryCategory, AlgorithmOption algorithmOption,
		boolean siphontrap, boolean queryReduction, boolean stubbornReduction) {
		super(extraTokens, traceOption, search, true, useOverApproximation, false, new InclusionPlaces(), enableOverApproximation, enableUnderApproximation, approximationDenominator);
		this.modelReduction = modelReduction;
		this.queryCategory = queryCategory;
		this.algorithmOption = algorithmOption;
		this.useSiphontrap = siphontrap;
		this.useQueryReduction = queryReduction;
		this.useStubbornReduction = stubbornReduction;
	}
	
	public VerifyPNOptions(int extraTokens, TraceOption traceOption, SearchOption search, boolean useOverApproximation, boolean useModelReduction, 
		boolean enableOverApproximation, boolean enableUnderApproximation, int approximationDenominator, QueryCategory queryCategory, AlgorithmOption algorithmOption,
		boolean siphontrap, boolean queryReduction, boolean stubbornReduction) {
		this(extraTokens, traceOption, search, useOverApproximation, useModelReduction? ModelReduction.AGGRESSIVE:ModelReduction.NO_REDUCTION, enableOverApproximation, 
			enableUnderApproximation, approximationDenominator,queryCategory, algorithmOption, siphontrap, queryReduction, stubbornReduction);
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();

		result.append("-k ");
		result.append(extraTokens+tokensInModel);
		result.append(traceMap.get(traceOption));
		result.append(searchMap.get(searchOption));
		switch(getModelReduction()){
		case AGGRESSIVE:
			result.append(" -r 1 ");
			break;
		case NO_REDUCTION:
			result.append(" -r 0 ");
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
		
		if (this.useSiphontrap) {
			result.append(" -a 10 ");
		}
		if (!this.useQueryReduction) {
			result.append(" -q 0 ");
		}
		if (!this.useStubbornReduction) {
			result.append(" -p ");
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
		map.put(SearchOption.BFS, " -s BFS");
		map.put(SearchOption.DFS, " -s DFS");
		map.put(SearchOption.RANDOM, " -s RDFS");
		map.put(SearchOption.HEURISTIC, " -s BestFS");
		map.put(SearchOption.OVERAPPROXIMATE, " -s OverApprox");

		return map;
	}
	
	public ModelReduction getModelReduction(){
		return modelReduction;
	}
	
	public AlgorithmOption getAlgorithmOption(){
		return this.algorithmOption;
	}
}
