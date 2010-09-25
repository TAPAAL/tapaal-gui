/**
 * 
 */
package pipe.gui;

import javax.swing.JOptionPane;

import dk.aau.cs.TA.NTA;
import dk.aau.cs.TA.UPPAALQuery;
import dk.aau.cs.verification.ModelChecker;
import dk.aau.cs.verification.VerificationResult;

public class RunVerification extends RunVerificationBase {

	public RunVerification(ModelChecker<NTA, UPPAALQuery> modelChecker) {
		super(modelChecker);
	}

	@Override
	protected void showResult(VerificationResult result, long verificationTime) {	
		if(result != null){
			String satisfaction = result.isQuerySatisfied(0) ? "satisfied" : "not satisfied";
			JOptionPane.showMessageDialog(CreateGui.getApp(), 
					String.format("Property is %1$s.\nEstimated verification time: %2$.2fs", satisfaction, verificationTime/1000.0),
					"Verification Result", JOptionPane.INFORMATION_MESSAGE);
		}
	}
}