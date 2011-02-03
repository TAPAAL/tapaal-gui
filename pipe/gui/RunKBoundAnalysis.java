package pipe.gui;

import javax.swing.JOptionPane;

import dk.aau.cs.Messenger;
import dk.aau.cs.model.tapn.simulation.TapaalTrace;
import dk.aau.cs.verification.ModelChecker;
import dk.aau.cs.verification.VerificationResult;

public class RunKBoundAnalysis extends RunVerificationBase {

	public RunKBoundAnalysis(ModelChecker modelChecker, Messenger messenger) {
		super(modelChecker, messenger);
	}

	@Override
	protected void showResult(VerificationResult<TapaalTrace> result,
			long verificationTime) {
		JOptionPane.showMessageDialog(CreateGui.getApp(), result
				.isQuerySatisfied() ? getAnswerNotBoundedString()
				: getAnswerBoundedString(), "Analysis Result",
				JOptionPane.INFORMATION_MESSAGE);
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
				+ "This means that the analysis using the currently selected number\n"
				+ "of extra tokens will be exact and always give the correct answer.\n";
	}
}
