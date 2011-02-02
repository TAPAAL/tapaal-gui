package pipe.gui;

import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import dk.aau.cs.Messenger;
import dk.aau.cs.TCTL.visitors.RenameAllPlacesVisitor;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.petrinet.TAPNQuery;
import dk.aau.cs.util.Tuple;
import dk.aau.cs.verification.ModelChecker;
import dk.aau.cs.verification.NameMapping;
import dk.aau.cs.verification.NewModelToOldModelTransformer;
import dk.aau.cs.verification.TAPNComposer;
import dk.aau.cs.verification.VerificationOptions;
import dk.aau.cs.verification.VerificationResult;

public abstract class RunVerificationBase extends
		SwingWorker<VerificationResult, Void> {

	private ModelChecker modelChecker;
	private VerificationOptions options;
	private TimedArcPetriNetNetwork model;
	private TAPNQuery query;
	protected Messenger messenger;
	
	public RunVerificationBase(ModelChecker modelChecker, Messenger messenger) {
		super();
		this.modelChecker = modelChecker;
		this.messenger = messenger;
	}
	
	public void execute(VerificationOptions options, TimedArcPetriNetNetwork model, TAPNQuery query){
		this.model = model;
		this.options = options;
		this.query = query;
		execute();
	}
	
	@Override
	protected VerificationResult doInBackground() throws Exception {
		TAPNComposer composer = new TAPNComposer();
		Tuple<TimedArcPetriNet, NameMapping> transformedModel = composer.transformModel(model);
		
		// TODO: Get rid of this step by changing the underlying translations etc.
		NewModelToOldModelTransformer transformer = new NewModelToOldModelTransformer();
		dk.aau.cs.petrinet.TimedArcPetriNet tapn = transformer.transformModel(transformedModel.value1());
		
		TAPNQuery clonedQuery = new TAPNQuery(query.getProperty().copy(), query.getTotalTokens());
		MapQueryToNewNames(clonedQuery, transformedModel.value2());
		
		VerificationResult result = modelChecker.verify(options, tapn, clonedQuery);
		return result;
	}
	
	private void MapQueryToNewNames(TAPNQuery query, NameMapping mapping) {
		RenameAllPlacesVisitor visitor = new RenameAllPlacesVisitor(mapping);
		query.getProperty().accept(visitor,null);
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