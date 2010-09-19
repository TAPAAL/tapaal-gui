/**
 * 
 */
package pipe.gui;

import java.io.File;
import java.util.concurrent.ExecutionException;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import pipe.gui.widgets.RunningVerificationDialog;

import dk.aau.cs.TA.NTA;
import dk.aau.cs.TA.UPPAALQuery;
import dk.aau.cs.verification.ModelChecker;
import dk.aau.cs.verification.VerificationOptions;
import dk.aau.cs.verification.VerificationResult;

public class RunUppaalVerification extends SwingWorker<VerificationResult, Void> {

	private ModelChecker<NTA, UPPAALQuery> modelChecker;
	private VerificationOptions options;
	private File modelFile;
	private File queryFile;
	private RunningVerificationDialog dialog;
	private long verificationTime = 0;
	
	public RunUppaalVerification(ModelChecker<NTA, UPPAALQuery> modelChecker, VerificationOptions options, File modelFile, File queryFile) {
		this.modelChecker = modelChecker;
		this.options = options;
		this.modelFile = modelFile;
		this.queryFile = queryFile;
	}

	@Override
	protected VerificationResult doInBackground() throws Exception {
		long startMS = System.currentTimeMillis();
		VerificationResult result = modelChecker.verify(modelFile, queryFile, options);
		long endMS = System.currentTimeMillis();
		
		verificationTime = endMS - startMS;
		return result;
	}
	
	@Override
	protected void done() {
		dialog.finished();
						
		if(!isCancelled() && isDone()){
			VerificationResult result = null;
			try {
				result = get();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String satisfaction = result.isQuerySatisfied() ? "satisfied" : "not satisfied";
			JOptionPane.showMessageDialog(CreateGui.getApp(), 
					String.format("Property is %1$s.\nEstimated verification time: %2$.2fs", satisfaction, verificationTime/1000.0),
					"Verification Result", JOptionPane.INFORMATION_MESSAGE);
		}else{
			modelChecker.kill();			
			JOptionPane.showMessageDialog(CreateGui.getApp(), "Verification was interupted by the user. No result found!",
					"Verification Result", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	public void setDialog(RunningVerificationDialog dialog) {
		this.dialog = dialog;
	}

}