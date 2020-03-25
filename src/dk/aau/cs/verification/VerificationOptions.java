package dk.aau.cs.verification;

import pipe.dataLayer.TAPNQuery.SearchOption;
import pipe.dataLayer.TAPNQuery.TraceOption;

public abstract class VerificationOptions {
	// Probably need something like this in reality, but for now we dont need it

	// void setOption(String option, String value);
	// String getOption(String option);
	protected SearchOption searchOption;
	protected TraceOption traceOption;
	protected boolean enableOverApproximation;
    protected boolean enableUnderApproximation;
	protected int approximationDenominator;
	protected boolean useOverApproximation;
	protected int extraTokens;


	public abstract String toString();

	public boolean enabledStateequationsCheck() {
		return useOverApproximation;
	}

	public boolean enabledOverApproximation() {
		return enableOverApproximation;
	}
	public boolean enabledUnderApproximation() {
		return enableUnderApproximation;
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
}
