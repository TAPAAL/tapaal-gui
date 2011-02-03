package pipe.gui;

import javax.swing.JOptionPane;
import javax.swing.JSpinner;

import dk.aau.cs.Messenger;
import dk.aau.cs.model.tapn.simulation.TapaalTrace;
import dk.aau.cs.verification.ModelChecker;
import dk.aau.cs.verification.VerificationResult;

public class RunKBoundOptimization extends RunKBoundAnalysis {

	private JSpinner spinner;
	private int extraTokens;
	
	public RunKBoundOptimization(ModelChecker modelChecker, Messenger messenger, int extraTokens, JSpinner spinner) {
		super(modelChecker, messenger);
		this.extraTokens = extraTokens;
		this.spinner = spinner;
	}

	@Override
	protected void showResult(VerificationResult<TapaalTrace> result, long verificationTime) {
		if(result.getQueryResult().integerResult() == extraTokens+1){
			JOptionPane.showMessageDialog(CreateGui.getApp(),
					getAnswerNotBoundedString(), 
					"Analysis Result", JOptionPane.INFORMATION_MESSAGE);
		}else{
			spinner.setValue(result.getQueryResult().integerResult());
		}
	}
	
}
