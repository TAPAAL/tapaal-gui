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
	private File modelFile; // TODO: MJ -- Get rid of both, used now for legacy support
	private File queryFile;
	private NTA model;
	private UPPAALQuery[] queries;
	private long verificationTime = 0;
	
	public RunVerificationBase(ModelChecker<NTA, UPPAALQuery> modelChecker) {
		super();
		this.modelChecker = modelChecker;
	}

	public void execute(File modelFile, File queryFile, VerificationOptions options){
		this.modelFile = modelFile;
		this.queryFile = queryFile;
		this.options = options;
		execute();
	}
	
	public void execute(VerificationOptions options, NTA model, UPPAALQuery... queries){
		this.model = model;
		this.queries = queries;
		this.options = options;
		execute();
	}
	
	@Override
	protected VerificationResult doInBackground() throws Exception {
		long startMS = System.currentTimeMillis();
		VerificationResult result;
		if(model != null){
			result = modelChecker.verify(options, model, queries);
		}else{
			result = modelChecker.verify(options, modelFile, queryFile);
		}
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
					"Verification Cancelled", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	protected abstract void showResult(VerificationResult result, long verificationTime);
}