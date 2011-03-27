package pipe.gui;

import pipe.dataLayer.TAPNQuery.SearchOption;
import pipe.dataLayer.TAPNQuery.TraceOption;
import pipe.gui.widgets.RunningVerificationDialog;
import dk.aau.cs.Messenger;
import dk.aau.cs.TCTL.TCTLAbstractProperty;
import dk.aau.cs.TCTL.TCTLAtomicPropositionNode;
import dk.aau.cs.TCTL.TCTLEFNode;
import dk.aau.cs.model.tapn.TAPNQuery;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.translations.ReductionOption;
import dk.aau.cs.verification.ModelChecker;
import dk.aau.cs.verification.UPPAAL.VerifytaOptions;

public class KBoundAnalyzer {
	protected TimedArcPetriNetNetwork tapnNetwork;
	protected int k;

	private ModelChecker modelChecker;
	private Messenger messenger;

	public KBoundAnalyzer(TimedArcPetriNetNetwork tapnNetwork, int k,
			ModelChecker modelChecker, Messenger messenger) {
		this.k = k;
		this.tapnNetwork = tapnNetwork;
		this.modelChecker = modelChecker;
		this.messenger = messenger;
	}

	protected RunKBoundAnalysis getAnalyzer(ModelChecker modelChecker, Messenger messenger) {
		return new RunKBoundAnalysis(modelChecker, messenger);
	}

	public void analyze() {
		TAPNQuery query = getBoundednessQuery(tapnNetwork.marking().size());
		VerifytaOptions options = verificationOptions();

		RunKBoundAnalysis analyzer = getAnalyzer(modelChecker, messenger);
		RunningVerificationDialog dialog = new RunningVerificationDialog(CreateGui.getApp());
		dialog.setupListeners(analyzer);

		analyzer.execute(options, tapnNetwork, query);
		dialog.setVisible(true);
	}

	protected VerifytaOptions verificationOptions() {
		return new VerifytaOptions(TraceOption.NONE, SearchOption.BFS, false, ReductionOption.KBOUNDANALYSIS);
	}

	protected TAPNQuery getBoundednessQuery(int tokensInModel) {
		TCTLAbstractProperty property = null;

		property = new TCTLEFNode(new TCTLAtomicPropositionNode("_BOTTOM_", "=", 0));

		return new TAPNQuery(property, k + 1 + tokensInModel);
	}
}
