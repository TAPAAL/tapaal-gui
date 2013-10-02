package dk.aau.cs.verification.VerifyTAPN;

import pipe.dataLayer.TAPNQuery.SearchOption;
import pipe.dataLayer.TAPNQuery.TraceOption;
import pipe.gui.widgets.InclusionPlaces;

public class VerifyDTAPNOptions extends VerifyTAPNOptions {
	
	private boolean timeDarts;
	private boolean pTrie;
	
	//only used for boundedness analysis
	private boolean dontUseDeadPlaces = false;

	public VerifyDTAPNOptions(int extraTokens, TraceOption traceOption,
			SearchOption search, boolean symmetry, boolean timeDarts,
			boolean pTrie, boolean useOverApproximation) {
		this(extraTokens, traceOption, search, symmetry, timeDarts, pTrie, useOverApproximation, false, new InclusionPlaces());
	}
	
	//Only used for boundedness analysis
	public VerifyDTAPNOptions(boolean dontUseDeadPlaces, int extraTokens, TraceOption traceOption,
			SearchOption search, boolean symmetry, boolean timeDarts,
			boolean pTrie) {
		this(extraTokens, traceOption, search, symmetry, timeDarts, pTrie, false, false, new InclusionPlaces());
		this.dontUseDeadPlaces = dontUseDeadPlaces;
	}

	public VerifyDTAPNOptions(int extraTokens, TraceOption traceOption,
			SearchOption search, boolean symmetry, boolean discreteInclusion,
			boolean timeDarts, boolean pTrie, boolean useOverApproximation) {
		this(extraTokens, traceOption, search, symmetry,
				timeDarts, pTrie, useOverApproximation, discreteInclusion, new InclusionPlaces());
	}

	public VerifyDTAPNOptions(int extraTokens, TraceOption traceOption,
			SearchOption search, boolean symmetry, boolean timeDarts,
			boolean pTrie, boolean useOverApproximation, boolean discreteInclusion,
			InclusionPlaces inclusionPlaces) {
		super(extraTokens, traceOption, search, symmetry, useOverApproximation,
				discreteInclusion, inclusionPlaces);
		this.timeDarts = timeDarts;
		this.pTrie = pTrie;
	}
	
	@Override
	public String toString() {
		StringBuffer result = new StringBuffer(super.toString());
		
		result.append(' ');
		result.append("-m ");
		result.append(timeDarts ? "1" : "0");
		result.append(' ');
		result.append("-p ");
		result.append(pTrie ? "1" : "0");
		result.append(' ');
		result.append(dontUseDeadPlaces ? "-d" : "");
		return result.toString();
	}

	public boolean timeDarts() {
		return timeDarts;
	}
	
	public boolean pTrie() {
		return pTrie;
	}
	
}
