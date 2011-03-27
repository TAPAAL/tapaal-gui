package pipe.gui;

import javax.swing.JOptionPane;
import javax.swing.JSpinner;

import dk.aau.cs.Messenger;
import dk.aau.cs.model.tapn.simulation.TAPNNetworkTrace;
import dk.aau.cs.verification.ModelChecker;
import dk.aau.cs.verification.VerificationResult;

public class RunKBoundOptimization extends RunKBoundAnalysis {

	private JSpinner spinner;
	private int extraTokens;

	public RunKBoundOptimization(ModelChecker modelChecker,
			Messenger messenger, int extraTokens, JSpinner spinner) {
		super(modelChecker, messenger);
		this.extraTokens = extraTokens;
		this.spinner = spinner;
	}

	@Override
	protected void showResult(VerificationResult<TAPNNetworkTrace> result, long verificationTime) {
		if(result != null && !result.error()) {
			if (result.getQueryResult().integerResult() == extraTokens + 1) {
				JOptionPane.showMessageDialog(CreateGui.getApp(),
						getAnswerNotBoundedString(), "Analysis Result",
						JOptionPane.INFORMATION_MESSAGE);
			} else {
				spinner.setValue(result.getQueryResult().integerResult());
			}
		} else {
			String extraInformation = "";
			
			if (result != null && (result.errorMessage().contains("relocation") || result.errorMessage().toLowerCase().contains("internet connection is required for activation"))){
				
				extraInformation = "We have detected an error that often arises when UPPAAL is missing a valid Licence File.\n" +
						"Please open the UPPAAL GUI while connected to the internet, to correct this problem.";
				
			}
			
			String message = "An error occured during the verification." +
			System.getProperty("line.separator") + 	
			System.getProperty("line.separator");
			
			if (extraInformation != ""){
				message += extraInformation +			
				System.getProperty("line.separator") + 	
				System.getProperty("line.separator");
			}
			
			message += "UPPAAL output:\n" + result.errorMessage();
			
			messenger.displayWrappedErrorMessage(message,"Error during verification");
		}
	}

}
