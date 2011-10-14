package pipe.gui;

import javax.swing.JSpinner;

import pipe.dataLayer.TAPNQuery.SearchOption;
import pipe.dataLayer.TAPNQuery.TraceOption;
import pipe.gui.widgets.RunningVerificationDialog;
import dk.aau.cs.Messenger;
import dk.aau.cs.TCTL.TCTLAGNode;
import dk.aau.cs.TCTL.TCTLAbstractProperty;
import dk.aau.cs.TCTL.TCTLTrueNode;
import dk.aau.cs.model.tapn.TAPNQuery;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.verification.ModelChecker;
import dk.aau.cs.verification.VerifyTAPN.VerifyTAPNOptions;

public class KBoundAnalyzer {
	protected TimedArcPetriNetNetwork tapnNetwork;
	protected int k;

	private ModelChecker modelChecker;
	private Messenger messenger;
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
		RunningVerificationDialog dialog = new RunningVerificationDialog(CreateGui.getApp());
		dialog.setupListeners(analyzer);

		analyzer.execute(options, tapnNetwork, query);
		dialog.setVisible(true);
	}

	protected VerifyTAPNOptions verificationOptions() {
		return new VerifyTAPNOptions(k, TraceOption.NONE, SearchOption.BFS, true);
	}

	protected TAPNQuery getBoundednessQuery() {
		TCTLAbstractProperty property = null;

		property = new TCTLAGNode(new TCTLTrueNode());

		return new TAPNQuery(property, k);
	}
}
