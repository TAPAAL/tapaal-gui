package pipe.gui;

import javax.swing.JOptionPane;
import javax.swing.JSpinner;

import dk.aau.cs.Messenger;
import dk.aau.cs.io.ResourceManager;
import dk.aau.cs.model.tapn.simulation.TAPNNetworkTrace;
import dk.aau.cs.verification.Boundedness;
import dk.aau.cs.verification.ModelChecker;
import dk.aau.cs.verification.VerificationResult;

public class RunKBoundAnalysis extends RunVerificationBase {

	private final JSpinner spinner;

	public RunKBoundAnalysis(ModelChecker modelChecker, Messenger messenger,JSpinner spinner) {
		super(modelChecker, messenger, null);
		this.spinner = spinner;
	}

	@Override
	protected void showResult(VerificationResult<TAPNNetworkTrace> result) {
		if(result != null && !result.error()) {
			if (!result.getQueryResult().boundednessAnalysis().boundednessResult().equals(Boundedness.Bounded)) {
				JOptionPane.showMessageDialog(CreateGui.getApp(),
						getAnswerNotBoundedString(), "Analysis Result",
						JOptionPane.INFORMATION_MESSAGE);
			} else {
				spinner.setValue(result.getQueryResult().boundednessAnalysis().usedTokens() - result.getQueryResult().boundednessAnalysis().tokensInNet());
				JOptionPane.showMessageDialog(CreateGui.getApp(),
						getAnswerBoundedString(), "Analysis Result",
						JOptionPane.INFORMATION_MESSAGE, ResourceManager.satisfiedIcon());
			}
		} else {						
			String message = "An error occured during the verification." +
			System.getProperty("line.separator") + 	
			System.getProperty("line.separator");
			message += "Model checker output:\n" + result.errorMessage();
			
			messenger.displayWrappedErrorMessage(message,"Error during verification");
		}
	}

	protected String getAnswerNotBoundedString() {
		return "The net with the speficied extra number of tokens is either unbounded or\n"
				+ "more extra tokens have to be added in order to achieve an exact analysis.\n\n"
				+ "This means that the analysis using the currently selected number \n"
				+ "of extra tokens provides only an underapproximation of the net behaviour.\n"
				+ "If you think that the net is bounded, try to add more extra tokens in order\n"
				+ "to achieve exact verification analysis.\n";
	}

	protected String getAnswerBoundedString() {
		return "The net with the specified extra number of tokens is bounded.\n\n"
				+ "This means that the analysis will be exact and always give \n"
				+ "the correct answer.\n\n"
				+ "The number of extra tokens was automatically lowered to the\n"
				+ "minimum number of tokens needed for an exact analysis.";
	}
}
