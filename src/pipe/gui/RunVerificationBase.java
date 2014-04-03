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
import dk.aau.cs.verification.UPPAAL.VerifytaOptions;
import dk.aau.cs.verification.VerifyTAPN.ModelReduction;
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
		
		if (options.enableOverApproximation())
		{
			OverApproximation overaprx = new OverApproximation();
			overaprx.modifyTAPN(transformedModel.value1(), options.approximationDenominator());
		}
		else if (options.enableUnderApproximation())
		{
			UnderApproximation underaprx = new UnderApproximation();
			underaprx.modifyTAPN(transformedModel.value1(), options.approximationDenominator());
		}

		TAPNQuery clonedQuery = new TAPNQuery(query.getProperty().copy(), query.getExtraTokens());
		MapQueryToNewNames(clonedQuery, transformedModel.value2());

		if(options.useOverApproximation() &&
				(query.queryType() == QueryType.EF || query.queryType() == QueryType.AG) &&
				!query.hasDeadlock() && !(options instanceof VerifyPNOptions)){
			VerifyPN verifypn = new VerifyPN(new FileFinderImpl(), new MessengerImpl());
			if(!verifypn.supportsModel(transformedModel.value1(), options)){
				// Skip over-approximation if model is not supported.
				// Prevents verification from displaying error.
			}


			if(!verifypn.setup()){
				messenger.displayInfoMessage("Over-approximation check is skipped because VerifyPN is not available.", "VerifyPN unavailable");
			}else{
				VerificationResult<TimedArcPetriNetTrace> overapprox_result = verifypn.verify(new VerifyPNOptions(options.extraTokens(), options.traceOption(), SearchOption.OVERAPPROXIMATE, true, ModelReduction.AGGRESSIVE, options.enableOverApproximation(), options.enableUnderApproximation(), options.approximationDenominator()), transformedModel, clonedQuery);
				if(!overapprox_result.error() && (
						(query.queryType() == QueryType.EF && !overapprox_result.getQueryResult().isQuerySatisfied()) ||
						(query.queryType() == QueryType.AG && overapprox_result.getQueryResult().isQuerySatisfied()))
						){
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
		if ((options.enableOverApproximation() || options.enableUnderApproximation()) && !(options instanceof VerifytaOptions)) {
			options.setTraceOption(TraceOption.SOME);
			MemoryMonitor.setCumulativePeakMemory(true);
		}
		
		VerificationResult<TAPNNetworkTrace> value = null;
		VerificationResult<TimedArcPetriNetTrace> result = modelChecker.verify(options, transformedModel, clonedQuery);
		if (isCancelled()) {
			firePropertyChange("state", StateValue.PENDING, StateValue.DONE);
		}
		if (result.error()) {
			options.setTraceOption(oldTraceOption);
			// if the old traceoption was none, we need to set the results traces to null so GUI doesn't try to display the traces later
			if (oldTraceOption == TraceOption.NONE){
				value.setTrace(null);
				value.setSecondaryTrace(null);
			}
			MemoryMonitor.setCumulativePeakMemory(false);
			return new VerificationResult<TAPNNetworkTrace>(result.errorMessage(), result.verificationTime());
		}
		else if (options.enableOverApproximation()) {
			// Over-approximation
			if (options.approximationDenominator() == 1) {
				// If r = 1
				// No matter what EF and AG answered -> return that answer
				QueryResult queryResult = result.getQueryResult();
				value =  new VerificationResult<TAPNNetworkTrace>(
						queryResult,
						decomposeTrace(result.getTrace(), transformedModel.value2()),
						decomposeTrace(result.getSecondaryTrace(), transformedModel.value2()),
						result.verificationTime(),
						result.stats(),
						result.isOverApproximationResult());
				value.setNameMapping(transformedModel.value2());
			} else {
				// If r > 1
				if (result.getTrace() != null && (((result.getQueryResult().queryType() == QueryType.EF || result.getQueryResult().queryType() == QueryType.EG) && result.getQueryResult().isQuerySatisfied())
					|| ((result.getQueryResult().queryType() == QueryType.AG || result.getQueryResult().queryType() == QueryType.AF) && !result.getQueryResult().isQuerySatisfied()))) {
						// If we have a trace AND ((EF OR EG) AND satisfied) OR ((AG OR AF) AND not satisfied)
						// The results are inconclusive, but we get a trace and can use trace TAPN for verification.
					
						VerificationResult<TimedArcPetriNetTrace> approxResult = result;
						value = new VerificationResult<TAPNNetworkTrace>(
								approxResult.getQueryResult(),
								decomposeTrace(approxResult.getTrace(), transformedModel.value2()),
								decomposeTrace(approxResult.getSecondaryTrace(), transformedModel.value2()),
								approxResult.verificationTime(),
								approxResult.stats(),
								approxResult.isOverApproximationResult());
						value.setNameMapping(transformedModel.value2());
						
						OverApproximation overaprx = new OverApproximation();
			
						//Create trace TAPN from the trace
						Tuple<TimedArcPetriNet, NameMapping> transformedOriginalModel = composer.transformModel(model);
						overaprx.makeTraceTAPN(transformedOriginalModel, value, clonedQuery);
						
						// Reset the inclusion places in order to avoid NullPointerExceptions
						if (options instanceof VerifyTAPNOptions && oldInclusionPlaces != null)
							((VerifyTAPNOptions) options).setInclusionPlaces(oldInclusionPlaces);
			
						// run model checker again for trace TAPN
						result = modelChecker.verify(options, transformedOriginalModel, clonedQuery);
						if (isCancelled()) {
							firePropertyChange("state", StateValue.PENDING, StateValue.DONE);
						}
						if (result.error()) {
							options.setTraceOption(oldTraceOption);
							// if the old traceoption was none, we need to set the results traces to null so GUI doesn't try to display the traces later
							if (oldTraceOption == TraceOption.NONE){
								value.setTrace(null);
								value.setSecondaryTrace(null);
							}
							MemoryMonitor.setCumulativePeakMemory(false);
							return new VerificationResult<TAPNNetworkTrace>(result.errorMessage(), result.verificationTime());
						}
						//Create the result from trace TAPN
						renameTraceTransitions(result.getTrace());
						renameTraceTransitions(result.getSecondaryTrace());
						QueryResult queryResult = result.getQueryResult();
						
						// If ((EG OR EG) AND not satisfied trace) OR ((AG OR AF) AND satisfied trace) -> inconclusive
						if (((result.getQueryResult().queryType() == QueryType.EF || result.getQueryResult().queryType() == QueryType.EG) && !queryResult.isQuerySatisfied()) 
								|| ((result.getQueryResult().queryType() == QueryType.AG || result.getQueryResult().queryType() == QueryType.AF) && queryResult.isQuerySatisfied())){
							queryResult.setApproximationInconclusive(true);
						}
						
						// If (EF AND satisfied trace) OR (AG AND satisfied trace) -> Return result
						// This is satisfied for EF and not satisfied for AG
						value = new VerificationResult<TAPNNetworkTrace>(
								queryResult,
								decomposeTrace(result.getTrace(), transformedModel.value2()),
								decomposeTrace(result.getSecondaryTrace(), transformedModel.value2()),
								approxResult.verificationTime() + result.verificationTime(),
								approxResult.stats(),
								approxResult.isOverApproximationResult());
						value.setNameMapping(transformedModel.value2());
					} 
					else if (((result.getQueryResult().queryType() == QueryType.EF || result.getQueryResult().queryType() == QueryType.EG) && !result.getQueryResult().isQuerySatisfied())
							|| ((result.getQueryResult().queryType() == QueryType.AG || result.getQueryResult().queryType() == QueryType.AF) && result.getQueryResult().isQuerySatisfied())) {
						// If ((EF OR EG) AND not satisfied) OR ((AG OR AF) AND satisfied)
						
						QueryResult queryResult = result.getQueryResult();
						
						if(queryResult.hasDeadlock() || result.getQueryResult().queryType() == QueryType.EG || result.getQueryResult().queryType() == QueryType.AF){
							queryResult.setApproximationInconclusive(true);
						}
						
						VerificationResult<TimedArcPetriNetTrace> approxResult = result;
						value = new VerificationResult<TAPNNetworkTrace>(
								approxResult.getQueryResult(),
								decomposeTrace(approxResult.getTrace(), transformedModel.value2()),
								decomposeTrace(approxResult.getSecondaryTrace(), transformedModel.value2()),
								approxResult.verificationTime(),
								approxResult.stats(),
								approxResult.isOverApproximationResult());
						value.setNameMapping(transformedModel.value2());
					} else {
						// We cannot use the result directly, and did not get a trace.
						
						QueryResult queryResult = result.getQueryResult();
						queryResult.setApproximationInconclusive(true);
						
						VerificationResult<TimedArcPetriNetTrace> approxResult = result;
						value = new VerificationResult<TAPNNetworkTrace>(
								approxResult.getQueryResult(),
								decomposeTrace(approxResult.getTrace(), transformedModel.value2()),
								decomposeTrace(approxResult.getSecondaryTrace(), transformedModel.value2()),
								approxResult.verificationTime(),
								approxResult.stats(),
								approxResult.isOverApproximationResult());
						value.setNameMapping(transformedModel.value2());
						
					}
			}
		} 
		else if (options.enableUnderApproximation()) {
			// Under-approximation
			
			if (result.getTrace() != null) {
				for (TimedArcPetriNetStep k : result.getTrace()) {
					if (k instanceof TimeDelayStep){
						((TimeDelayStep) k).setDelay(((TimeDelayStep) k).delay().multiply(new BigDecimal(options.approximationDenominator())));
					}
					else if (k instanceof TimedTransitionStep) {
						for (TimedToken a : ((TimedTransitionStep) k).consumedTokens()){
							a.setAge(a.age().multiply(new BigDecimal(options.approximationDenominator())));
						}
					}
				}
			}
			
			if (options.approximationDenominator() == 1) { 
				// If r = 1
				// No matter what EF and AG answered -> return that answer
				QueryResult queryResult= result.getQueryResult();
				value =  new VerificationResult<TAPNNetworkTrace>(
						queryResult,
						decomposeTrace(result.getTrace(), transformedModel.value2()),
						decomposeTrace(result.getSecondaryTrace(), transformedModel.value2()),
						result.verificationTime(),
						result.stats(),
						result.isOverApproximationResult());
				value.setNameMapping(transformedModel.value2());
			}
			else {
				// If r > 1
				if ((result.getQueryResult().queryType() == QueryType.EF || result.getQueryResult().queryType() == QueryType.EG) && ! result.getQueryResult().isQuerySatisfied()
				|| ((result.getQueryResult().queryType() == QueryType.AG || result.getQueryResult().queryType() == QueryType.AF) && result.getQueryResult().isQuerySatisfied())) {
					// If ((EF OR EG) AND not satisfied) OR ((AG OR AF) and satisfied) -> Inconclusive
					QueryResult queryResult= result.getQueryResult();
					queryResult.setApproximationInconclusive(true);
					value =  new VerificationResult<TAPNNetworkTrace>(
							queryResult,
							decomposeTrace(result.getTrace(), transformedModel.value2()),
							decomposeTrace(result.getSecondaryTrace(), transformedModel.value2()),
							result.verificationTime(),
							result.stats(),
							result.isOverApproximationResult());
					value.setNameMapping(transformedModel.value2());
				} else if ((result.getQueryResult().queryType() == QueryType.EF || result.getQueryResult().queryType() == QueryType.EG) && result.getQueryResult().isQuerySatisfied()
						|| ((result.getQueryResult().queryType() == QueryType.AG || result.getQueryResult().queryType() == QueryType.AF) && ! result.getQueryResult().isQuerySatisfied())) {
					// ((EF OR EG) AND satisfied) OR ((AG OR AF) and not satisfied) -> Check for deadlock
					
					if ( ! query.hasDeadlock() && result.getQueryResult().queryType() != QueryType.EG && result.getQueryResult().queryType() != QueryType.AF) {
						// If query does not have deadlock and are of the type EF or AG -> return answer from result
						QueryResult queryResult= result.getQueryResult();
						value =  new VerificationResult<TAPNNetworkTrace>(
								queryResult,
								decomposeTrace(result.getTrace(), transformedModel.value2()),
								decomposeTrace(result.getSecondaryTrace(), transformedModel.value2()),
								result.verificationTime(),
								result.stats(),
								result.isOverApproximationResult());
						value.setNameMapping(transformedModel.value2());
					} else if (result.getTrace() != null){
						// If query does have deadlock or EG or AF a trace -> create trace TAPN
						//Create the verification satisfied result for the approximation
						VerificationResult<TimedArcPetriNetTrace> approxResult = result;
						value = new VerificationResult<TAPNNetworkTrace>(
								approxResult.getQueryResult(),
								decomposeTrace(approxResult.getTrace(), transformedModel.value2()),
								decomposeTrace(approxResult.getSecondaryTrace(), transformedModel.value2()),
								approxResult.verificationTime(),
								approxResult.stats(),
								result.isOverApproximationResult());
						value.setNameMapping(transformedModel.value2());
						
						OverApproximation overaprx = new OverApproximation();
						
			
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
							options.setTraceOption(oldTraceOption);
							// if the old traceoption was none, we need to set the results traces to null so GUI doesn't try to display the traces later
							if (oldTraceOption == TraceOption.NONE){
								value.setTrace(null);
								value.setSecondaryTrace(null);
							}
							MemoryMonitor.setCumulativePeakMemory(false);
							return new VerificationResult<TAPNNetworkTrace>(result.errorMessage(), result.verificationTime());
						}
						//Create the result from trace TAPN
						renameTraceTransitions(result.getTrace());
						renameTraceTransitions(result.getSecondaryTrace());
						QueryResult queryResult = result.getQueryResult();
						
						
						// If (EF AND not satisfied trace) OR (AG AND satisfied trace) -> inconclusive
						if ((result.getQueryResult().queryType() == QueryType.EF && !queryResult.isQuerySatisfied())
							|| result.getQueryResult().queryType() == QueryType.AG && queryResult.isQuerySatisfied()) {
							queryResult.setApproximationInconclusive(true);
						}
						
						// If (EF AND satisfied trace) OR (AG AND satisfied trace) -> Return result
						// This is satisfied for EF and not satisfied for AG
						value = new VerificationResult<TAPNNetworkTrace>(
								queryResult,
								decomposeTrace(result.getTrace(), transformedModel.value2()),
								decomposeTrace(result.getSecondaryTrace(), transformedModel.value2()),
								approxResult.verificationTime() + result.verificationTime(),
								approxResult.stats(),
								approxResult.isOverApproximationResult());
						value.setNameMapping(transformedModel.value2());
					}
					else {
						// the query contains deadlock, but we do not have a trace.
						QueryResult queryResult = result.getQueryResult();
						queryResult.setApproximationInconclusive(true);
						
						VerificationResult<TimedArcPetriNetTrace> approxResult = result;
						value = new VerificationResult<TAPNNetworkTrace>(
								approxResult.getQueryResult(),
								decomposeTrace(approxResult.getTrace(), transformedModel.value2()),
								decomposeTrace(approxResult.getSecondaryTrace(), transformedModel.value2()),
								approxResult.verificationTime(),
								approxResult.stats(),
								approxResult.isOverApproximationResult());
						value.setNameMapping(transformedModel.value2());
					}
				}
			}
		} 
		else {
			value =  new VerificationResult<TAPNNetworkTrace>(
					result.getQueryResult(),
					decomposeTrace(result.getTrace(), transformedModel.value2()),
					decomposeTrace(result.getSecondaryTrace(), transformedModel.value2()),
					result.verificationTime(),
					result.stats(),
					result.isOverApproximationResult());
			value.setNameMapping(transformedModel.value2());
		}
		
		options.setTraceOption(oldTraceOption);
		// if the old traceoption was none, we need to set the results traces to null so GUI doesn't try to display the traces later
		if (oldTraceOption == TraceOption.NONE){
			value.setTrace(null);
			value.setSecondaryTrace(null);
		}
		MemoryMonitor.setCumulativePeakMemory(false);
		
		return value;
	}
	
	private void renameTraceTransitions(TimedArcPetriNetTrace trace) {
		if (trace != null){
			trace.reduceTraceForOriginalNet("_traceNet_", "PTRACE");
			trace.removeTokens("PBLOCK");
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
