package pipe.gui;

import javax.swing.JSpinner;

import pipe.dataLayer.DataLayer;
import dk.aau.cs.TA.NTA;
import dk.aau.cs.petrinet.TimedArcPetriNet;
import dk.aau.cs.translations.ModelTransformer;
import dk.aau.cs.translations.coloredtapn.ColoredDegree2BroadcastKBoundOptimizationTransformer;
import dk.aau.cs.translations.tapn.TAPNToNTASymmetryKBoundOptimizeTransformer;
import dk.aau.cs.verification.ModelChecker;

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
	protected ModelTransformer<TimedArcPetriNet, NTA> getReductionStrategy() {
		if(!appModel.isUsingColors()){
			return new TAPNToNTASymmetryKBoundOptimizeTransformer(k+1);
		}else{
			return new ColoredDegree2BroadcastKBoundOptimizationTransformer(k+1);
		}
	}
}
