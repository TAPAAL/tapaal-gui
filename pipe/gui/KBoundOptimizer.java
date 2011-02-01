package pipe.gui;

import javax.swing.JSpinner;

import pipe.dataLayer.TAPNQuery.SearchOption;
import pipe.dataLayer.TAPNQuery.TraceOption;
import dk.aau.cs.Messenger;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.translations.ReductionOption;
import dk.aau.cs.verification.ModelChecker;
import dk.aau.cs.verification.UPPAAL.VerifytaOptions;

public class KBoundOptimizer extends KBoundAnalyzer {

	private int minBound = -1;
	private JSpinner spinner;
	public int getMinBound()
	{
		return minBound;
	}

	public KBoundOptimizer(TimedArcPetriNetNetwork tapnNetwork, int k, ModelChecker modelChecker, Messenger messenger, JSpinner spinner)
	{
		super(tapnNetwork, k, modelChecker, messenger);
		this.spinner = spinner;
	}

	@Override
	protected RunKBoundAnalysis getAnalyzer(ModelChecker modelChecker, Messenger messenger) {
		return new RunKBoundOptimization(modelChecker, messenger, super.k, spinner);
	}
		
	@Override
	protected VerifytaOptions verificationOptions() {
		return new VerifytaOptions(TraceOption.NONE, SearchOption.BFS, false, ReductionOption.KBOUNDOPTMIZATION);
	}
}
