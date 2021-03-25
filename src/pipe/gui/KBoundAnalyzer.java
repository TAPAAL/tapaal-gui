package pipe.gui;

import javax.swing.JSpinner;

import dk.aau.cs.model.tapn.TimedArcPetriNet;
import pipe.dataLayer.DataLayer;
import pipe.dataLayer.TAPNQuery.SearchOption;
import pipe.dataLayer.TAPNQuery.TraceOption;
import pipe.dataLayer.TAPNQuery.AlgorithmOption;
import pipe.dataLayer.TAPNQuery.QueryCategory;
import pipe.gui.widgets.RunningVerificationDialog;
import dk.aau.cs.Messenger;
import dk.aau.cs.TCTL.TCTLAGNode;
import dk.aau.cs.TCTL.TCTLAbstractProperty;
import dk.aau.cs.TCTL.TCTLTrueNode;
import dk.aau.cs.model.tapn.TAPNQuery;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.verification.ModelChecker;
import dk.aau.cs.verification.VerifyTAPN.ModelReduction;
import dk.aau.cs.verification.VerifyTAPN.VerifyDTAPNOptions;
import dk.aau.cs.verification.VerifyTAPN.VerifyPN;
import dk.aau.cs.verification.VerifyTAPN.VerifyPNOptions;
import dk.aau.cs.verification.VerifyTAPN.VerifyTAPN;
import dk.aau.cs.verification.VerifyTAPN.VerifyTAPNDiscreteVerification;
import dk.aau.cs.verification.VerifyTAPN.VerifyTAPNOptions;

import java.util.HashMap;

public class KBoundAnalyzer {
	protected TimedArcPetriNetNetwork tapnNetwork;
	protected int k;

	private final ModelChecker modelChecker;
	private final ModelChecker unfoldingEngine;
	private final Messenger messenger;
	private final JSpinner spinner;
	private final HashMap<TimedArcPetriNet, DataLayer> guiModels;

	public KBoundAnalyzer(TimedArcPetriNetNetwork tapnNetwork, int k, ModelChecker modelChecker,
                          ModelChecker unfoldingEngine, Messenger messenger, JSpinner tokensControl, HashMap<TimedArcPetriNet, DataLayer> guiModels) {
		this.k = k;
		this.tapnNetwork = tapnNetwork;
		this.modelChecker = modelChecker;
        this.unfoldingEngine = unfoldingEngine;
        this.messenger = messenger;
        this.guiModels = guiModels;
		spinner = tokensControl;
	}

	public void analyze() {
		TAPNQuery query = getBoundednessQuery();
		VerifyTAPNOptions options = verificationOptions();

		RunKBoundAnalysis analyzer = new RunKBoundAnalysis(modelChecker, unfoldingEngine, messenger, spinner, guiModels);
		RunningVerificationDialog dialog = new RunningVerificationDialog(CreateGui.getApp(), analyzer);

		analyzer.execute(options, tapnNetwork, query, null);
		dialog.setVisible(true);
	}

	protected VerifyTAPNOptions verificationOptions() {
		if(modelChecker instanceof VerifyPN){
			return new VerifyPNOptions(k, TraceOption.NONE, SearchOption.BFS, false, ModelReduction.BOUNDPRESERVING, false, false, 1, QueryCategory.Default, AlgorithmOption.CERTAIN_ZERO, false, pipe.dataLayer.TAPNQuery.QueryReductionTime.UnlimitedTime,false, tapnNetwork.isColored(), null, false);
		} else if(modelChecker instanceof VerifyTAPN){
			return new VerifyTAPNOptions(k, TraceOption.NONE, SearchOption.BFS, true, false, true, false, false, 1);
		} else if(modelChecker instanceof VerifyTAPNDiscreteVerification){
			return new VerifyDTAPNOptions(true, k, TraceOption.NONE, SearchOption.BFS, true, !tapnNetwork.hasUrgentTransitions(), true, false, false, 1, false);
		}
		return null;
	}

	protected TAPNQuery getBoundednessQuery() {
		TCTLAbstractProperty property = null;

		property = new TCTLAGNode(new TCTLTrueNode());

		return new TAPNQuery(property, k);
	}
}
