package dk.aau.cs.verification;

import pipe.dataLayer.TAPNQuery.SearchOption;
import pipe.dataLayer.TAPNQuery.TraceOption;

public abstract class VerificationOptions {
	// Probably need something like this in reality, but for now we dont need it

	// void setOption(String option, String value);
	// String getOption(String option);
	protected SearchOption searchOption;
	protected TraceOption traceOption;
	protected boolean enabledOverApproximation;
    protected boolean enabledUnderApproximation;
	protected int approximationDenominator;
	protected boolean useStateequationCheck;
	protected int extraTokens;


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
}
