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
	private static final Map<TraceOption, String> traceMap = Map.of(
        TraceOption.SOME, " --trace",
		TraceOption.NONE, ""
    );
	private static final Map<SearchOption, String> searchMap = Map.of(
        SearchOption.BFS, " --search-strategy DFS",
        SearchOption.RANDOM, " --search-strategy RDFS",
        SearchOption.HEURISTIC, " --search-strategy BestFS",
        SearchOption.OVERAPPROXIMATE, " --search-strategy OverApprox"
    );

	private final ModelReduction modelReduction;
	private final QueryCategory queryCategory;
	private final AlgorithmOption algorithmOption;
	private final boolean useSiphontrap;
	private final QueryReductionTime queryReductionTime;
	private final boolean useStubbornReduction;
	private final boolean unfold;
	private final boolean colored;
	private final boolean useTarOption;
	private final boolean partition;
	private final boolean colorFixpoint;
    private final boolean symmetricVars;
    private final boolean useTarjan;

	public VerifyPNOptions(
        int extraTokens,
        TraceOption traceOption,
        SearchOption search,
        boolean useOverApproximation,
        ModelReduction modelReduction,
        boolean enableOverApproximation,
        boolean enableUnderApproximation,
        int approximationDenominator,
        QueryCategory queryCategory,
        AlgorithmOption algorithmOption,
        boolean siphontrap,
        QueryReductionTime queryReduction,
        boolean stubbornReduction,
        String pathToReducedNet,
        boolean useTarOption,
        boolean useTarjan,
        boolean colored,
        boolean unfold,
        boolean partition,
        boolean colorFixpoint,
        boolean useSymmetricVars
    ) {
		super(extraTokens, traceOption, search, true, useOverApproximation, false, new InclusionPlaces(), enableOverApproximation, enableUnderApproximation, approximationDenominator, useTarOption);

        this.modelReduction = modelReduction;
		this.queryCategory = queryCategory;
		this.algorithmOption = algorithmOption;
		this.useSiphontrap = siphontrap;
		this.queryReductionTime = queryReduction;
		this.useStubbornReduction = stubbornReduction;
		this.unfold = unfold;
		this.colored = colored;
        this.partition = partition;
        this.colorFixpoint = colorFixpoint;
        this.useTarOption = useTarOption;
        this.useTarjan = useTarjan;
		this.reducedModelPath = pathToReducedNet;
		this.symmetricVars = useSymmetricVars;

        try {
            unfoldedModelPath = File.createTempFile("unfolded-", ".pnml").getAbsolutePath();
            unfoldedQueriesPath = File.createTempFile("unfoldedQueries-", ".xml").getAbsolutePath();
        } catch (IOException e) {
            new MessengerImpl().displayErrorMessage(e.getMessage(), "Error");
        }
	}

    public VerifyPNOptions(
        int extraTokens,
        TraceOption traceOption,
        SearchOption search,
        boolean useOverApproximation,
        ModelReduction modelReduction,
        boolean enableOverApproximation,
        boolean enableUnderApproximation,
        int approximationDenominator,
        QueryCategory queryCategory,
        AlgorithmOption algorithmOption,
        boolean siphontrap,
        QueryReductionTime queryReduction,
        boolean stubbornReduction,
        String pathToReducedNet,
        boolean useTarOption,
        boolean useTarjan,
        boolean colored,
        boolean partition,
        boolean colorFixpoint,
        boolean useSymmetricVars
    ) {
        this(extraTokens, traceOption, search, useOverApproximation, modelReduction, enableOverApproximation, enableUnderApproximation, approximationDenominator,queryCategory, algorithmOption, siphontrap, queryReduction, stubbornReduction, pathToReducedNet, useTarOption, useTarjan, colored, false, partition, colorFixpoint, useSymmetricVars);
    }

	public VerifyPNOptions(
	    int extraTokens,
        TraceOption traceOption,
        SearchOption search,
        boolean useOverApproximation,
        boolean useModelReduction,
        boolean enableOverApproximation,
        boolean enableUnderApproximation,
        int approximationDenominator,
        QueryCategory queryCategory,
        AlgorithmOption algorithmOption,
        boolean siphontrap,
        QueryReductionTime queryReduction,
        boolean stubbornReduction
    ) {
		this(extraTokens, traceOption, search, useOverApproximation, useModelReduction? ModelReduction.AGGRESSIVE:ModelReduction.NO_REDUCTION, enableOverApproximation, enableUnderApproximation, approximationDenominator,queryCategory, algorithmOption, siphontrap, queryReduction, stubbornReduction, null, false, true, false, false, false, false, false);
	}

    public VerifyPNOptions(
        int extraTokens,
        TraceOption traceOption,
        SearchOption search,
        boolean useOverApproximation,
        boolean useModelReduction,
        boolean enableOverApproximation,
        boolean enableUnderApproximation,
        int approximationDenominator,
        QueryCategory queryCategory,
        AlgorithmOption algorithmOption,
        boolean siphontrap,
        QueryReductionTime queryReduction,
        boolean stubbornReduction,
        boolean useTarOption
    ) {
        this(extraTokens, traceOption, search, useOverApproximation, useModelReduction? ModelReduction.AGGRESSIVE:ModelReduction.NO_REDUCTION, enableOverApproximation, enableUnderApproximation, approximationDenominator,queryCategory, algorithmOption, siphontrap, queryReduction, stubbornReduction, null, useTarOption, true, false, false, false, false, false);
    }

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();

		result.append("--k-bound ");
		result.append(extraTokens+tokensInModel);
		result.append(traceMap.get(traceOption));
		result.append(searchMap.get(searchOption));
		switch(getModelReduction()){
		case AGGRESSIVE:
			result.append(" --reduction 1 ");
			if(reducedModelPath != null && !reducedModelPath.isEmpty()){
                result.append(" --write-reduced " +reducedModelPath);
            }

			break;
		case NO_REDUCTION:
			result.append(" --reduction 0 ");

			break;
		case BOUNDPRESERVING:
			result.append(" --reduction 2 ");
            if(reducedModelPath != null && !reducedModelPath.isEmpty()){
                result.append(" --write-reduced " +reducedModelPath);
            }
            break;
		default:
			break;			
		}

        if(unfold){
            String writeUnfoldedCMD = " --write-unfolded-net " +unfoldedModelPath + " --write-unfolded-queries " + unfoldedQueriesPath;
            result.append(writeUnfoldedCMD);
        }

		if (this.queryCategory == QueryCategory.CTL){
			result.append(" -ctl " + (getAlgorithmOption() == AlgorithmOption.CERTAIN_ZERO ? "czero" : "local"));
			result.append(" --xml-query 1");
		} else if (this.queryCategory == QueryCategory.LTL) {
            result.append(" -ltl");
            if (!this.useTarjan) {
                result.append(" ndfs");
            }
            result.append(" --xml-query 1");
        }
		if (this.useSiphontrap) {
			result.append(" -a 10 ");
		}
		if (this.queryReductionTime == QueryReductionTime.NoTime) {
			result.append(
			    " --query-reduction 0 ");
		} else if (this.queryReductionTime == QueryReductionTime.ShortestTime) {
		    //Run query reduction for 1 second, to avoid conflict with -s OverApprox argument, but also still not run the verification.
		    result.append(" --query-reduction 1");
        }
		if (!this.useStubbornReduction) {
			result.append(" --partial-order-reduction ");
		}
		if (this.useTarOption) {
		    result.append(" -tar ");
        }
		if (colored) {
            if (!this.partition) {
                result.append(" --disable-partitioning");
            }
            if (!this.colorFixpoint) {
                result.append(" --disable-cfp");
            }
            if (!symmetricVars) {
                result.append(" --disable-symmetry-vars");
            }
        }

		return result.toString();
	}
	
	public ModelReduction getModelReduction(){
		return modelReduction;
	}
	
	public AlgorithmOption getAlgorithmOption(){
		return this.algorithmOption;
	}
}
