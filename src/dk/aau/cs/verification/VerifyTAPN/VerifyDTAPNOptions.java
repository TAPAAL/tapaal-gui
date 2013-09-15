package dk.aau.cs.verification.VerifyTAPN;

import pipe.dataLayer.TAPNQuery.ModelType;
import pipe.dataLayer.TAPNQuery.SearchOption;
import pipe.dataLayer.TAPNQuery.TraceOption;
import pipe.gui.widgets.InclusionPlaces;

public class VerifyDTAPNOptions extends VerifyTAPNOptions {
	
	private boolean timeDarts;
	private boolean pTrie;
	private ModelType modelType;
	private boolean strongSoundness;
	private boolean findMin;
	private boolean findMax;

	public VerifyDTAPNOptions(int extraTokens, TraceOption traceOption,
			SearchOption search, boolean symmetry, boolean timeDarts,
			boolean pTrie, ModelType modelType) {
		this(extraTokens, traceOption, search, symmetry, timeDarts, pTrie);
		this.modelType = modelType;
	}
	
	public VerifyDTAPNOptions(int extraTokens, TraceOption traceOption,
			SearchOption search, boolean symmetry, boolean timeDarts,
			boolean pTrie) {
		this(extraTokens, traceOption, search, symmetry, timeDarts, pTrie, false, new InclusionPlaces(), ModelType.TAPN, false, false, false);
	}

	public VerifyDTAPNOptions(int extraTokens, TraceOption traceOption,
			SearchOption search, boolean symmetry, boolean discreteInclusion,
			boolean timeDarts, boolean pTrie) {
		this(extraTokens, traceOption, search, symmetry,
				timeDarts, pTrie, discreteInclusion, new InclusionPlaces(), ModelType.TAPN, false, false, false);
	}

	public VerifyDTAPNOptions(int extraTokens, TraceOption traceOption,
			SearchOption search, boolean symmetry, boolean timeDarts,
			boolean pTrie, boolean discreteInclusion,
			InclusionPlaces inclusionPlaces, ModelType modelType, boolean strongSoundness, boolean findMin, boolean findMax) {
		super(extraTokens, traceOption, search, symmetry,
				discreteInclusion, inclusionPlaces);
		this.timeDarts = timeDarts;
		this.pTrie = pTrie;
		this.modelType = modelType;
		this.strongSoundness = strongSoundness;
		this.findMin = findMin;
		this.findMax = findMax;
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
		if(modelType == ModelType.TAWFN){
			result.append(" -w");
			if(strongSoundness)	result.append(" -ws");
			if(findMin)			result.append(" -wmin");
			if(findMax)			result.append(" -wmax");
		}
		return result.toString();
	}

	public boolean timeDarts() {
		return timeDarts;
	}
	
	public boolean pTrie() {
		return pTrie;
	}
	
}
