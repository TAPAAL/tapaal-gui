package pipe.gui;

import javax.swing.JSpinner;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.TAPNQuery.SearchOption;
import pipe.dataLayer.TAPNQuery.TraceOption;
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

	public KBoundOptimizer(DataLayer appModel, int k, ModelChecker modelChecker, JSpinner spinner)
	{
		super(appModel, k, modelChecker);
		this.spinner = spinner;
	}

	@Override
	protected RunKBoundAnalysis getAnalyzer(ModelChecker modelChecker) {
		return new RunKBoundOptimization(modelChecker, spinner);
	}
		
	@Override
	protected VerifytaOptions verificationOptions() {
		return new VerifytaOptions(TraceOption.NONE, SearchOption.BFS, false, ReductionOption.KBOUNDOPTMIZATION);
	}
}
