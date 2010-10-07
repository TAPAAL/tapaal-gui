package pipe.gui;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.TAPNQuery.SearchOption;
import pipe.dataLayer.TAPNQuery.TraceOption;
import pipe.gui.widgets.RunningVerificationDialog;
import dk.aau.cs.TCTL.TCTLAbstractProperty;
import dk.aau.cs.TCTL.TCTLAtomicPropositionNode;
import dk.aau.cs.TCTL.TCTLEFNode;
import dk.aau.cs.petrinet.PipeTapnToAauTapnTransformer;
import dk.aau.cs.petrinet.TAPN;
import dk.aau.cs.petrinet.TAPNQuery;
import dk.aau.cs.petrinet.colors.ColoredPipeTapnToColoredAauTapnTransformer;
import dk.aau.cs.translations.ReductionOption;
import dk.aau.cs.verification.ModelChecker;
import dk.aau.cs.verification.UPPAAL.VerifytaOptions;

public class KBoundAnalyzer 
{
	protected DataLayer appModel;
	protected int k;

	private ModelChecker modelChecker;

	public KBoundAnalyzer(DataLayer appModel, int k, ModelChecker modelChecker)
	{
		this.k = k;
		this.appModel = appModel;
		this.modelChecker = modelChecker;
	}

	protected RunKBoundAnalysis getAnalyzer(ModelChecker modelChecker) {
		return new RunKBoundAnalysis(modelChecker);
	}

	public void analyze()
	{
		TAPN model = convertModelToAAUTAPN(appModel);
		TAPNQuery query = getBoundednessQuery(model.getNumberOfTokens());
		VerifytaOptions options = verificationOptions();

		RunKBoundAnalysis analyzer = getAnalyzer(modelChecker);
		RunningVerificationDialog dialog = new RunningVerificationDialog(CreateGui.getApp());	
		dialog.setupListeners(analyzer);

		analyzer.execute(options, model, query);
		dialog.setVisible(true);
	}

	protected VerifytaOptions verificationOptions() {
		return new VerifytaOptions(TraceOption.NONE, SearchOption.BFS, false, ReductionOption.KBOUNDANALYSIS);
	}

	private TAPN convertModelToAAUTAPN(DataLayer appModel) {
		PipeTapnToAauTapnTransformer transformer = appModel.isUsingColors() ? new ColoredPipeTapnToColoredAauTapnTransformer() : new PipeTapnToAauTapnTransformer();

		TAPN model=null;
		try {
			model = transformer.getAAUTAPN(appModel, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return model;
	}

	protected TAPNQuery getBoundednessQuery(int tokensInModel) {
		TCTLAbstractProperty property = null;

		property = new TCTLEFNode(
				new TCTLAtomicPropositionNode("P_capacity", "=", 0)
		);		


		return new TAPNQuery(property, k + 1 + tokensInModel);
	}
}
