package pipe.gui;

import javax.swing.JSpinner;

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

public class KBoundAnalyzer {
	protected TimedArcPetriNetNetwork tapnNetwork;
	protected int k;

	private final ModelChecker modelChecker;
	private final Messenger messenger;
	private final JSpinner spinner;

	public KBoundAnalyzer(TimedArcPetriNetNetwork tapnNetwork, int k,
			ModelChecker modelChecker, Messenger messenger, JSpinner tokensControl) {
		this.k = k;
		this.tapnNetwork = tapnNetwork;
		this.modelChecker = modelChecker;
		this.messenger = messenger;
		spinner = tokensControl;
	}

	public void analyze() {
		TAPNQuery query = getBoundednessQuery();
		VerifyTAPNOptions options = verificationOptions();

		RunKBoundAnalysis analyzer = new RunKBoundAnalysis(modelChecker, messenger, spinner);
		RunningVerificationDialog dialog = new RunningVerificationDialog(CreateGui.getApp(), analyzer);

		analyzer.execute(options, tapnNetwork, query, null);
		dialog.setVisible(true);
	}

	protected VerifyTAPNOptions verificationOptions() {
		if(modelChecker instanceof VerifyPN){
			return new VerifyPNOptions(k, TraceOption.NONE, SearchOption.BFS, false, ModelReduction.BOUNDPRESERVING, false, false, 1, QueryCategory.Default, AlgorithmOption.CERTAIN_ZERO, false, false, false, null);
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
