package pipe.gui;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.TAPNQuery.SearchOption;
import pipe.dataLayer.TAPNQuery.TraceOption;
import pipe.gui.widgets.RunningVerificationDialog;
import dk.aau.cs.Messenger;
import dk.aau.cs.TA.NTA;
import dk.aau.cs.TA.StandardUPPAALQuery;
import dk.aau.cs.TA.UPPAALQuery;
import dk.aau.cs.petrinet.TimedArcPetriNet;
import dk.aau.cs.translations.ModelTransformer;
import dk.aau.cs.translations.PipeToNTATransformer;
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
	private ModelChecker<NTA, UPPAALQuery> modelChecker;
	
	public KBoundAnalyzer(DataLayer appModel, int k, ModelChecker<NTA, UPPAALQuery> modelChecker)
	{
		this.k = k;
		this.appModel = appModel;
		this.modelChecker = modelChecker;
		this.pipeToNtaTransformer = new PipeToNTATransformer(getReductionStrategy());
	}

	protected RunKBoundAnalysis getAnalyzer(ModelChecker<NTA, UPPAALQuery> modelChecker) {
		return new RunKBoundAnalysis(modelChecker);
	}

	public void analyze()
	{
		NTA nta;
		try {
			nta = pipeToNtaTransformer.transformModel(appModel);
		} catch (Exception e1) {
			messenger.displayErrorMessage("Something went wrong while translating the model.");
			return;
		}

		UPPAALQuery[] queries = getQueries();
		VerifytaOptions options = new VerifytaOptions(TraceOption.NONE, SearchOption.BFS, false);

		RunKBoundAnalysis analyzer = getAnalyzer(modelChecker);
		RunningVerificationDialog dialog = new RunningVerificationDialog(CreateGui.getApp());	
		dialog.setupListeners(analyzer);
		
		analyzer.execute(options, nta, queries);
		dialog.setVisible(true);
	}

	protected ModelTransformer<TimedArcPetriNet, NTA> getReductionStrategy() {
		if(!appModel.isUsingColors()){
			return new TAPNToNTASymmetryTransformer(k+1);
		}else{
			return new ColoredDegree2BroadcastTransformer(k+1, true);
		}
	}

	protected UPPAALQuery[] getQueries() {
		return new UPPAALQuery[] { getBoundednessQuery() };
	}

	protected UPPAALQuery getBoundednessQuery() {
		StringBuffer buffer = new StringBuffer();

		if(!appModel.isUsingColors()){
			//stream.println("A[]((sum(i:pid_t) P(i).P_capacity)>= 1) and (Control.finish == 1)");
			buffer.append("E<>((sum(i:pid_t) Token(i).P_capacity)== 0) and (Control.finish == 1)\n");
		}else{
			buffer.append("E<>((sum(i:pid_t) Token(i).P_capacity) == 0) and (Control.P_lock == 1) and lock == 0\n");
		}

		UPPAALQuery boundednessQuery = new StandardUPPAALQuery(buffer.toString());
		return boundednessQuery;
	}
}
