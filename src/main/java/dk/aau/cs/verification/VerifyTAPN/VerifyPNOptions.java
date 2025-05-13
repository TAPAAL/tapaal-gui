package dk.aau.cs.verification.VerifyTAPN;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.sun.jna.Platform;

import net.tapaal.gui.petrinet.verification.TAPNQuery.SearchOption;
import net.tapaal.gui.petrinet.verification.TAPNQuery.QueryReductionTime;
import net.tapaal.gui.petrinet.verification.TAPNQuery.TraceOption;
import net.tapaal.gui.petrinet.verification.TAPNQuery.AlgorithmOption;
import net.tapaal.gui.petrinet.verification.TAPNQuery.QueryCategory;
import pipe.gui.MessengerImpl;
import net.tapaal.gui.petrinet.verification.InclusionPlaces;

public class VerifyPNOptions extends VerifyTAPNOptions{
	private static final Map<TraceOption, String> traceMap = Map.of(
        TraceOption.SOME, " --trace ",
		TraceOption.NONE, ""
    );
	private static final Map<SearchOption, String> searchMap = Map.of(
        SearchOption.HEURISTIC, " --search-strategy BestFS",
        SearchOption.RANDOMHEURISTIC, " --search-strategy RPFS",
        SearchOption.BFS, "--search-strategy BFS",
        SearchOption.DFS, " --search-strategy DFS",
        SearchOption.RANDOM, " --search-strategy RDFS",
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
    private final boolean useColoredReduction;

    private boolean useRawVerification;
    private String rawVerificationOptions;

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
        boolean useSymmetricVars,
        boolean useColoredReduction,
        boolean useExplicitSearch,
        boolean useRawVerification,
        String rawVerificationOptions
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
		this.useColoredReduction = useColoredReduction;
        this.useExplicitSearch = useExplicitSearch;
        this.useRawVerification = useRawVerification;
        this.rawVerificationOptions = rawVerificationOptions;

        if (useExplicitSearch) {
                unfoldedModelPath = null;
                unfoldedQueriesPath = null;
        } else if (unfold && !useRawVerification) {
            try {
                if (Platform.isWindows()) {
                    unfoldedModelPath = "\"" + File.createTempFile("unfolded-", ".pnml").getAbsolutePath() + "\"";
                    unfoldedQueriesPath = "\"" + File.createTempFile("unfoldedQueries-", ".xml").getAbsolutePath() + "\"";
                } else {
                    unfoldedModelPath = File.createTempFile("unfolded-", ".pnml").getAbsolutePath();
                    unfoldedQueriesPath = File.createTempFile("unfoldedQueries-", ".xml").getAbsolutePath();
                }
            } catch (IOException e) {
                new MessengerImpl().displayErrorMessage(e.getMessage(), "Error");
            }
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
        boolean unfold,
        boolean partition,
        boolean colorFixpoint,
        boolean useSymmetricVars
    ) {
        this(extraTokens, traceOption, search, useOverApproximation, modelReduction, enableOverApproximation, enableUnderApproximation, approximationDenominator,queryCategory, algorithmOption, siphontrap, queryReduction, stubbornReduction, pathToReducedNet, useTarOption, useTarjan, colored, false, partition, colorFixpoint, useSymmetricVars, false, true, false, null);
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
        boolean unfold,
        boolean partition,
        boolean colorFixpoint,
        boolean useSymmetricVars,
        boolean useRawVerification,
        String rawVerificationOptions
    ) {
        this(extraTokens, traceOption, search, useOverApproximation, modelReduction, enableOverApproximation, enableUnderApproximation, approximationDenominator,queryCategory, algorithmOption, siphontrap, queryReduction, stubbornReduction, pathToReducedNet, useTarOption, useTarjan, colored, false, partition, colorFixpoint, useSymmetricVars, false, false, useRawVerification, rawVerificationOptions);
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

    @Override
	public String toString() {
		StringBuilder result = new StringBuilder();
    
        if (useRawVerification && rawVerificationOptions != null) {
			return rawVerificationString(rawVerificationOptions, traceMap.get(traceOption));
		}

		result.append(kBoundArg());

        var traceSwitch =traceMap.get(traceOption) ;
        if (traceSwitch != null) {
            result.append(traceSwitch + " ");
        }

        var searchSwitch = searchMap.get(searchOption);
        if (searchSwitch != null) {
            result.append(searchSwitch + " ");
        }

		switch(getModelReduction()){
		case AGGRESSIVE:
			result.append(" --reduction 1 ");
			if(reducedModelPath != null && !reducedModelPath.isEmpty()){
                result.append(" --write-reduced " + reducedModelPath);
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

        if (unfold) {
            result.append(" --write-unfolded-net ");
            result.append(unfoldedModelPath);
            result.append(" --write-unfolded-queries ");
            result.append(unfoldedQueriesPath);
            result.append(" --bindings ");
        }

		if (this.queryCategory == QueryCategory.CTL){
			result.append(" --ctl-algorithm " + (getAlgorithmOption() == AlgorithmOption.CERTAIN_ZERO ? "czero" : "local"));
			result.append(" --xml-queries 1");
		} else if (this.queryCategory == QueryCategory.LTL || this.queryCategory == QueryCategory.HyperLTL) {
            result.append(" --ltl-algorithm");
            if (!this.useTarjan) {
                result.append(" ndfs");
            }
            result.append(" --xml-queries 1");
        }
		if (this.useSiphontrap) {
			result.append(" --siphon-trap 10 ");
		}
		if (this.queryReductionTime == QueryReductionTime.NoTime) {
			result.append(
			    " --query-reduction 0 ");
		} else if (this.queryReductionTime == QueryReductionTime.ShortestTime) {
		    //Run query reduction for 1 second, to avoid conflict with -s OverApprox argument, but also still not run the verification.
		    result.append(" --query-reduction 1 ");
        }
		if (!this.useStubbornReduction) {
			result.append(" --disable-partial-order ");
		}
		if (this.useTarOption) {
		    result.append(" --trace-abstraction ");
        }

		if (colored) {
            if (!this.partition) {
                result.append(" --disable-partitioning ");
            }
            if (!this.colorFixpoint) {
                result.append(" --disable-cfp ");
            }
            if (!symmetricVars) {
                result.append(" --disable-symmetry-vars ");
            }
            if (useExplicitSearch) {
                result.append(" -C ");
            }
        }
		if (!useColoredReduction && colored) {
		    result.append(" --col-reduction 0 ");
        }

		return result.toString();
	}
	
	public ModelReduction getModelReduction(){
		return modelReduction;
	}
	
	public AlgorithmOption getAlgorithmOption(){
		return this.algorithmOption;
	}

    public boolean useExplicitSearch() {
        return useExplicitSearch;
    }
}
