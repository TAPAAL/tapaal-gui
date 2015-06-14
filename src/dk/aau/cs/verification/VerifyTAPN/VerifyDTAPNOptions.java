package dk.aau.cs.verification.VerifyTAPN;

import pipe.dataLayer.TAPNQuery.SearchOption;
import pipe.dataLayer.TAPNQuery.TraceOption;
import pipe.dataLayer.TAPNQuery.WorkflowMode;
import pipe.gui.widgets.InclusionPlaces;

public class VerifyDTAPNOptions extends VerifyTAPNOptions {
	
	private boolean gcd;
	private boolean timeDarts;
	private boolean pTrie;
	private WorkflowMode workflow;
        private long workflowbound;
	//only used for boundedness analysis
	private boolean dontUseDeadPlaces = false;

	public VerifyDTAPNOptions(int extraTokens, TraceOption traceOption,
			SearchOption search, boolean symmetry, boolean gcd, boolean timeDarts,
			boolean pTrie, WorkflowMode workflowMode,
			boolean enableOverApproximation, boolean enableUnderApproximation, int approximationDenominator) {
		this(extraTokens, traceOption, search, symmetry, gcd, timeDarts, pTrie, false, enableOverApproximation, enableUnderApproximation, approximationDenominator);
		this.workflow = workflowMode;
	}
	
	public VerifyDTAPNOptions(int extraTokens, TraceOption traceOption,
			SearchOption search, boolean symmetry, boolean gcd, boolean timeDarts,
			boolean pTrie, boolean useOverApproximation,
			boolean enableOverApproximation, boolean enableUnderApproximation, int approximationDenominator) {
		this(extraTokens, traceOption, search, symmetry, gcd, timeDarts, pTrie, useOverApproximation, false, new InclusionPlaces(), WorkflowMode.NOT_WORKFLOW, 0, enableOverApproximation, enableUnderApproximation, approximationDenominator);
	}
	
	//Only used for boundedness analysis
	public VerifyDTAPNOptions(boolean dontUseDeadPlaces, int extraTokens, TraceOption traceOption,
			SearchOption search, boolean symmetry, boolean timeDarts,
			boolean pTrie,
			boolean enableOverApproximation, boolean enableUnderApproximation, int approximationDenominator) {
		this(extraTokens, traceOption, search, symmetry, true, timeDarts, pTrie, false, false, new InclusionPlaces(), WorkflowMode.NOT_WORKFLOW, 0, enableOverApproximation, enableUnderApproximation, approximationDenominator);
		this.dontUseDeadPlaces = dontUseDeadPlaces;
	}

	public VerifyDTAPNOptions(int extraTokens, TraceOption traceOption,
			SearchOption search, boolean symmetry, boolean discreteInclusion, boolean gcd,
			boolean timeDarts, boolean pTrie, boolean useOverApproximation,
			boolean enableOverApproximation, boolean enableUnderApproximation, int approximationDenominator) {
		this(extraTokens, traceOption, search, symmetry, gcd, timeDarts, pTrie, useOverApproximation, discreteInclusion, new InclusionPlaces(), WorkflowMode.NOT_WORKFLOW, 0, enableOverApproximation, enableUnderApproximation, approximationDenominator);
	}

	public VerifyDTAPNOptions(int extraTokens, TraceOption traceOption,
			SearchOption search, boolean symmetry, boolean gcd, boolean timeDarts,
			boolean pTrie, boolean useOverApproximation, boolean discreteInclusion,
			InclusionPlaces inclusionPlaces, WorkflowMode workflow, long workflowbound,
			boolean enableOverApproximation, boolean enableUnderApproximation, int approximationDenominator) {
		super(extraTokens, traceOption, search, symmetry, useOverApproximation, discreteInclusion, inclusionPlaces, enableOverApproximation, enableUnderApproximation, approximationDenominator);
		this.timeDarts = timeDarts;
		this.pTrie = pTrie;
		this.workflow = workflow;
		this.gcd = gcd;
                this.workflowbound = workflowbound;
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
                        result.append(" -b ");
                        result.append(workflowbound);
                }
		result.append(' ');
		result.append(dontUseDeadPlaces ? "-d" : "");
		result.append(' ');
                if (workflow != WorkflowMode.WORKFLOW_SOUNDNESS && workflow != WorkflowMode.WORKFLOW_STRONG_SOUNDNESS) {
                    result.append(gcd ? "-c" : ""); // GCD optimization is not sound for workflow analysis
                }
		return result.toString();
	}

	public boolean timeDarts() {
		return timeDarts;
	}
	
	public boolean pTrie() {
		return pTrie;
	}
        
        public WorkflowMode getWorkflowMode(){
            return workflow;
        }
	
}
