/**
 * 
 */
package pipe.gui;

import javax.swing.JOptionPane;

import pipe.dataLayer.TAPNTrace;

import dk.aau.cs.verification.ModelChecker;
import dk.aau.cs.verification.VerificationResult;

public class RunVerification extends RunVerificationBase {

	public RunVerification(ModelChecker modelChecker) {
		super(modelChecker);
	}

	@Override
	protected void showResult(VerificationResult result, long verificationTime) {	
		if(result != null){
			String satisfaction = result.isQuerySatisfied() ? "satisfied" : "not satisfied";
			JOptionPane.showMessageDialog(CreateGui.getApp(), 
					String.format("Property is %1$s.\nEstimated verification time: %2$.2fs", satisfaction, verificationTime/1000.0),
					"Verification Result", JOptionPane.INFORMATION_MESSAGE);
			
			if(result.getTrace() != null){ // TODO: This is too simplistic.. It works because the same naming scheme works for all translations (excluding colors)
				TraceTransformer interpreter = new TraceTransformer(CreateGui.getModel());
				TAPNTrace trace = interpreter.interpretTrace(result.getTrace());
				showAnimationMode();
				CreateGui.getAnimator().SetTrace(trace);
			}
		}
	}

	private void showAnimationMode() {
		CreateGui.getApp().setAnimationMode(true);
		CreateGui.getApp().setMode(Pipe.START);
        CreateGui.getView().getSelectionObject().clearSelection();
		CreateGui.getAnimator().resethistory();
	}
}