/**
 * 
 */
package pipe.gui;

import javax.swing.JOptionPane;

import pipe.gui.GuiFrame.GUIMode;
import dk.aau.cs.Messenger;
import dk.aau.cs.model.tapn.simulation.TapaalTrace;
import dk.aau.cs.verification.ModelChecker;
import dk.aau.cs.verification.VerificationResult;

public class RunVerification extends RunVerificationBase {

	public RunVerification(ModelChecker modelChecker, Messenger messenger) {
		super(modelChecker, messenger);
	}

	@Override
	protected void showResult(VerificationResult<TapaalTrace> result, long verificationTime) {
		if(result != null && !result.error()){
			String satisfaction = result.isQuerySatisfied() ? "satisfied" : "not satisfied";
			JOptionPane.showMessageDialog(CreateGui.getApp(), 
					String.format("Property is %1$s.\nEstimated verification time: %2$.2fs", satisfaction, verificationTime/1000.0),
					"Verification Result", JOptionPane.INFORMATION_MESSAGE);
			
			if(result.getTrace() != null){
//				DataLayer model = CreateGui.getModel();
//				TraceTransformer interpreter =  model.isUsingColors() ? new ColoredTraceTransformer(model) : new TraceTransformer(model);
//				TAPNTrace trace = interpreter.interpretTrace(result.getTrace());
				CreateGui.getApp().setGUIMode(GUIMode.animation);
				CreateGui.getAnimator().SetTrace(result.getTrace());
			}
		}else{
			messenger.displayWrappedErrorMessage("An error occured during the verification." +
					System.getProperty("line.separator") + 	
					System.getProperty("line.separator") + 
					"UPPAAL output:\n" + result.errorMessage(), 
					"Error during verification");
		}
	}
}