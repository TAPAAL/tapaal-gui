package pipe.gui;

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
	private TimedArcPetriNet model;
	private long verificationTime = 0;
	private TAPNQuery query;
	
	public RunVerificationBase(ModelChecker modelChecker) {
		super();
		this.modelChecker = modelChecker;
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
		VerificationResult result = modelChecker.verify(options, model, query);
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