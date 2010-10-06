package pipe.gui;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.TAPNQuery.SearchOption;
import pipe.dataLayer.TAPNQuery.TraceOption;
import pipe.gui.widgets.RunningVerificationDialog;
import dk.aau.cs.Messenger;
import dk.aau.cs.TA.NTA;
import dk.aau.cs.TCTL.TCTLAbstractProperty;
import dk.aau.cs.TCTL.TCTLAndNode;
import dk.aau.cs.TCTL.TCTLAtomicPropositionNode;
import dk.aau.cs.TCTL.TCTLEFNode;
import dk.aau.cs.petrinet.PipeTapnToAauTapnTransformer;
import dk.aau.cs.petrinet.TAPN;
import dk.aau.cs.petrinet.TAPNQuery;
import dk.aau.cs.petrinet.TimedArcPetriNet;
import dk.aau.cs.petrinet.colors.ColoredPipeTapnToColoredAauTapnTransformer;
import dk.aau.cs.translations.ModelTransformer;
import dk.aau.cs.translations.PipeToNTATransformer;
import dk.aau.cs.translations.ReductionOption;
import dk.aau.cs.translations.coloredtapn.ColoredDegree2BroadcastTransformer;
import dk.aau.cs.translations.tapn.TAPNToNTASymmetryTransformer;
import dk.aau.cs.verification.ModelChecker;
import dk.aau.cs.verification.UPPAAL.VerifytaOptions;

public class KBoundAnalyzer 
{
	protected DataLayer appModel;
	protected int k;

	private ModelTransformer<DataLayer, NTA> pipeToNtaTransformer;
	private Messenger messenger;
	private ModelChecker modelChecker;

	public KBoundAnalyzer(DataLayer appModel, int k, ModelChecker modelChecker)
	{
		this.k = k;
		this.appModel = appModel;
		this.modelChecker = modelChecker;
		this.pipeToNtaTransformer = new PipeToNTATransformer(getReductionStrategy());
	}

	protected RunKBoundAnalysis getAnalyzer(ModelChecker modelChecker) {
		return new RunKBoundAnalysis(modelChecker);
	}

	public void analyze()
	{
		TAPN model = convertModelToAAUTAPN(appModel);
		TAPNQuery query = getBoundednessQuery(model.getNumberOfTokens());
		VerifytaOptions options = new VerifytaOptions(TraceOption.NONE, SearchOption.BFS, false, ReductionOption.KBOUNDANALYSIS);

		RunKBoundAnalysis analyzer = getAnalyzer(modelChecker);
		RunningVerificationDialog dialog = new RunningVerificationDialog(CreateGui.getApp());	
		dialog.setupListeners(analyzer);

		analyzer.execute(options, model, query);
		dialog.setVisible(true);
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

	protected ModelTransformer<TimedArcPetriNet, NTA> getReductionStrategy() {
		if(!appModel.isUsingColors()){
			return new TAPNToNTASymmetryTransformer(k+1);
		}else{
			return new ColoredDegree2BroadcastTransformer(k+1, true);
		}
	}

	protected TAPNQuery getBoundednessQuery(int tokensInModel) {
		TCTLAbstractProperty property = null;

		if(!appModel.isUsingColors()){
			property = new TCTLEFNode(
					new TCTLAtomicPropositionNode("P_capacity", "=", 0)
			);		
		}else{
			//	buffer.append("E<>((sum(i:pid_t) Token(i).P_capacity) == 0) and (Control.P_lock == 1) and lock == 0\n");
		}

		return new TAPNQuery(property, k + tokensInModel);
	}
}
