package pipe.gui;

import java.io.File;
import java.util.concurrent.ExecutionException;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import dk.aau.cs.TA.NTA;
import dk.aau.cs.TA.UPPAALQuery;
import dk.aau.cs.verification.ModelChecker;
import dk.aau.cs.verification.VerificationOptions;
import dk.aau.cs.verification.VerificationResult;

public abstract class RunVerificationBase extends
		SwingWorker<VerificationResult, Void> {

	private ModelChecker<NTA, UPPAALQuery> modelChecker;
	private VerificationOptions options;
	private File modelFile;
	private File queryFile;
	private long verificationTime = 0;
	
	public RunVerificationBase(ModelChecker<NTA, UPPAALQuery> modelChecker, VerificationOptions options, File modelFile, File queryFile) {
		super();
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
		if(!isCancelled()){
			VerificationResult result = null;
			try {
				result = get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
			
			showResult(result, verificationTime);
		}else{
			modelChecker.kill();			
			JOptionPane.showMessageDialog(CreateGui.getApp(), "Verification was interupted by the user. No result found!",
					"Verification Result", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	protected abstract void showResult(VerificationResult result, long verificationTime);
}