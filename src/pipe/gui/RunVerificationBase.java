package pipe.gui;

import java.math.BigDecimal;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.TAPNQuery.SearchOption;
import dk.aau.cs.Messenger;
import dk.aau.cs.TCTL.visitors.RenameAllPlacesVisitor;
import dk.aau.cs.approximation.OverApproximation;
import dk.aau.cs.model.tapn.TAPNQuery;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.model.tapn.simulation.TAPNNetworkTimedTransitionStep;
import dk.aau.cs.model.tapn.simulation.TAPNNetworkTimeDelayStep;
import dk.aau.cs.model.tapn.simulation.TAPNNetworkTrace;
import dk.aau.cs.model.tapn.simulation.TAPNNetworkTraceStep;
import dk.aau.cs.model.tapn.simulation.TimedArcPetriNetStep;
import dk.aau.cs.model.tapn.simulation.TimedArcPetriNetTrace;
import dk.aau.cs.util.Tuple;
import dk.aau.cs.util.UnsupportedModelException;
import dk.aau.cs.verification.ITAPNComposer;
import dk.aau.cs.verification.ModelChecker;
import dk.aau.cs.verification.NameMapping;
import dk.aau.cs.verification.QueryType;
import dk.aau.cs.verification.TAPNComposer;
import dk.aau.cs.verification.TAPNComposerExtended;
import dk.aau.cs.verification.TAPNTraceDecomposer;
import dk.aau.cs.verification.VerificationOptions;
import dk.aau.cs.verification.VerificationResult;
import dk.aau.cs.verification.VerifyTAPN.VerifyPN;
import dk.aau.cs.verification.VerifyTAPN.VerifyPNOptions;

public abstract class RunVerificationBase extends SwingWorker<VerificationResult<TAPNNetworkTrace>, Void> {

	protected ModelChecker modelChecker;

	private VerificationOptions options;
	protected TimedArcPetriNetNetwork model;
	protected TAPNQuery query;
	protected pipe.dataLayer.TAPNQuery dataLayerQuery;
	protected HashMap<TimedArcPetriNet, DataLayer> guiModels;
	
	
	protected Messenger messenger;

	public RunVerificationBase(ModelChecker modelChecker, Messenger messenger, HashMap<TimedArcPetriNet, DataLayer> guiModels) {
		super();
		this.modelChecker = modelChecker;
		this.messenger = messenger;
		this.guiModels = guiModels;
	}

	
	public void execute(VerificationOptions options, TimedArcPetriNetNetwork model, TAPNQuery query, pipe.dataLayer.TAPNQuery dataLayerQuery) {
		this.model = model;
		this.options = options;
		this.query = query;
		this.dataLayerQuery = dataLayerQuery;
		execute();
	}

	@Override
	protected VerificationResult<TAPNNetworkTrace> doInBackground() throws Exception {
		ITAPNComposer composer;
//		if (this.guiModels != null) {
//			composer = new TAPNComposerExtended(messenger, guiModels);
//		} else {
			composer = new TAPNComposer(messenger);			
//		}
		
		Tuple<TimedArcPetriNet, NameMapping> transformedModel = composer.transformModel(model);
		
		if (dataLayerQuery != null && dataLayerQuery.isApproximationEnabled())
		{
			OverApproximation overaprx = new OverApproximation();
			overaprx.modifyTAPN(transformedModel.value1(), dataLayerQuery);
		}
		
		TAPNQuery clonedQuery = new TAPNQuery(query.getProperty().copy(), query.getExtraTokens());
		MapQueryToNewNames(clonedQuery, transformedModel.value2());

		if(options.useOverApproximation() &&
				(query.queryType() == QueryType.EF || query.queryType() == QueryType.AG) &&
				!query.hasDeadlock() && !(options instanceof VerifyPNOptions)){
			VerifyPN verifypn = new VerifyPN(new FileFinderImpl(), new MessengerImpl());
			if(!verifypn.supportsModel(transformedModel.value1())){
				// Skip over-approximation if model is not supported.
				// Prevents verification from displaying error.
			}
			if(!verifypn.setup()){
				messenger.displayInfoMessage("Over-approximation check is skipped because VerifyPN is not available.", "VerifyPN unavailable");
			}else{
				VerificationResult<TimedArcPetriNetTrace> overapprox_result = verifypn.verify(new VerifyPNOptions(options.extraTokens(), options.traceOption(), SearchOption.OVERAPPROXIMATE, true), transformedModel, clonedQuery);
				if(!overapprox_result.error() && !overapprox_result.getQueryResult().isQuerySatisfied()){
					VerificationResult<TAPNNetworkTrace> value = new VerificationResult<TAPNNetworkTrace>(overapprox_result.getQueryResult(), 
							decomposeTrace(overapprox_result.getTrace(), transformedModel.value2()), 
							overapprox_result.verificationTime(), 
							overapprox_result.stats());
					value.setNameMapping(transformedModel.value2());
					return value;
				}
			}
		}
		
		VerificationResult<TAPNNetworkTrace> value = null;
		VerificationResult<TimedArcPetriNetTrace> result = modelChecker.verify(options, transformedModel, clonedQuery);
		if (isCancelled()) {
			firePropertyChange("state", StateValue.PENDING, StateValue.DONE);
		}
		if (result.error()) {
			return new VerificationResult<TAPNNetworkTrace>(result.errorMessage(), result.verificationTime());
		}
		else if (dataLayerQuery != null && dataLayerQuery.isApproximationEnabled() && result.getQueryResult().isQuerySatisfied()) {
			System.out.println("Query for approximation is satisfied");
			//Create the verification satisfied result for the approximation
			VerificationResult<TimedArcPetriNetTrace> approxResult = result;
			value =  new VerificationResult<TAPNNetworkTrace>(
					approxResult.getQueryResult(),
					decomposeTrace(approxResult.getTrace(), transformedModel.value2()),
					decomposeTrace(approxResult.getSecondaryTrace(), transformedModel.value2()),
					approxResult.verificationTime(),
					approxResult.stats());
			value.setNameMapping(transformedModel.value2());
			
			OverApproximation overaprx = new OverApproximation();

			//Create N'' from the trace
			System.out.println("Creating N''");
			Tuple<TimedArcPetriNet, NameMapping> transformedOriginalModel = composer.transformModel(model);
			overaprx.makeTraceTAPN(transformedOriginalModel, value, clonedQuery);
			System.out.println(clonedQuery.toString());
			//run model checker again for N''
			System.out.println("Verifying N''");
			result = modelChecker.verify(options, transformedOriginalModel, clonedQuery);
			if (isCancelled()) {
				firePropertyChange("state", StateValue.PENDING, StateValue.DONE);
			}
			if (result.error()) {
				return new VerificationResult<TAPNNetworkTrace>(result.errorMessage(), result.verificationTime());
			}
			//Create the result from N''
			VerificationResult<TAPNNetworkTrace> value2 = new VerificationResult<TAPNNetworkTrace>(
					result.getQueryResult(),
					decomposeTrace(result.getTrace(), transformedModel.value2()),
					decomposeTrace(result.getSecondaryTrace(), transformedModel.value2()),
					result.verificationTime(),
					result.stats());
			value.setNameMapping(transformedModel.value2());
			
			if (result.getQueryResult().isQuerySatisfied()) {
				//Recreate the approximation result with delays from N''
				 value = new VerificationResult<TAPNNetworkTrace>(
						approxResult.getQueryResult(),
						decomposeTrace(approxResult.getTrace(), transformedModel.value2()),
						decomposeTrace(approxResult.getSecondaryTrace(), transformedModel.value2()),
						approxResult.verificationTime(),
						approxResult.stats());
				 changeDelaysInTrace(value.getTrace(), value2.getTrace());
				 value.setNameMapping(transformedModel.value2());
			}
		}
		else {
			value =  new VerificationResult<TAPNNetworkTrace>(
					result.getQueryResult(),
					decomposeTrace(result.getTrace(), transformedModel.value2()),
					decomposeTrace(result.getSecondaryTrace(), transformedModel.value2()),
					result.verificationTime(),
					result.stats());
			value.setNameMapping(transformedModel.value2());
		}
		
		return value;
	}

	private void changeDelaysInTrace(TAPNNetworkTrace approxResult, TAPNNetworkTrace trace) {
		//Loop over trace and accumulate delays
		int delay = 0;
		for(TAPNNetworkTraceStep step : approxResult) {
			if (step instanceof TAPNNetworkTimeDelayStep) {
				((TAPNNetworkTimeDelayStep) step).setDelay(new BigDecimal(delay));
			}
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
				if(!(e.getCause() instanceof UnsupportedModelException)){
					e.printStackTrace();
				}
				showErrorMessage(e.getMessage());
				return;
			}
			firePropertyChange("state", StateValue.PENDING, StateValue.DONE);
			showResult(result);

		} else {
			modelChecker.kill();
			messenger.displayInfoMessage("Verification was interrupted by the user. No result found!", "Verification Cancelled");

		}
	}

	private String error;
	private void showErrorMessage(String errorMessage) {
		error = errorMessage;
		SwingUtilities.invokeLater(new Runnable() { //The invoke later will make sure all the verification is finished before showing the error
			public void run() {
			    messenger.displayErrorMessage("The engine selected in the query dialog cannot verify this model.\nPlease choose another engine.\n" + error);
				CreateGui.getCurrentTab().editSelectedQuery();
			}
		});
	}

	protected abstract void showResult(VerificationResult<TAPNNetworkTrace> result);
}
