package pipe.gui;

import java.io.File;
import java.util.concurrent.ExecutionException;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import dk.aau.cs.petrinet.TAPNQuery;
import dk.aau.cs.petrinet.TimedArcPetriNet;
import dk.aau.cs.verification.ModelChecker;
import dk.aau.cs.verification.VerificationOptions;
import dk.aau.cs.verification.VerificationResult;

public abstract class RunVerificationBase extends
		SwingWorker<VerificationResult, Void> {

	private ModelChecker modelChecker;
	private VerificationOptions options;
	private File modelFile; // TODO: MJ -- Get rid of both, used now for legacy support
	private File queryFile;
	private TimedArcPetriNet model;
	private long verificationTime = 0;
	private TAPNQuery query;
	
	public RunVerificationBase(ModelChecker modelChecker) {
		super();
		this.modelChecker = modelChecker;
	}

	public void execute(File modelFile, File queryFile, VerificationOptions options){
		this.modelFile = modelFile;
		this.queryFile = queryFile;
		this.options = options;
		execute();
	}
	
	public void execute(VerificationOptions options, TimedArcPetriNet model, TAPNQuery query){
		this.model = model;
		this.options = options;
		this.query = query;
		execute();
	}
	
	@Override
	protected VerificationResult doInBackground() throws Exception {
		long startMS = System.currentTimeMillis();
		VerificationResult result;
		if(model != null){
			result = modelChecker.verify(options, model, query);
		}else{
			result = modelChecker.verify(options, modelFile.getAbsolutePath(), queryFile.getAbsolutePath());
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