package pipe.gui;

import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import dk.aau.cs.Messenger;
import dk.aau.cs.petrinet.TAPNQuery;
import dk.aau.cs.petrinet.TimedArcPetriNet;
import dk.aau.cs.verification.ModelChecker;
import dk.aau.cs.verification.VerificationOptions;
import dk.aau.cs.verification.VerificationResult;

public abstract class RunVerificationBase extends
		SwingWorker<VerificationResult, Void> {

	private ModelChecker modelChecker;
	protected VerificationOptions options;
	private TimedArcPetriNet model;
	protected TAPNQuery query;
	protected Messenger messenger;
	
	public RunVerificationBase(ModelChecker modelChecker, Messenger messenger) {
		super();
		this.modelChecker = modelChecker;
		this.messenger = messenger;
	}
	
	public void execute(VerificationOptions options, TimedArcPetriNet model, TAPNQuery query){
		this.model = model;
		this.options = options;
		this.query = query;
		execute();
	}
	
	@Override
	protected VerificationResult doInBackground() throws Exception {
		VerificationResult result = modelChecker.verify(options, model, query);
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
			
			showResult(result, result.verificationTime());
		}else{
			modelChecker.kill();			
			messenger.displayInfoMessage("Verification was interupted by the user. No result found!",
					"Verification Cancelled");
		}
	}

	protected abstract void showResult(VerificationResult result, long verificationTime);
}