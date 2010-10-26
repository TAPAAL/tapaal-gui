/**
 * 
 */
package pipe.gui;

import javax.swing.JOptionPane;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.TAPNTrace;
import pipe.gui.GuiFrame.GUIMode;
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
			
			if(result.getTrace() != null){
				DataLayer model = CreateGui.getModel();
				TraceTransformer interpreter =  model.isUsingColors() ? new ColoredTraceTransformer(model) : new TraceTransformer(model);
				TAPNTrace trace = interpreter.interpretTrace(result.getTrace());
				showAnimationMode();
				CreateGui.getAnimator().SetTrace(trace);
			}
		}
	}

	private void showAnimationMode() {
		CreateGui.getApp().setGUIMode(GUIMode.animation);
//		CreateGui.getApp().setMode(Pipe.START);
//        CreateGui.getView().getSelectionObject().clearSelection();
//		CreateGui.getAnimator().resethistory();
	}
}