package dk.aau.cs.verification.VerifyTAPN;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import pipe.dataLayer.TAPNQuery.SearchOption;
import pipe.dataLayer.TAPNQuery.QueryReductionTime;
import pipe.dataLayer.TAPNQuery.TraceOption;
import pipe.dataLayer.TAPNQuery.AlgorithmOption;
import pipe.dataLayer.TAPNQuery.QueryCategory;
import pipe.gui.MessengerImpl;
import pipe.gui.widgets.InclusionPlaces;

public class VerifyPNOptions extends VerifyTAPNOptions{
	private static final Map<TraceOption, String> traceMap = createTraceOptionsMap();
	private static final Map<SearchOption, String> searchMap = createSearchOptionsMap();
	private final ModelReduction modelReduction;
	private final QueryCategory queryCategory;
	private final AlgorithmOption algorithmOption;
	private boolean useSiphontrap = false; 
	private QueryReductionTime queryReductionTime;
	private boolean useStubbornReduction = true;
	private boolean unfold = false;
	private boolean useTarOption;
	private boolean partition;
	private boolean colorFixpoint;


	public VerifyPNOptions(int extraTokens, TraceOption traceOption, SearchOption search, boolean useOverApproximation, ModelReduction modelReduction,
                           boolean enableOverApproximation, boolean enableUnderApproximation, int approximationDenominator, QueryCategory queryCategory, AlgorithmOption algorithmOption,
                           boolean siphontrap, QueryReductionTime queryReduction, boolean stubbornReduction, boolean unfold, String pathToReducedNet, boolean useTarOption, boolean partition, boolean colorFixpoint) {
		super(extraTokens, traceOption, search, true, useOverApproximation, false, new InclusionPlaces(), enableOverApproximation, enableUnderApproximation, approximationDenominator, useTarOption);
		this.modelReduction = modelReduction;
		this.queryCategory = queryCategory;
		this.algorithmOption = algorithmOption;
		this.useSiphontrap = siphontrap;
		this.queryReductionTime = queryReduction;
		this.useStubbornReduction = stubbornReduction;
		this.unfold = unfold;
        this.partition = partition;
        this.colorFixpoint = colorFixpoint;
		this.useTarOption = useTarOption;
		reducedModelPath = pathToReducedNet;

        try {
            unfoldedModelPath = File.createTempFile("unfolded-", ".pnml").getAbsolutePath();
        } catch (IOException e) {
            new MessengerImpl().displayErrorMessage(
                e.getMessage(),
                "Error");
            return;
        }

	}

    public VerifyPNOptions(int extraTokens, TraceOption traceOption, SearchOption search, boolean useOverApproximation, boolean useModelReduction,
                           boolean enableOverApproximation, boolean enableUnderApproximation, int approximationDenominator, QueryCategory queryCategory, AlgorithmOption algorithmOption,
                           boolean siphontrap, QueryReductionTime queryReduction, boolean stubbornReduction, boolean useTarOption, boolean partition, boolean colorFixpoint) {
        this(extraTokens, traceOption, search, useOverApproximation, useModelReduction? ModelReduction.AGGRESSIVE:ModelReduction.NO_REDUCTION, enableOverApproximation,
            enableUnderApproximation, approximationDenominator,queryCategory, algorithmOption, siphontrap, queryReduction, stubbornReduction, false,null, useTarOption, partition, colorFixpoint);
    }

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();

		result.append("-k ");
		result.append(extraTokens+tokensInModel);
		result.append(traceMap.get(traceOption));
		result.append(searchMap.get(searchOption));
		switch(getModelReduction()){
		case AGGRESSIVE:
			result.append(" -r 1 ");
            String writeReducedCMD = " --write-reduced " +reducedModelPath;
            result.append(writeReducedCMD);
			break;
		case NO_REDUCTION:
			result.append(" -r 0 ");

			break;
		case BOUNDPRESERVING:
			result.append(" -r 2 ");
            writeReducedCMD = " --write-reduced " +reducedModelPath;
            result.append(writeReducedCMD);
			break;
		default:
			break;			
		}

        if(unfold){
            String writeUnfoldedCMD = " --write-unfolded-net " +unfoldedModelPath;
            result.append(writeUnfoldedCMD);
        }

		if (this.queryCategory == QueryCategory.CTL){
			result.append(" -ctl " + (getAlgorithmOption() == AlgorithmOption.CERTAIN_ZERO ? "czero" : "local"));
			//if(!unfold){
                result.append(" -x 1");
            //}
		}

/*		if(unfold){
		    result.append(" --write-simplified " + );
        }*/

		if (this.useSiphontrap) {
			result.append(" -a 10 ");
		}
		if (this.queryReductionTime == QueryReductionTime.NoTime) {
			result.append(" -q 0 ");
		} else if (this.queryReductionTime == QueryReductionTime.ShortestTime) {
		    //Run query reduction for 1 second, to avoid conflict with -s OverApprox argument, but also still not run the verification.
		    result.append(" -q 1");
        }
		if (!this.useStubbornReduction) {
			result.append(" -p ");
		}
		if (this.useTarOption) {
		    result.append(" -tar ");
        }

		if(!this.partition){
		    result.append(" --disable-partitioning");
        }

		if(!this.colorFixpoint){
		    result.append(" --disable-cfp");
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
