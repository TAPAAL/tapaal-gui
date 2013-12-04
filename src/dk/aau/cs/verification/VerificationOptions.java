package dk.aau.cs.verification;

import pipe.dataLayer.TAPNQuery.SearchOption;
import pipe.dataLayer.TAPNQuery.TraceOption;
import pipe.gui.widgets.InclusionPlaces;

public interface VerificationOptions {
	// Probably need something like this in reality, but for now we dont need it

	// void setOption(String option, String value);
	// String getOption(String option);

	String toString();
	boolean useOverApproximation();
	int extraTokens();
	TraceOption traceOption();
	void setTraceOption(TraceOption option);
	SearchOption searchOption();
}
