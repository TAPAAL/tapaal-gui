package dk.aau.cs.verification.VerifyTAPN;

import pipe.dataLayer.TAPNQuery.SearchOption;
import pipe.dataLayer.TAPNQuery.TraceOption;
import pipe.dataLayer.TAPNQuery.WorkflowMode;
import pipe.gui.widgets.InclusionPlaces;

public class VerifyDTAPNOptions extends VerifyTAPNOptions {
	
	private boolean timeDarts;
	private boolean pTrie;
	private WorkflowMode workflow;

	public VerifyDTAPNOptions(int extraTokens, TraceOption traceOption,
			SearchOption search, boolean symmetry, boolean timeDarts,
			boolean pTrie, WorkflowMode workflowMode) {
		this(extraTokens, traceOption, search, symmetry, timeDarts, pTrie);
		this.workflow = workflowMode;
	}
	
	public VerifyDTAPNOptions(int extraTokens, TraceOption traceOption,
			SearchOption search, boolean symmetry, boolean timeDarts,
			boolean pTrie) {
		this(extraTokens, traceOption, search, symmetry, timeDarts, pTrie, false, new InclusionPlaces(), WorkflowMode.NOT_WORKFLOW);
	}

	public VerifyDTAPNOptions(int extraTokens, TraceOption traceOption,
			SearchOption search, boolean symmetry, boolean discreteInclusion,
			boolean timeDarts, boolean pTrie) {
		this(extraTokens, traceOption, search, symmetry,
				timeDarts, pTrie, discreteInclusion, new InclusionPlaces(), WorkflowMode.NOT_WORKFLOW);
	}

	public VerifyDTAPNOptions(int extraTokens, TraceOption traceOption,
			SearchOption search, boolean symmetry, boolean timeDarts,
			boolean pTrie, boolean discreteInclusion,
			InclusionPlaces inclusionPlaces, WorkflowMode workflow) {
		super(extraTokens, traceOption, search, symmetry,
				discreteInclusion, inclusionPlaces);
		this.timeDarts = timeDarts;
		this.pTrie = pTrie;
		this.workflow = workflow;
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
		if(workflow == WorkflowMode.WORKFLOW_SOUNDNESS){
			result.append(" -w 1");
		}else if(workflow == WorkflowMode.WORKFLOW_STRONG_SOUNDNESS){
			result.append(" -w 2");
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
