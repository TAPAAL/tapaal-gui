package dk.aau.cs.verification.batchProcessing;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingWorker;
import pipe.dataLayer.TAPNQuery.SearchOption;
import pipe.dataLayer.TAPNQuery.TraceOption;
import pipe.dataLayer.TAPNQuery.WorkflowMode;
import pipe.gui.CreateGui;
import pipe.gui.FileFinder;
import pipe.gui.MessengerImpl;
import pipe.gui.Verifier;
import pipe.gui.widgets.QueryPane;
import dk.aau.cs.Messenger;
import dk.aau.cs.TCTL.TCTLAGNode;
import dk.aau.cs.TCTL.TCTLEFNode;
import dk.aau.cs.TCTL.TCTLEGNode;
import dk.aau.cs.TCTL.TCTLPlaceNode;
import dk.aau.cs.TCTL.TCTLAbstractProperty;
import dk.aau.cs.TCTL.TCTLAtomicPropositionNode;
import dk.aau.cs.TCTL.TCTLConstNode;
import dk.aau.cs.TCTL.TCTLTrueNode;
import dk.aau.cs.TCTL.TCTLDeadlockNode;
import dk.aau.cs.TCTL.visitors.RenameAllPlacesVisitor;
import dk.aau.cs.TCTL.visitors.RenameAllTransitionsVisitor;
import dk.aau.cs.approximation.ApproximationWorker;
import dk.aau.cs.gui.components.BatchProcessingResultsTableModel;
import dk.aau.cs.io.batchProcessing.BatchProcessingModelLoader;
import dk.aau.cs.io.batchProcessing.LoadedBatchProcessingModel;
import dk.aau.cs.model.tapn.Bound;
import dk.aau.cs.model.tapn.TAPNQuery;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.simulation.TAPNNetworkTrace;
import dk.aau.cs.model.tapn.simulation.TimedArcPetriNetTrace;
import dk.aau.cs.model.tapn.simulation.TimedTAPNNetworkTrace;
import dk.aau.cs.translations.ReductionOption;
import dk.aau.cs.util.MemoryMonitor;
import dk.aau.cs.util.Require;
import dk.aau.cs.util.Tuple;
import dk.aau.cs.util.UnsupportedModelException;
import dk.aau.cs.util.UnsupportedQueryException;
import dk.aau.cs.util.VerificationCallback;
import dk.aau.cs.verification.ITAPNComposer;
import dk.aau.cs.verification.ModelChecker;
import dk.aau.cs.verification.NameMapping;
import dk.aau.cs.verification.NullStats;
import dk.aau.cs.verification.QueryType;
import dk.aau.cs.verification.Stats;
import dk.aau.cs.verification.TAPNComposer;
import dk.aau.cs.verification.TAPNTraceDecomposer;
import dk.aau.cs.verification.VerificationOptions;
import dk.aau.cs.verification.VerificationResult;
import dk.aau.cs.verification.UPPAAL.Verifyta;
import dk.aau.cs.verification.UPPAAL.VerifytaOptions;
import dk.aau.cs.verification.VerifyTAPN.TraceType;
import dk.aau.cs.verification.VerifyTAPN.VerifyDTAPNOptions;
import dk.aau.cs.verification.VerifyTAPN.VerifyPN;
import dk.aau.cs.verification.VerifyTAPN.VerifyPNOptions;
import dk.aau.cs.verification.VerifyTAPN.VerifyTAPN;
import dk.aau.cs.verification.VerifyTAPN.VerifyTAPNDiscreteVerification;
import dk.aau.cs.verification.VerifyTAPN.VerifyTAPNOptions;
import dk.aau.cs.verification.batchProcessing.BatchProcessingVerificationOptions.ApproximationMethodOption;
import dk.aau.cs.verification.batchProcessing.BatchProcessingVerificationOptions.QueryPropertyOption;
import dk.aau.cs.verification.batchProcessing.BatchProcessingVerificationOptions.StubbornReductionOption;
import dk.aau.cs.verification.batchProcessing.BatchProcessingVerificationOptions.SymmetryOption;
import pipe.dataLayer.TAPNQuery.ExtrapolationOption;
import pipe.dataLayer.TAPNQuery.QueryCategory;

public class BatchProcessingWorker extends SwingWorker<Void, BatchProcessingVerificationResult> {
	private List<File> files;
	private final BatchProcessingResultsTableModel tableModel;
	private BatchProcessingVerificationOptions batchProcessingVerificationOptions;
	private boolean isExiting = false;
	private ModelChecker modelChecker;
	List<BatchProcessingListener> listeners = new ArrayList<BatchProcessingListener>();
	private boolean skippingCurrentVerification = false;
	private boolean timeoutCurrentVerification = false;
	private boolean oomCurrentVerification = false;
	private int verificationTasksCompleted;
	private LoadedBatchProcessingModel model;
	private boolean isSoundnessCheck;
	private boolean isModelCheckOnly;
	private ArrayList<File> filesProcessed;
	
	
	public BatchProcessingWorker(List<File> files, BatchProcessingResultsTableModel tableModel, BatchProcessingVerificationOptions batchProcessingVerificationOptions) {
		super();
		this.files = files;
		this.tableModel = tableModel;
		this.batchProcessingVerificationOptions = batchProcessingVerificationOptions;
		
	}

	public synchronized void notifyExiting(){
		isExiting = true;
	}
	
	private synchronized boolean exiting(){
		return isExiting;
	}
	
	public synchronized void notifySkipCurrentVerification() {
		skippingCurrentVerification = true;
		if(modelChecker != null) {
			modelChecker.kill();
		}
	}
	
	public synchronized void notifyTimeoutCurrentVerificationTask() {
		timeoutCurrentVerification = true;
		if(modelChecker != null) {
			modelChecker.kill();
		}
	}
	
	public synchronized void notifyOOMCurrentVerificationTask() {
		oomCurrentVerification = true;
		if(modelChecker != null) {
			modelChecker.kill();
		}
	}
	
	
	@Override
	protected Void doInBackground() throws Exception {
		isSoundnessCheck = false;
		filesProcessed = new ArrayList<File>();
		for(File file : files){

			fireFileChanged(file.getName());
			LoadedBatchProcessingModel model = loadModel(file);
			this.model = model;
			if(model != null) {	
                Tuple<TimedArcPetriNet, NameMapping> composedModel = composeModel(model);
				for(pipe.dataLayer.TAPNQuery query : model.queries()) {
                    if(exiting()) {
                        return null;
                    }
                    //For "Search whole state space", "Existence of deadlock", "Soundness" and "Strong Soundness" 
                    //the file should only be checked once instead of checking every query
                    if(isModelCheckOnly && filesProcessed.contains(file)) {
                    	continue;
                    }
                                        
					pipe.dataLayer.TAPNQuery queryToVerify = overrideVerificationOptions(composedModel.value1(), query, file);
					
					if (batchProcessingVerificationOptions.isReductionOptionUserdefined()){
						processQueryForUserdefinedReductions(file, composedModel, queryToVerify);
					} else {
						processQuery(file, composedModel, queryToVerify);
					}
				}
				if(model.queries().isEmpty() && batchProcessingVerificationOptions.queryPropertyOption() != QueryPropertyOption.KeepQueryOption) {
					pipe.dataLayer.TAPNQuery queryToVerify = createQueryFromQueryPropertyOption(composedModel.value1(), batchProcessingVerificationOptions.queryPropertyOption(), file);
					processQuery(file, composedModel, queryToVerify);
				}
			}
		}
		fireFileChanged("");
		fireStatusChanged("");
		return null;
	}

	private void processQueryForUserdefinedReductions(File file, Tuple<TimedArcPetriNet, NameMapping> composedModel, pipe.dataLayer.TAPNQuery queryToVerify) throws Exception {
		pipe.dataLayer.TAPNQuery query = queryToVerify;
		query.setDiscreteInclusion(false);
		if(!exiting() && batchProcessingVerificationOptions.reductionOptions().contains(ReductionOption.VerifyTAPN)){
			query = query.copy();
			query.setReductionOption(ReductionOption.VerifyTAPN);
			processQuery(file, composedModel, query);
		}
		
		if(!exiting() && batchProcessingVerificationOptions.discreteInclusion()){
			query = query.copy();
			query.setReductionOption(ReductionOption.VerifyTAPN);
			query.setDiscreteInclusion(true);
			processQuery(file, composedModel, query);
		}
		
		//Make the PTrie/timedarts availible 
		//TODO This shold be made simpler in the engine refacter process
		query = query.copy();
		query.setDiscreteInclusion(false);
		query.setReductionOption(ReductionOption.VerifyTAPNdiscreteVerification);
		if(!exiting() && batchProcessingVerificationOptions.useTimeDartPTrie()){
			query = query.copy();
			query.setUseTimeDarts(true);
			query.setUsePTrie(true);
			processQuery(file, composedModel, query);
		}
		
		if(!exiting() && batchProcessingVerificationOptions.useTimeDart()){
			query = query.copy();
			query.setUseTimeDarts(true);
			query.setUsePTrie(false);
			processQuery(file, composedModel, query);
		}
		
		if(!exiting() && batchProcessingVerificationOptions.usePTrie()){
			query = query.copy();
			query.setUseTimeDarts(false);
			query.setUsePTrie(true);
			processQuery(file, composedModel, query);
		}
		
		if(!exiting() && batchProcessingVerificationOptions.reductionOptions().contains(ReductionOption.VerifyTAPNdiscreteVerification)){
			query = query.copy();
			query.setUseTimeDarts(false);
			query.setUsePTrie(false);
			processQuery(file, composedModel, query);
		}
		
		// VerifyTA reductions
		query = query.copy();
		query.setDiscreteInclusion(false);
		for(ReductionOption r : batchProcessingVerificationOptions.reductionOptions()){
			if(r == ReductionOption.VerifyTAPN || r == ReductionOption.VerifyTAPNdiscreteVerification || r == ReductionOption.VerifyPNApprox || r == ReductionOption.VerifyPN || r == ReductionOption.VerifyPNReduce) { continue; }
			if(exiting()) return;
			query = query.copy();
			query.setReductionOption(r);
			
			// UPPAAL does not recognize: SearchOption.DEFAULT
			if(query.getSearchOption() == SearchOption.DEFAULT){
				query.setSearchOption(SearchOption.HEURISTIC);
			}
			
			processQuery(file, composedModel, query);
		}
		
		// VerifyPN reductions
		if(!exiting() && batchProcessingVerificationOptions.reductionOptions().contains(ReductionOption.VerifyPN)){
			query = query.copy();
			query.setReductionOption(ReductionOption.VerifyPN);
			query.setUseOverApproximation(false);
			query.setUseReduction(false);
			processQuery(file, composedModel, query);
		}
		
		if(!exiting() && batchProcessingVerificationOptions.reductionOptions().contains(ReductionOption.VerifyPNApprox)){
			query = query.copy();
			query.setReductionOption(ReductionOption.VerifyPNApprox);
			query.setUseOverApproximation(true);
			query.setUseReduction(false);
			processQuery(file, composedModel, query);
		}
		
		if(!exiting() && batchProcessingVerificationOptions.reductionOptions().contains(ReductionOption.VerifyPNReduce)){
			query = query.copy();
			query.setReductionOption(ReductionOption.VerifyPNReduce);
			query.setUseOverApproximation(false);
			query.setUseReduction(true);
			processQuery(file, composedModel, query);
		}
	}

	private void processQuery(File file, Tuple<TimedArcPetriNet, NameMapping> composedModel, pipe.dataLayer.TAPNQuery queryToVerify) throws Exception {
		if(queryToVerify.isActive()) {

			if(isSoundnessCheck) {
				processSoundnessCheck(file, composedModel, queryToVerify);
			}
			if(!isSoundnessCheck) { 
				VerificationResult<TimedArcPetriNetTrace> verificationResult = verifyQuery(file, composedModel, queryToVerify);
			
				if(verificationResult != null)
					processVerificationResult(file, queryToVerify, verificationResult);
			}
		}
		else
			publishResult(file.getName(), queryToVerify, "Skipped - query is disabled because it contains propositions involving places from a deactivated component", 0, new NullStats());
		
		fireVerificationTaskComplete();
	}
	private void processSoundnessCheck(final File file, Tuple<TimedArcPetriNet, NameMapping> composedModel, final pipe.dataLayer.TAPNQuery queryToVerify) throws Exception{
		VerificationResult<TimedArcPetriNetTrace> verificationResult;
		if(queryToVerify.getWorkflowMode() == WorkflowMode.WORKFLOW_SOUNDNESS) {
			try {
				verificationResult = verifyQuery(file, composedModel, queryToVerify);
				if(verificationResult != null)
					processVerificationResult(file, queryToVerify, verificationResult);
			} catch (Exception e) {
				publishResult(file.getName(), queryToVerify, "Skipped - model not supported by the verification method. Try running workflow analysis from the menu.", 0, new NullStats());
			}
		}
		if(queryToVerify.getWorkflowMode() == WorkflowMode.WORKFLOW_STRONG_SOUNDNESS) {
			//Test for Soundness before Strong Soundness
			pipe.dataLayer.TAPNQuery queryToCheckIfSound = new pipe.dataLayer.TAPNQuery(
					"Workflow soundness check", queryToVerify.getCapacity(),
					new TCTLEFNode(new TCTLTrueNode()), TraceOption.SOME,
					SearchOption.DEFAULT,
					ReductionOption.VerifyTAPNdiscreteVerification, true, true,
					false, true, false, null, ExtrapolationOption.AUTOMATIC,
					WorkflowMode.WORKFLOW_SOUNDNESS);
			long time = 0;
			try {
				final VerificationResult<TimedArcPetriNetTrace> resultOfSoundCheck = verifyQuery(file, composedModel, queryToCheckIfSound);
				//Strong Soundness check
				if(resultOfSoundCheck.isQuerySatisfied()) {
					long m = resultOfSoundCheck.stats().exploredStates();
					long B = 0;
					for (TimedPlace p : composedModel.value1().places()) {
						if (p.invariant().upperBound().equals(Bound.Infinity)) {
							continue;
						}
						B = Math.max(B, p.invariant().upperBound().value());
					}
					long c  = m*B+1;
					queryToVerify.setStrongSoundnessBound(c);
					Verifier.runVerifyTAPNVerification(model.network(), queryToVerify, new VerificationCallback() {
						
						@Override
						public void run() {							
						}
						
						@Override
						public void run(VerificationResult<TAPNNetworkTrace> result) {
							TraceType traceType = ((TimedTAPNNetworkTrace) result.getTrace()).getTraceType();
							if(traceType == TraceType.EG_DELAY_FOREVER || traceType == TraceType.EG_LOOP) {
								publishResult(file.getName(), queryToVerify, "Not Strongly Sound", result.verificationTime(), result.stats());
							} else
								publishResult(file.getName(), queryToVerify, "Strongly Sound", result.verificationTime(), result.stats());
						}
					});

				} else
					publishResult(file.getName(), queryToVerify, "Not Strongly Sound", resultOfSoundCheck.verificationTime(), resultOfSoundCheck.stats());
				
			} catch (Exception e) {
				publishResult(file.getName(), queryToVerify, "Skipped - model is not a workflow net. Try running workflow analysis from the menu.", time, new NullStats());
			}
		}
	}

	private pipe.dataLayer.TAPNQuery overrideVerificationOptions(TimedArcPetriNet model, pipe.dataLayer.TAPNQuery query, File fileToBeChecked) throws Exception {
		if(batchProcessingVerificationOptions != null) {
			int capacity = batchProcessingVerificationOptions.KeepCapacityFromQuery() ? query.getCapacity() : batchProcessingVerificationOptions.capacity();
			if(!(batchProcessingVerificationOptions.queryPropertyOption() == QueryPropertyOption.StrongSoundness || batchProcessingVerificationOptions.queryPropertyOption() == QueryPropertyOption.Soundness)) {
				SearchOption search = batchProcessingVerificationOptions.searchOption() == SearchOption.BatchProcessingKeepQueryOption ? query.getSearchOption() : batchProcessingVerificationOptions.searchOption();
				ReductionOption option = query.getReductionOption();
	            TCTLAbstractProperty property;
	            String name;
	            if (batchProcessingVerificationOptions.queryPropertyOption() == QueryPropertyOption.ExistDeadlock) {
					property = generateExistDeadlock(model);
					name = "Existence of a deadlock";
					filesProcessed.add(fileToBeChecked);
					isModelCheckOnly = true;
	            } else if (batchProcessingVerificationOptions.queryPropertyOption() == QueryPropertyOption.SearchWholeStateSpace) {
	            	property = generateSearchWholeStateSpaceProperty(model);
	                name = "Search whole state space";
	                filesProcessed.add(fileToBeChecked);
	                isModelCheckOnly = true;
	            } else {
	                property = query.getProperty();
	                name = query.getName();
	            }
				boolean symmetry = batchProcessingVerificationOptions.symmetry() == SymmetryOption.KeepQueryOption ? query.useSymmetry() : getSymmetryFromBatchProcessingOptions();
				boolean stubbornReduction = batchProcessingVerificationOptions.stubbornReductionOption() == StubbornReductionOption.KeepQueryOption ? query.isStubbornReductionEnabled() : getStubbornReductionFromBatchProcessingOptions();
				boolean overApproximation = query.isOverApproximationEnabled();
				boolean underApproximation = query.isUnderApproximationEnabled();
				int approximationDenominator = query.approximationDenominator();
				if (batchProcessingVerificationOptions.approximationMethodOption() == ApproximationMethodOption.None) {
					overApproximation = false;
					underApproximation = false;
				} else if (batchProcessingVerificationOptions.approximationMethodOption() == ApproximationMethodOption.OverApproximation) {
					overApproximation = true;
					underApproximation = false;
				} else if (batchProcessingVerificationOptions.approximationMethodOption() == ApproximationMethodOption.UnderApproximation) {
					overApproximation = false;
					underApproximation = true;
				}
				if (batchProcessingVerificationOptions.approximationDenominator() != 0) {
					approximationDenominator = batchProcessingVerificationOptions.approximationDenominator();
				}
				
				pipe.dataLayer.TAPNQuery changedQuery = new pipe.dataLayer.TAPNQuery(name, capacity, property, TraceOption.NONE, search, option, symmetry, false, query.useTimeDarts(), query.usePTrie(), query.useOverApproximation(), query.useReduction(),  query.getHashTableSize(), query.getExtrapolationOption(), query.inclusionPlaces(), overApproximation, underApproximation, approximationDenominator);
				
				if(batchProcessingVerificationOptions.queryPropertyOption() == QueryPropertyOption.KeepQueryOption)
					changedQuery.setActive(query.isActive());
	
				changedQuery.setCategory(query.getCategory());
				changedQuery.setAlgorithmOption(query.getAlgorithmOption());
				changedQuery.setUseSiphontrap(query.isSiphontrapEnabled());
				changedQuery.setUseQueryReduction(query.isQueryReductionEnabled());
				changedQuery.setUseStubbornReduction(stubbornReduction);
				return changedQuery;
			} else if (batchProcessingVerificationOptions.queryPropertyOption() == QueryPropertyOption.Soundness) {
				isSoundnessCheck = true;
				query = new pipe.dataLayer.TAPNQuery(
					"Workflow soundness check", capacity,
							new TCTLEFNode(new TCTLTrueNode()), TraceOption.SOME,
							SearchOption.DEFAULT,
							ReductionOption.VerifyTAPNdiscreteVerification, true, true,
							false, true, false, null, ExtrapolationOption.AUTOMATIC,
							WorkflowMode.WORKFLOW_SOUNDNESS);
				filesProcessed.add(fileToBeChecked);
				isModelCheckOnly = true;
        	
	        } else if (batchProcessingVerificationOptions.queryPropertyOption() == QueryPropertyOption.StrongSoundness) {
	        	isSoundnessCheck = true;
	        	query = new pipe.dataLayer.TAPNQuery(
						"Workflow soundness check", capacity,
								new TCTLEGNode(new TCTLTrueNode()), TraceOption.SOME,
								SearchOption.DEFAULT,
								ReductionOption.VerifyTAPNdiscreteVerification, true, true,
								false, true, false, null, ExtrapolationOption.AUTOMATIC,
								WorkflowMode.WORKFLOW_STRONG_SOUNDNESS);
				filesProcessed.add(fileToBeChecked);
				isModelCheckOnly = true;
			}
		}
		
		return query;
	}
	
	private pipe.dataLayer.TAPNQuery createQueryFromQueryPropertyOption(TimedArcPetriNet model, QueryPropertyOption option, File fileToBeChecked) throws Exception {
		int capacity = batchProcessingVerificationOptions.capacity();
		ReductionOption reductionOption = model.isUntimed() ? ReductionOption.VerifyPN : ReductionOption.VerifyTAPN;
		if(option == QueryPropertyOption.ExistDeadlock) {
			filesProcessed.add(fileToBeChecked);
			return new pipe.dataLayer.TAPNQuery(
					"Existence of a deadlock", capacity,
							generateExistDeadlock(model), TraceOption.NONE,
							SearchOption.DEFAULT,
							reductionOption, true, true,
							false, true, false, null, ExtrapolationOption.AUTOMATIC,
							WorkflowMode.WORKFLOW_SOUNDNESS);
		}
		if(option == QueryPropertyOption.SearchWholeStateSpace) {
			filesProcessed.add(fileToBeChecked);
			return new pipe.dataLayer.TAPNQuery(
					"Search whole state space", capacity,
							generateSearchWholeStateSpaceProperty(model), TraceOption.NONE,
							SearchOption.DEFAULT,
							reductionOption, true, true,
							false, true, false, null, ExtrapolationOption.AUTOMATIC,
							WorkflowMode.WORKFLOW_SOUNDNESS);
		}
		if (option == QueryPropertyOption.Soundness) {
			isSoundnessCheck = true;
			filesProcessed.add(fileToBeChecked);
			return new pipe.dataLayer.TAPNQuery(
				"Workflow soundness check", capacity,
						new TCTLEFNode(new TCTLTrueNode()), TraceOption.SOME,
						SearchOption.DEFAULT,
						ReductionOption.VerifyTAPNdiscreteVerification, true, true,
						false, true, false, null, ExtrapolationOption.AUTOMATIC,
						WorkflowMode.WORKFLOW_SOUNDNESS);
		}
		if(option == QueryPropertyOption.StrongSoundness) {
			isSoundnessCheck = true;
			filesProcessed.add(fileToBeChecked);
        	return new pipe.dataLayer.TAPNQuery(
					"Workflow soundness check", capacity,
							new TCTLEGNode(new TCTLTrueNode()), TraceOption.SOME,
							SearchOption.DEFAULT,
							ReductionOption.VerifyTAPNdiscreteVerification, true, true,
							false, true, false, null, ExtrapolationOption.AUTOMATIC,
							WorkflowMode.WORKFLOW_STRONG_SOUNDNESS);
		}
		return null;
	}

	private boolean getSymmetryFromBatchProcessingOptions() {
		return batchProcessingVerificationOptions.symmetry() == SymmetryOption.Yes;
	}
        
	private boolean getStubbornReductionFromBatchProcessingOptions(){
		return batchProcessingVerificationOptions.stubbornReductionOption() == StubbornReductionOption.Yes;
	}

	private Tuple<TimedArcPetriNet, NameMapping> composeModel(LoadedBatchProcessingModel model) {
		ITAPNComposer composer = new TAPNComposer(new Messenger(){
			public void displayInfoMessage(String message) { }
			public void displayInfoMessage(String message, String title) {}
			public void displayErrorMessage(String message) {}
			public void displayErrorMessage(String message, String title) {}
			public void displayWrappedErrorMessage(String message, String title) {}
			
		}, false);
		Tuple<TimedArcPetriNet, NameMapping> composedModel = composer.transformModel(model.network());
		return composedModel;
	}

	private VerificationResult<TimedArcPetriNetTrace> verifyQuery(File file, Tuple<TimedArcPetriNet, NameMapping> composedModel, pipe.dataLayer.TAPNQuery query) throws Exception {
		fireStatusChanged(query.getName());
		
		VerificationResult<TimedArcPetriNetTrace> verificationResult = null;
		try {
			verificationResult = verify(composedModel, query);
		} catch(UnsupportedModelException e) {
			if(isSoundnessCheck)
				publishResult(file.getName(), query, "Skipped - model is not a workflow net. Try running workflow analysis from the menu.", 0, new NullStats());
			else
				publishResult(file.getName(), query, "Skipped - model not supported by the verification method", 0, new NullStats());
			return null;
		} catch(UnsupportedQueryException e) {
			if(e.getMessage().toLowerCase().contains("discrete inclusion"))
				publishResult(file.getName(), query, "Skipped - discrete inclusion is enabled and query is not upward closed", 0, new NullStats());
			else
				publishResult(file.getName(), query, "Skipped - query not supported by the verification method", 0, new NullStats());
			return null;
		} 
		return verificationResult;
	}

	private void processVerificationResult(File file, pipe.dataLayer.TAPNQuery query, VerificationResult<TimedArcPetriNetTrace> verificationResult) {
		if(skippingCurrentVerification) {
			publishResult(file.getName(), query, "Skipped - by the user", verificationResult.verificationTime(), new NullStats());
			skippingCurrentVerification = false;
		} else if(timeoutCurrentVerification) {
			publishResult(file.getName(), query, "Skipped - due to timeout", verificationResult.verificationTime(), new NullStats());
			timeoutCurrentVerification = false;
		} else if(oomCurrentVerification) {
			publishResult(file.getName(), query, "Skipped - due to OOM", verificationResult.verificationTime(), new NullStats());
			oomCurrentVerification = false;
		} else if(!verificationResult.error()) {
			String queryResult = "";
			if (verificationResult.getQueryResult().isApproximationInconclusive())
			{
				queryResult = "Inconclusive";
			}
			else
			{
				queryResult = verificationResult.getQueryResult().isQuerySatisfied() ? "Satisfied" : "Not Satisfied";
				if(isSoundnessCheck && !verificationResult.isQuerySatisfied())
					queryResult = "Not Sound";
				if(isSoundnessCheck && verificationResult.isQuerySatisfied()) {
					if(query.getWorkflowMode() == WorkflowMode.WORKFLOW_STRONG_SOUNDNESS)
						queryResult = "Strongly Sound";
					else
						queryResult = "Sound";
				}
			}
			if (query.discreteInclusion() && !verificationResult.isBounded() && 
					((query.queryType().equals(QueryType.EF) && !verificationResult.getQueryResult().isQuerySatisfied())
					||
					(query.queryType().equals(QueryType.AG) && verificationResult.getQueryResult().isQuerySatisfied())))
			{queryResult = "Inconclusive";}
				if(query.getReductionOption().equals(ReductionOption.VerifyPNApprox) && 
						((query.queryType().equals(QueryType.EF) && verificationResult.getQueryResult().isQuerySatisfied()) ||
						(query.queryType().equals(QueryType.AG) && !verificationResult.getQueryResult().isQuerySatisfied()))){
					queryResult = "Inconclusive";
				}
			publishResult(file.getName(), query, queryResult,	verificationResult.verificationTime(), verificationResult.stats());
		} else if(isSoundnessCheck && verificationResult.error()) {
			publishResult(file.getName(), query, "Skipped - model is not a workflow net. Try running workflow analysis from the menu.", verificationResult.verificationTime(), new NullStats());
		} else {
			publishResult(file.getName(), query, "Error during verification", verificationResult.verificationTime(), new NullStats());
		}		
	}

	private void publishResult(String fileName, pipe.dataLayer.TAPNQuery query, String verificationResult, long verificationTime, Stats stats) {
		BatchProcessingVerificationResult result;		
		if(QueryPane.getTemporaryFile() != null && fileName.equals(QueryPane.getTemporaryFile().getName())) {
			//removes numbers from tempFile so it looks good
			result = new BatchProcessingVerificationResult(CreateGui.getAppGui().getCurrentTabName(), query, verificationResult, verificationTime, MemoryMonitor.getPeakMemory(), stats);
		} else {
			result = new BatchProcessingVerificationResult(fileName, query, verificationResult, verificationTime, MemoryMonitor.getPeakMemory(), stats);
		}
		publish(result);
	}

	
	private void renameTraceTransitions(TimedArcPetriNetTrace trace) {
		if (trace != null)
			trace.reduceTraceForOriginalNet("_traceNet_", "PTRACE");
	}

	private TAPNNetworkTrace decomposeTrace(TimedArcPetriNetTrace trace, NameMapping mapping) {
		if (trace == null)
			return null;

		TAPNTraceDecomposer decomposer = new TAPNTraceDecomposer(trace, model.network(), mapping);
		return decomposer.decompose();
	}
	
	private VerificationResult<TimedArcPetriNetTrace> verify(Tuple<TimedArcPetriNet, NameMapping> composedModel, pipe.dataLayer.TAPNQuery query) throws Exception {		
		TAPNQuery queryToVerify = getTAPNQuery(composedModel.value1(),query);
		queryToVerify.setCategory(query.getCategory());
		MapQueryToNewNames(queryToVerify, composedModel.value2());
		
		TAPNQuery clonedQuery = new TAPNQuery(query.getProperty().copy(), queryToVerify.getExtraTokens());
		clonedQuery.setCategory(query.getCategory());
		MapQueryToNewNames(clonedQuery, composedModel.value2());
		
		VerificationOptions options = getVerificationOptionsFromQuery(query);
		modelChecker = getModelChecker(query);
		fireVerificationTaskStarted();
		
		ApproximationWorker worker = new ApproximationWorker();
		return worker.batchWorker(composedModel, options, query, model, modelChecker, queryToVerify, clonedQuery, this);
	}

	private TAPNQuery getTAPNQuery(TimedArcPetriNet model, pipe.dataLayer.TAPNQuery query) throws Exception {
		return new TAPNQuery(query.getProperty().copy(), query.getCapacity());
	}

	private TCTLAbstractProperty generateSearchWholeStateSpaceProperty(TimedArcPetriNet model) throws Exception {
		TimedPlace p = model.places().iterator().next();
		if(p == null)
			throw new Exception("Model contains no places. This may not happen.");
		
		return new TCTLAGNode(new TCTLTrueNode());
	}
        
	private TCTLAbstractProperty generateExistDeadlock(TimedArcPetriNet model) throws Exception {
		return new TCTLEFNode(new TCTLDeadlockNode()); 
	}
	
	private ModelChecker getModelChecker(pipe.dataLayer.TAPNQuery query) {
		if(query.getReductionOption() == ReductionOption.VerifyTAPN)
			return getVerifyTAPN();
		else if(query.getReductionOption() == ReductionOption.VerifyTAPNdiscreteVerification)
			return getVerifyTAPNDiscreteVerification();
		else if(query.getReductionOption() == ReductionOption.VerifyPN || query.getReductionOption() == ReductionOption.VerifyPNApprox || query.getReductionOption() == ReductionOption.VerifyPNReduce)
			return getVerifyPN();
		else
			return getVerifyta();
	}

	public VerificationOptions getVerificationOptionsFromQuery(pipe.dataLayer.TAPNQuery query) {
		if(query.getReductionOption() == ReductionOption.VerifyTAPN)
			return new VerifyTAPNOptions(query.getCapacity(), TraceOption.NONE, query.getSearchOption(), query.useSymmetry(), false, query.discreteInclusion(), query.inclusionPlaces(), query.isOverApproximationEnabled(), query.isUnderApproximationEnabled(), query.approximationDenominator());	// XXX DISABLES OverApprox
		else if(query.getReductionOption() == ReductionOption.VerifyTAPNdiscreteVerification)
			return new VerifyDTAPNOptions(query.getCapacity(), TraceOption.NONE, query.getSearchOption(), query.useSymmetry(), query.useGCD(), query.useTimeDarts(), query.usePTrie(), false,  query.discreteInclusion(), query.inclusionPlaces(), query.getWorkflowMode(), 0, query.isOverApproximationEnabled(), query.isUnderApproximationEnabled(), query.approximationDenominator(), query.isStubbornReductionEnabled());
		else if(query.getReductionOption() == ReductionOption.VerifyPN || query.getReductionOption() == ReductionOption.VerifyPNApprox || query.getReductionOption() == ReductionOption.VerifyPNReduce)
			if (batchProcessingVerificationOptions.queryPropertyOption() == QueryPropertyOption.SearchWholeStateSpace){
			    return new VerifyPNOptions(query.getCapacity(), TraceOption.NONE, query.getSearchOption(), false, false, false, false, query.approximationDenominator(), QueryCategory.Default, query.getAlgorithmOption(), false, false, false);
			} else {
			    return new VerifyPNOptions(query.getCapacity(), TraceOption.NONE, query.getSearchOption(), query.useOverApproximation(), query.useReduction(), query.isOverApproximationEnabled(), query.isUnderApproximationEnabled(), query.approximationDenominator(), query.getCategory(), query.getAlgorithmOption(), query.isSiphontrapEnabled(), query.isQueryReductionEnabled(), query.isStubbornReductionEnabled());
			}
		else
			return new VerifytaOptions(TraceOption.NONE, query.getSearchOption(), false, query.getReductionOption(), query.useSymmetry(), false, query.isOverApproximationEnabled(), query.isUnderApproximationEnabled(), query.approximationDenominator());
	}
	
	private void MapQueryToNewNames(TAPNQuery query, NameMapping mapping) {
		RenameAllPlacesVisitor placeVisitor = new RenameAllPlacesVisitor(mapping);
                RenameAllTransitionsVisitor transitionVisitor = new RenameAllTransitionsVisitor(mapping);
		query.getProperty().accept(placeVisitor, null);
                query.getProperty().accept(transitionVisitor, null);
	}

	private Verifyta getVerifyta() {
		Verifyta verifyta = new Verifyta(new FileFinder(), new MessengerImpl());
		verifyta.setup();
		return verifyta;
	}

	private static VerifyTAPN getVerifyTAPN() {
		VerifyTAPN verifytapn = new VerifyTAPN(new FileFinder(), new MessengerImpl());
		verifytapn.setup();
		return verifytapn;
	}
	
	private static VerifyPN getVerifyPN() {
		VerifyPN verifypn = new VerifyPN(new FileFinder(), new MessengerImpl());
		verifypn.setup();
		return verifypn;
	}
	
	private static VerifyTAPNDiscreteVerification getVerifyTAPNDiscreteVerification() {
		VerifyTAPNDiscreteVerification verifytapnDiscreteVerification = new VerifyTAPNDiscreteVerification(new FileFinder(), new MessengerImpl());
		verifytapnDiscreteVerification.setup();
		return verifytapnDiscreteVerification;
	}
	
	private LoadedBatchProcessingModel loadModel(File modelFile) {
		fireStatusChanged("Loading model...");
		
		BatchProcessingModelLoader loader = new BatchProcessingModelLoader();
		try {
			return loader.load(modelFile);
		}
		catch(Exception e) {
			publishResult(modelFile.getName(), null, "Error loading model",	0, new NullStats());
			fireVerificationTaskComplete();
			return null;
		}
	}

	@Override
	protected void process(List<BatchProcessingVerificationResult> chunks) {
		for(BatchProcessingVerificationResult result : chunks){
			tableModel.addResult(result);
		}
	}
	
	@Override
	protected void done() {
		if(isCancelled()){
			if(modelChecker != null)
				modelChecker.kill();
		}
	}
	
	public void addBatchProcessingListener(BatchProcessingListener listener){
		Require.that(listener != null, "Listener cannot be null");
		listeners.add(listener);
	}

	public void removeBatchProcessingListener(BatchProcessingListener listener){
		Require.that(listener != null, "Listener cannot be null");
		listeners.remove(listener);
	}
	
	private void fireStatusChanged(String status) {
		for(BatchProcessingListener listener : listeners)
			listener.fireStatusChanged(new StatusChangedEvent(status));
	}
	
	private void fireFileChanged(String fileName) {
		for(BatchProcessingListener listener : listeners)
			listener.fireFileChanged(new FileChangedEvent(fileName));
	}
	
	private void fireVerificationTaskComplete() {
		verificationTasksCompleted++;
		for(BatchProcessingListener listener : listeners)
			listener.fireVerificationTaskComplete(new VerificationTaskCompleteEvent(verificationTasksCompleted));
	}
	
	private void fireVerificationTaskStarted() {
		for(BatchProcessingListener listener : listeners)
			listener.fireVerificationTaskStarted();
	}
}
