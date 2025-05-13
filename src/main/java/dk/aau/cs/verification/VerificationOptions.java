package dk.aau.cs.verification;

import net.tapaal.gui.petrinet.verification.TAPNQuery.SearchOption;
import net.tapaal.gui.petrinet.verification.TAPNQuery.TraceOption;

public abstract class VerificationOptions {

	protected SearchOption searchOption;
	protected TraceOption traceOption;
	protected boolean enabledOverApproximation;
    protected boolean enabledUnderApproximation;
	protected int approximationDenominator;
	protected boolean useStateequationCheck;
	protected int extraTokens;

	protected String reducedModelPath;
    protected static String unfoldedModelPath;
    protected static String unfoldedQueriesPath;
    protected boolean isSimulate;
    protected boolean useExplicitSearch;

	public abstract String toString();

	public boolean enabledStateequationsCheck() {
		return useStateequationCheck;
	}

	public boolean enabledOverApproximation() {
		return enabledOverApproximation;
	}
	public boolean enabledUnderApproximation() {
		return enabledUnderApproximation;
	}
	public int approximationDenominator() {
		return approximationDenominator;
	}

	public int extraTokens() {
		return extraTokens;
	}
	public TraceOption traceOption() {
		return traceOption;
	}
	public void setTraceOption(TraceOption option) {
		traceOption = option;
	}

	public SearchOption searchOption() {
		return searchOption;
	}

    public String unfoldedModelPath(){
        return unfoldedModelPath;
    }
    public String unfoldedQueriesPath(){
        return unfoldedQueriesPath;
    }

    public boolean isSimulate(){
        return isSimulate;
    }

    public boolean useExplicitSearch() {
        return useExplicitSearch;
    }
}
