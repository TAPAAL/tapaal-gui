package dk.aau.cs.verification.VerifyTAPN;

import pipe.dataLayer.TAPNQuery.SearchOption;
import pipe.dataLayer.TAPNQuery.TraceOption;
import pipe.dataLayer.TAPNQuery.WorkflowMode;
import pipe.gui.MessengerImpl;
import pipe.gui.widgets.InclusionPlaces;

import java.io.File;
import java.io.IOException;

public class VerifyDTAPNOptions extends VerifyTAPNOptions {
	
	private final boolean gcd;
	private final boolean timeDarts;
	private final boolean pTrie;
	private final WorkflowMode workflow;
	private final long workflowbound;
	//only used for boundedness analysis
	private boolean dontUseDeadPlaces = false;
	private boolean useStubbornReduction = true;
	private boolean partition;
	private boolean colorFixpoint;
	private String libQueryFilePath;
	
	//Only used for boundedness analysis
	public VerifyDTAPNOptions(
			boolean dontUseDeadPlaces,
			int extraTokens,
			TraceOption traceOption,
			SearchOption search,
			boolean symmetry,
			boolean timeDarts,
			boolean pTrie,
			boolean enableOverApproximation,
			boolean enableUnderApproximation,
			int approximationDenominator,
			boolean stubbornReduction,
            boolean partition,
            boolean colorFixpoint
	) {
		this(extraTokens, traceOption, search, symmetry, true, timeDarts, pTrie, false, false, new InclusionPlaces(), WorkflowMode.NOT_WORKFLOW, 0, enableOverApproximation, enableUnderApproximation, approximationDenominator, stubbornReduction, null, partition, colorFixpoint);
		this.dontUseDeadPlaces = dontUseDeadPlaces;
	}

	public VerifyDTAPNOptions(
			int extraTokens,
			TraceOption traceOption,
			SearchOption search,
			boolean symmetry,
			boolean gcd,
			boolean timeDarts,
			boolean pTrie,
			boolean useStateequationCheck,
			boolean discreteInclusion,
			InclusionPlaces inclusionPlaces,
			WorkflowMode workflow,
			long workflowbound,
			boolean enableOverApproximation,
			boolean enableUnderApproximation,
			int approximationDenominator,
			boolean stubbornReduction,
            String reducedModelPath,
            boolean partition,
            boolean colorFixpoint
	) {
		super(extraTokens, traceOption, search, symmetry, useStateequationCheck, discreteInclusion, inclusionPlaces, enableOverApproximation, enableUnderApproximation, approximationDenominator);
		this.timeDarts = timeDarts;
		this.pTrie = pTrie;
		this.workflow = workflow;
		this.gcd = gcd;
		this.workflowbound = workflowbound;
		this.useStubbornReduction = stubbornReduction;
		this.reducedModelPath = reducedModelPath;
		this.partition = partition;
		this.colorFixpoint = colorFixpoint;

        try {
            unfoldedModelPath = File.createTempFile("unfolded-", ".xml").getAbsolutePath();
            unfoldedQueriesPath = File.createTempFile("unfolded-", ".xml").getAbsolutePath();
            libQueryFilePath = File.createTempFile("tempLibQuery", ".q").getAbsolutePath();
        } catch (IOException e) {
            new MessengerImpl().displayErrorMessage(
                e.getMessage(),
                "Error");
            return;
        }
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder(super.toString());
		
		result.append(' ');
		result.append("-m ");
		result.append(timeDarts ? "1" : "0");
		result.append(' ');
		result.append("-p ");
		result.append(pTrie ? "1" : "0");
		if (! useStubbornReduction) {
			result.append(" -i");
		}
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

        result.append(" -q " + libQueryFilePath);
		result.append(" -q-xml " + unfoldedQueriesPath);
		result.append(" -f " + unfoldedModelPath);
        /* partitioning and color fixpoint is currently not available for timed nets
        if(!this.partition){
            result.append(" --disable-partitioning");
        }

        if(!this.colorFixpoint){
            result.append(" --disable-cfp");
        }*/

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
