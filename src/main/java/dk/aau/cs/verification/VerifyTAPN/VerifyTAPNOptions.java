package dk.aau.cs.verification.VerifyTAPN;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.tapaal.gui.petrinet.verification.TAPNQuery.SearchOption;
import net.tapaal.gui.petrinet.verification.TAPNQuery.TraceOption;
import net.tapaal.gui.petrinet.verification.InclusionPlaces;
import net.tapaal.gui.petrinet.verification.InclusionPlaces.InclusionPlacesOption;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.util.Require;
import dk.aau.cs.verification.VerificationOptions;
import pipe.gui.MessengerImpl;

public class VerifyTAPNOptions extends VerificationOptions{

	protected int tokensInModel;
	private final boolean symmetry;
	private final boolean discreteInclusion;
    private final boolean tarOption;
	private InclusionPlaces inclusionPlaces;
	private boolean useRawVerification;
	private String rawVerificationOptions;
	
	//only used for boundedness analysis
	private final boolean dontUseDeadPlaces = false;

	private static final Map<TraceOption, String> traceMap = createTraceOptionsMap();
	private static final Map<SearchOption, String> searchMap = createSearchOptionsMap();

	public VerifyTAPNOptions(int extraTokens, TraceOption traceOption, SearchOption search, boolean symmetry, boolean useStateequationCheck, boolean discreteInclusion, boolean enableOverApproximation, boolean enableUnderApproximation, int approximationDenominator, boolean useRawVerification, String rawVerificationOptions) {
		this(extraTokens,traceOption, search, symmetry, useStateequationCheck, discreteInclusion, new InclusionPlaces(), enableOverApproximation, enableUnderApproximation, approximationDenominator);
		this.rawVerificationOptions = rawVerificationOptions;
		this.useRawVerification = useRawVerification;
	}

    public VerifyTAPNOptions(int extraTokens, TraceOption traceOption, SearchOption search, boolean symmetry, boolean useStateequationCheck, boolean discreteInclusion, InclusionPlaces inclusionPlaces, boolean enableOverApproximation, boolean enableUnderApproximation, int approximationDenominator) {
        this(extraTokens,traceOption, search, symmetry, useStateequationCheck, discreteInclusion, inclusionPlaces, enableOverApproximation, enableUnderApproximation, approximationDenominator, false);
    }

    public VerifyTAPNOptions(int extraTokens, TraceOption traceOption, SearchOption search, boolean symmetry, boolean useStateequationCheck, boolean discreteInclusion, InclusionPlaces inclusionPlaces, boolean enableOverApproximation, boolean enableUnderApproximation, int approximationDenominator, boolean tarOption, boolean isColor, boolean useRawVerification, String rawVerificationOptions) {
        this(extraTokens,traceOption, search, symmetry, useStateequationCheck, discreteInclusion, inclusionPlaces, enableOverApproximation, enableUnderApproximation, approximationDenominator, tarOption);

		this.useRawVerification = useRawVerification;
		this.rawVerificationOptions = rawVerificationOptions;

        if(isColor && trace() != TraceOption.NONE && !useRawVerification) // we only force unfolding when traces are involved
        {
            try {
                unfoldedModelPath = File.createTempFile("unfolded-", ".pnml").getAbsolutePath();
                unfoldedQueriesPath = File.createTempFile("unfoldedQueries-", ".xml").getAbsolutePath();
            } catch (IOException e) {
                new MessengerImpl().displayErrorMessage(e.getMessage(), "Error");
            }
        }
    }

	public VerifyTAPNOptions(int extraTokens, TraceOption traceOption, SearchOption search, boolean symmetry, boolean useStateEquationCheck, boolean discreteInclusion, InclusionPlaces inclusionPlaces, boolean enableOverApproximation, boolean enableUnderApproximation, int approximationDenominator, boolean tarOption) {
		this.extraTokens = extraTokens;
		this.traceOption = traceOption;
		searchOption = search;
		this.symmetry = symmetry;
		this.discreteInclusion = discreteInclusion;
		this.useStateequationCheck = useStateEquationCheck;
		this.inclusionPlaces = inclusionPlaces;
		this.enabledOverApproximation = enableOverApproximation;
		this.enabledUnderApproximation = enableUnderApproximation;
		this.approximationDenominator = approximationDenominator;
        this.tarOption = tarOption;
	}

	public TraceOption trace() {
		return traceOption;
	}
	
	public boolean symmetry() {
		return symmetry;
	}
	
	public boolean discreteInclusion(){
		return discreteInclusion;
	}
	
	public void setTokensInModel(int tokens){ // TODO: Get rid of this method when verifytapn refactored
		tokensInModel = tokens;
	}

    public String kBoundArg() {
        return " --k-bound " + (extraTokens+tokensInModel) + " ";
    }

    public String deadTokenArg() {
        return dontUseDeadPlaces ? " --keep-dead-tokens " : "";
    }

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		
		if (useRawVerification) {
            return result.append(rawVerificationOptions).toString();
        }

        if(unfoldedModelPath != null && unfoldedQueriesPath != null)
        {
            result.append(" --write-unfolded-net ");
            result.append(unfoldedModelPath);
            result.append(" --write-unfolded-queries ");
            result.append(unfoldedQueriesPath);
            result.append(" ");
        }

		result.append(kBoundArg());
        result.append(deadTokenArg());
		result.append(traceMap.get(traceOption));
		result.append(' ');
		result.append(searchMap.get(searchOption));
		result.append(' ');
		result.append(symmetry ? "" : "--disable-symmetry"); // symmetry is on by default in verifyTAPN so "-s" disables it
		result.append(' ');
		result.append(discreteInclusion ? " --inclusion-check 1" : "");
		result.append(discreteInclusion ? " --inclusion-places " + generateDiscretePlacesList() : "");
		return result.toString();
	}

	private String generateDiscretePlacesList() {
		if(inclusionPlaces.inclusionOption() == InclusionPlacesOption.AllPlaces) return "*ALL*";
		if(inclusionPlaces.inclusionPlaces().isEmpty()) return "*NONE*";
		
		StringBuilder s = new StringBuilder();
		boolean first = true;
		for(TimedPlace p : inclusionPlaces.inclusionPlaces()) {
			if(!first) s.append(',');
			
			s.append(p.name());
			if(first) first = false;
		}
		
		return s.toString();
	}

	public static Map<TraceOption, String> createTraceOptionsMap() {
		HashMap<TraceOption, String> map = new HashMap<TraceOption, String>();
		map.put(TraceOption.SOME, "--trace 1 ");
		map.put(TraceOption.FASTEST, "--trace 2 ");
		map.put(TraceOption.NONE, "");

		return map;
	}

	private static Map<SearchOption, String> createSearchOptionsMap() {
		HashMap<SearchOption, String> map = new HashMap<SearchOption, String>();
		map.put(SearchOption.BFS, "--search-strategy BFS");
		map.put(SearchOption.DFS, "--search-strategy DFS");
		map.put(SearchOption.RANDOM, "--search-strategy RDFS");
		map.put(SearchOption.HEURISTIC, "--search-strategy MAX-COVER");
		map.put(SearchOption.DEFAULT, "");
		return map;
	}

	public InclusionPlaces inclusionPlaces() {
		return inclusionPlaces;
	}
	
	public void setInclusionPlaces(InclusionPlaces inclusionPlaces) {
		Require.that(inclusionPlaces != null, "Inclusion places cannot be null");
		
		this.inclusionPlaces = inclusionPlaces;
	}

}
