package pipe.gui;

import javax.swing.JOptionPane;
import javax.swing.JSpinner;

import dk.aau.cs.TA.NTA;
import dk.aau.cs.TA.UPPAALQuery;
import dk.aau.cs.verification.ModelChecker;
import dk.aau.cs.verification.VerificationResult;

public class RunKBoundOptimization extends RunKBoundAnalysis {

	private JSpinner spinner;

	public RunKBoundOptimization(ModelChecker<NTA, UPPAALQuery> modelChecker, JSpinner spinner) {
		super(modelChecker);
		this.spinner = spinner;
	}

	@Override
	protected void showResult(VerificationResult result, long verificationTime) {
		if(result.isQuerySatisfied(0)){
			JOptionPane.showMessageDialog(CreateGui.getApp(),
					getAnswerNotBoundedString(), 
					"Analysis Result", JOptionPane.INFORMATION_MESSAGE);
		}else{
			spinner.setValue(result.getQueryResult(1).integerResult());
		}
	}
	
}
