package pipe.gui;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.TAPNQuery.TraceOption;
import pipe.dataLayer.TAPNQuery.SearchOption;
import pipe.gui.widgets.InclusionPlaces;
import dk.aau.cs.Messenger;
import dk.aau.cs.TCTL.visitors.RenameAllPlacesVisitor;
import dk.aau.cs.approximation.OverApproximation;
import dk.aau.cs.approximation.UnderApproximation;
import dk.aau.cs.model.tapn.TAPNQuery;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.model.tapn.TimedToken;
import dk.aau.cs.model.tapn.simulation.TAPNNetworkTrace;
import dk.aau.cs.model.tapn.simulation.TimeDelayStep;
import dk.aau.cs.model.tapn.simulation.TimedArcPetriNetStep;
import dk.aau.cs.model.tapn.simulation.TimedArcPetriNetTrace;
import dk.aau.cs.model.tapn.simulation.TimedTransitionStep;
import dk.aau.cs.util.MemoryMonitor;
import dk.aau.cs.util.Tuple;
import dk.aau.cs.util.UnsupportedModelException;
import dk.aau.cs.verification.ITAPNComposer;
import dk.aau.cs.verification.ModelChecker;
import dk.aau.cs.verification.NameMapping;
import dk.aau.cs.verification.QueryType;
import dk.aau.cs.verification.QueryResult;
import dk.aau.cs.verification.TAPNComposer;
import dk.aau.cs.verification.TAPNTraceDecomposer;
import dk.aau.cs.verification.VerificationOptions;
import dk.aau.cs.verification.VerificationResult;
import dk.aau.cs.verification.VerifyTAPN.VerifyPN;
import dk.aau.cs.verification.VerifyTAPN.VerifyPNOptions;
import dk.aau.cs.verification.VerifyTAPN.VerifyTAPNOptions;

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
		ITAPNComposer composer = new TAPNComposer(messenger);
		Tuple<TimedArcPetriNet, NameMapping> transformedModel = composer.transformModel(model);
		
		if (dataLayerQuery != null && dataLayerQuery.isOverApproximationEnabled())
		{
			OverApproximation overaprx = new OverApproximation();
			overaprx.modifyTAPN(transformedModel.value1(), dataLayerQuery);
		}
		else if (dataLayerQuery != null && dataLayerQuery.isUnderApproximationEnabled())
		{
			UnderApproximation underaprx = new UnderApproximation();
			underaprx.modifyTAPN(transformedModel.value1(), dataLayerQuery);
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
							overapprox_result.stats(),
							true);
					value.setNameMapping(transformedModel.value2());
					return value;
				}
			}
		}
		// If options is of an instance of VerifyTAPNOptions then save the inclusion places before verify alters them
		InclusionPlaces oldInclusionPlaces = null;
		if (options instanceof VerifyTAPNOptions)
			oldInclusionPlaces = ((VerifyTAPNOptions) options).inclusionPlaces();
		
		// Enable SOME_TRACE if not already
		TraceOption oldTraceOption = options.traceOption();
		if (dataLayerQuery != null && (dataLayerQuery.isOverApproximationEnabled() || dataLayerQuery.isUnderApproximationEnabled())) {
			options.setTraceOption(TraceOption.SOME);
			MemoryMonitor.setCumulativePeakMemory(true);
		}
		
		VerificationResult<TAPNNetworkTrace> value = null;
		VerificationResult<TimedArcPetriNetTrace> result = modelChecker.verify(options, transformedModel, clonedQuery);
		if (isCancelled()) {
			firePropertyChange("state", StateValue.PENDING, StateValue.DONE);
		}
		if (result.error()) {
			return new VerificationResult<TAPNNetworkTrace>(result.errorMessage(), result.verificationTime());
		}
		else if (dataLayerQuery != null && dataLayerQuery.isOverApproximationEnabled() && ((result.getQueryResult().queryType() == QueryType.EF && result.getQueryResult().isQuerySatisfied()) || (result.getQueryResult().queryType() == QueryType.AG && !result.getQueryResult().isQuerySatisfied()))) {		
			//Create the verification satisfied result for the approximation
			VerificationResult<TimedArcPetriNetTrace> approxResult = result;
			value = new VerificationResult<TAPNNetworkTrace>(
					approxResult.getQueryResult(),
					decomposeTrace(approxResult.getTrace(), transformedModel.value2()),
					decomposeTrace(approxResult.getSecondaryTrace(), transformedModel.value2()),
					approxResult.verificationTime(),
					approxResult.stats());
			value.setNameMapping(transformedModel.value2());
			
			OverApproximation overaprx = new OverApproximation();
			
			// get the originalQueryType before a a potential AG query is rewritten to an EF query
			QueryType originalQueryType = result.getQueryResult().queryType();

			//Create trace TAPN from the trace
			Tuple<TimedArcPetriNet, NameMapping> transformedOriginalModel = composer.transformModel(model);
			overaprx.makeTraceTAPN(transformedOriginalModel, value, clonedQuery);
			
			// Reset the inclusion places in order to avoid NullPointerExceptions
			if (options instanceof VerifyTAPNOptions && oldInclusionPlaces != null)
				((VerifyTAPNOptions) options).setInclusionPlaces(oldInclusionPlaces);

			//run model checker again for trace TAPN
			result = modelChecker.verify(options, transformedOriginalModel, clonedQuery);
			if (isCancelled()) {
				firePropertyChange("state", StateValue.PENDING, StateValue.DONE);
			}
			if (result.error()) {
				return new VerificationResult<TAPNNetworkTrace>(result.errorMessage(), result.verificationTime());
			}
			//Create the result from trace TAPN
			renameTraceTransitions(result.getTrace());
			renameTraceTransitions(result.getSecondaryTrace());
			QueryResult queryResult= result.getQueryResult();
			
			// The query were rewritten to an EF query, and since the topNode cannot be a not node we need to flip the result.
			if(originalQueryType == QueryType.AG){
				queryResult.flipResult();
			}
			
			if ((originalQueryType == QueryType.EF && !queryResult.isQuerySatisfied()) || originalQueryType == QueryType.AG && queryResult.isQuerySatisfied()){
				queryResult.setApproximationInconclusive(true);
			}
			value = new VerificationResult<TAPNNetworkTrace>(
					queryResult,
					decomposeTrace(result.getTrace(), transformedModel.value2()),
					decomposeTrace(result.getSecondaryTrace(), transformedModel.value2()),
					approxResult.verificationTime() + result.verificationTime(),
					approxResult.stats());
			
			value.setNameMapping(transformedModel.value2());
		} 
		else if (dataLayerQuery != null && dataLayerQuery.isUnderApproximationEnabled()) {
			if ((result.getQueryResult().queryType() == QueryType.EF && result.getQueryResult().isQuerySatisfied()) || (result.getQueryResult().queryType() == QueryType.AG && !result.getQueryResult().isQuerySatisfied())) {
				QueryResult queryResult= result.getQueryResult();
				if (query.queryType() == QueryType.EF && query.hasDeadlock()) {
					queryResult.setApproximationInconclusive(true);
				}
				for (TimedArcPetriNetStep k : result.getTrace()) {
					if (k instanceof TimeDelayStep){
						((TimeDelayStep) k).setDelay(((TimeDelayStep) k).delay().multiply(new BigDecimal(dataLayerQuery.approximationDenominator())));
					}
					else if (k instanceof TimedTransitionStep) {
						for (TimedToken a : ((TimedTransitionStep) k).consumedTokens()){
							a.setAge(a.age().multiply(new BigDecimal(dataLayerQuery.approximationDenominator())));
						}
					}
				}
				value =  new VerificationResult<TAPNNetworkTrace>(
						queryResult,
						decomposeTrace(result.getTrace(), transformedModel.value2()),
						decomposeTrace(result.getSecondaryTrace(), transformedModel.value2()),
						result.verificationTime(),
						result.stats());
				value.setNameMapping(transformedModel.value2());
			}
			else
			{
				QueryResult queryResult= result.getQueryResult();
				queryResult.setApproximationInconclusive(true);
				value =  new VerificationResult<TAPNNetworkTrace>(
						queryResult,
						decomposeTrace(result.getTrace(), transformedModel.value2()),
						decomposeTrace(result.getSecondaryTrace(), transformedModel.value2()),
						result.verificationTime(),
						result.stats());
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
		// TODO: Handle under approximation
		
		options.setTraceOption(oldTraceOption);
		MemoryMonitor.setCumulativePeakMemory(false);
		
		return value;
	}
	
	private void renameTraceTransitions(TimedArcPetriNetTrace trace) {
		if (trace != null)
			trace.reduceTraceForOriginalNet("_traceNet_", "PTRACE");
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
