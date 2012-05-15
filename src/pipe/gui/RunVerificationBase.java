package pipe.gui;

import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import dk.aau.cs.Messenger;
import dk.aau.cs.TCTL.visitors.RenameAllPlacesVisitor;
import dk.aau.cs.model.tapn.TAPNQuery;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.model.tapn.simulation.TAPNNetworkTrace;
import dk.aau.cs.model.tapn.simulation.TimedArcPetriNetTrace;
import dk.aau.cs.util.Tuple;
import dk.aau.cs.verification.ModelChecker;
import dk.aau.cs.verification.NameMapping;
import dk.aau.cs.verification.TAPNComposer;
import dk.aau.cs.verification.TAPNTraceDecomposer;
import dk.aau.cs.verification.VerificationOptions;
import dk.aau.cs.verification.VerificationResult;

public abstract class RunVerificationBase extends SwingWorker<VerificationResult<TAPNNetworkTrace>, Void> {

	protected ModelChecker modelChecker;

	private VerificationOptions options;
	protected TimedArcPetriNetNetwork model;
	protected TAPNQuery query;

	protected Messenger messenger;

	public RunVerificationBase(ModelChecker modelChecker, Messenger messenger) {
		super();
		this.modelChecker = modelChecker;
		this.messenger = messenger;
	}

	public void execute(VerificationOptions options, TimedArcPetriNetNetwork model, TAPNQuery query) {
		this.model = model;
		this.options = options;
		this.query = query;
		execute();
	}

	@Override
	protected VerificationResult<TAPNNetworkTrace> doInBackground() throws Exception {
		TAPNComposer composer = new TAPNComposer(messenger);
		Tuple<TimedArcPetriNet, NameMapping> transformedModel = composer.transformModel(model);

		TAPNQuery clonedQuery = new TAPNQuery(query.getProperty().copy(), query.getExtraTokens());
		MapQueryToNewNames(clonedQuery, transformedModel.value2());

		VerificationResult<TimedArcPetriNetTrace> result = modelChecker.verify(options, transformedModel, clonedQuery);
		if (isCancelled()) {
			firePropertyChange("state", StateValue.PENDING, StateValue.DONE);
		}
		if (result.error()) {
			return new VerificationResult<TAPNNetworkTrace>(result.errorMessage(), result.verificationTime());
		} else {
			return new VerificationResult<TAPNNetworkTrace>(
					result.getQueryResult(),
					decomposeTrace(result.getTrace(), transformedModel.value2()),
					result.verificationTime(),
					mapHumanTrace(result.getHumanTrace(), transformedModel),
					result.stats()
					);
		}		
	}
	
	protected int kBound(){
		return model.marking().size() + query.getExtraTokens();
	}

	private TAPNNetworkTrace decomposeTrace(TimedArcPetriNetTrace trace, NameMapping mapping) {
		if (trace == null)
			return null;

		TAPNTraceDecomposer decomposer = new TAPNTraceDecomposer(trace, model, mapping);
		return decomposer.decompose();
	}
	
	private String mapHumanTrace(String trace, Tuple<TimedArcPetriNet, NameMapping> transformedModel){
		if(trace == null) return null;
		int index = trace.indexOf("Trace:");
		if(index == -1) return null;
		
		trace = trace.substring(index);
		
		Object[] arr = (Object[])transformedModel.value2().getMappedToOrg().entrySet().toArray();
		try{
		    for(int i = arr.length-1; i > -1; i--){
			Entry<String, Tuple<String, String>> e = (Entry<String, Tuple<String, String>>) arr[i];
			trace = trace.replace("("+e.getKey()+",", "("+e.getValue().value1() + "." + e.getValue().value2()+",");
		    }
		}catch(java.lang.OutOfMemoryError e) {
			trace = "Trace too long; could not display.";
		}
		return trace;
	}

	private void MapQueryToNewNames(TAPNQuery query, NameMapping mapping) {
		RenameAllPlacesVisitor visitor = new RenameAllPlacesVisitor(mapping);
		query.getProperty().accept(visitor, null);
	}

	@Override
	protected void done() {
		if (!isCancelled()) {
			VerificationResult<TAPNNetworkTrace> result = null;

			try {
				result = get();
			} catch (InterruptedException e) {
				e.printStackTrace();
				showErrorMessage(e.getMessage());
				return;
			} catch (ExecutionException e) {
				e.printStackTrace();
				showErrorMessage(e.getMessage());
				return;
			}
			firePropertyChange("state", StateValue.PENDING, StateValue.DONE);
			showResult(result);
			showHumanTrace(result);

		} else {
			modelChecker.kill();
			messenger.displayInfoMessage("Verification was interrupted by the user. No result found!", "Verification Cancelled");

		}
	}

	private void showErrorMessage(String errorMessage) {
		messenger.displayErrorMessage("An error occured during verification.\n\nReason: " + errorMessage, "Verification Error");
	}
	
	protected void showHumanTrace(VerificationResult<TAPNNetworkTrace> result) { }

	protected abstract void showResult(VerificationResult<TAPNNetworkTrace> result);
}
