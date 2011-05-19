package dk.aau.cs.verification.batchProcessing;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingWorker;

import pipe.dataLayer.TAPNQuery.SearchOption;
import pipe.dataLayer.TAPNQuery.TraceOption;
import pipe.gui.FileFinderImpl;
import pipe.gui.MessengerImpl;

import dk.aau.cs.TCTL.TCTLAGNode;
import dk.aau.cs.TCTL.TCTLAbstractProperty;
import dk.aau.cs.TCTL.TCTLTrueNode;
import dk.aau.cs.TCTL.visitors.RenameAllPlacesVisitor;
import dk.aau.cs.TCTL.visitors.SimplifyPropositionsVisitor;
import dk.aau.cs.gui.components.BatchProcessingResultsTableModel;
import dk.aau.cs.io.batchProcessing.BatchProcessingModelLoader;
import dk.aau.cs.io.batchProcessing.LoadedBatchProcessingModel;
import dk.aau.cs.model.tapn.TAPNQuery;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.simulation.TimedArcPetriNetTrace;
import dk.aau.cs.translations.ReductionOption;
import dk.aau.cs.util.Require;
import dk.aau.cs.util.Tuple;
import dk.aau.cs.util.UnsupportedModelException;
import dk.aau.cs.util.UnsupportedQueryException;
import dk.aau.cs.verification.ModelChecker;
import dk.aau.cs.verification.NameMapping;
import dk.aau.cs.verification.TAPNComposer;
import dk.aau.cs.verification.VerificationOptions;
import dk.aau.cs.verification.VerificationResult;
import dk.aau.cs.verification.UPPAAL.Verifyta;
import dk.aau.cs.verification.UPPAAL.VerifytaOptions;
import dk.aau.cs.verification.VerifyTAPN.VerifyTAPN;
import dk.aau.cs.verification.VerifyTAPN.VerifyTAPNOptions;
import dk.aau.cs.verification.batchProcessing.BatchProcessingVerificationOptions.QueryPropertyOption;
import dk.aau.cs.verification.batchProcessing.BatchProcessingVerificationOptions.SymmetryOption;


public class BatchProcessingWorker extends SwingWorker<Void, BatchProcessingVerificationResult> {
	private List<File> files;
	private final BatchProcessingResultsTableModel tableModel;
	private BatchProcessingVerificationOptions batchProcessingVerificationOptions;
	private boolean isExiting = false;
	private ModelChecker modelChecker;
	List<BatchProcessingListener> listeners = new ArrayList<BatchProcessingListener>();
	private boolean skippingCurrentVerification = false;
	private boolean timeoutCurrentVerification = false;
	private int verificationTasksCompleted;
	

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
	
	
	@Override
	protected Void doInBackground() throws Exception {
		for(File file : files){
			if(exiting()) {
				return null;
			}
			
			fireFileChanged(file.getName());
			LoadedBatchProcessingModel model = loadModel(file);
			if(model != null) {
				Tuple<TimedArcPetriNet, NameMapping> composedModel = composeModel(model);
				
				for(pipe.dataLayer.TAPNQuery query : model.queries()) {
					
					pipe.dataLayer.TAPNQuery queryToVerify = overrideVerificationOptions(composedModel.value1(), query);
					
					if(queryToVerify.getReductionOption() == ReductionOption.BatchProcessingAllReductions)
						processQueryForAllReductions(file,composedModel, queryToVerify);
					else
						processQuery(file, composedModel, queryToVerify);
					
				}
			}
		}
		fireFileChanged("");
		fireStatusChanged("Done");
		return null;
	}

	private void processQueryForAllReductions(File file, Tuple<TimedArcPetriNet, NameMapping> composedModel, pipe.dataLayer.TAPNQuery queryToVerify) throws Exception {
		pipe.dataLayer.TAPNQuery query = queryToVerify.copy();
		query.setReductionOption(ReductionOption.VerifyTAPN);
		query.setDiscreteInclusion(false);
		processQuery(file, composedModel, query);
		
		query = query.copy();
		query.setDiscreteInclusion(true);
		processQuery(file, composedModel, query);

		query = query.copy();
		query.setReductionOption(ReductionOption.STANDARD);
		query.setDiscreteInclusion(false);
		processQuery(file, composedModel, query);
		
		query = query.copy();
		query.setReductionOption(ReductionOption.OPTIMIZEDSTANDARD);
		processQuery(file, composedModel, query);
		
		query = query.copy();
		query.setReductionOption(ReductionOption.BROADCAST);
		processQuery(file, composedModel, query);
		
		query = query.copy();
		query.setReductionOption(ReductionOption.DEGREE2BROADCAST);
		processQuery(file, composedModel, query);
	}

	private void processQuery(File file, Tuple<TimedArcPetriNet, NameMapping> composedModel, pipe.dataLayer.TAPNQuery queryToVerify) throws Exception {
		fireVerificationTaskStarted();
		if(queryToVerify.isActive()) { 
			VerificationResult<TimedArcPetriNetTrace> verificationResult = verifyQuery(file, composedModel, queryToVerify);
			
			if(verificationResult != null)
				processVerificationResult(file, queryToVerify, verificationResult);
		}
		else
			publishResult(file.getName(), queryToVerify, "Skipped - Query is disabled because it contains propositions involving places from a deactivated component", 0);
		fireVerificationTaskComplete();
	}

	private pipe.dataLayer.TAPNQuery overrideVerificationOptions(TimedArcPetriNet model, pipe.dataLayer.TAPNQuery query) throws Exception {
		if(batchProcessingVerificationOptions != null) {
			SearchOption search = batchProcessingVerificationOptions.searchOption() == SearchOption.BatchProcessingKeepQueryOption ? query.getSearchOption() : batchProcessingVerificationOptions.searchOption();
			ReductionOption option = batchProcessingVerificationOptions.reductionOption() == ReductionOption.BatchProcessingKeepQueryOption ? query.getReductionOption() : batchProcessingVerificationOptions.reductionOption();
			TCTLAbstractProperty property = batchProcessingVerificationOptions.queryPropertyOption() == QueryPropertyOption.KeepQueryOption ? query.getProperty() : generateSearchWholeStateSpaceProperty(model);
			boolean symmetry = batchProcessingVerificationOptions.symmetry() == SymmetryOption.KeepQueryOption ? query.useSymmetry() : getSymmetryFromBatchProcessingOptions();
			int capacity = batchProcessingVerificationOptions.KeepCapacityFromQuery() ? query.getCapacity() : batchProcessingVerificationOptions.capacity();
			String name = batchProcessingVerificationOptions.queryPropertyOption() == QueryPropertyOption.KeepQueryOption ? query.getName() : "Search Whole State Space"; 
			
			pipe.dataLayer.TAPNQuery changedQuery = new pipe.dataLayer.TAPNQuery(name, capacity, property, TraceOption.NONE, search, option, symmetry, query.getHashTableSize(), query.getExtrapolationOption());
			if(batchProcessingVerificationOptions.queryPropertyOption() == QueryPropertyOption.KeepQueryOption)
				changedQuery.setActive(query.isActive());
			
			if(changedQuery.getReductionOption() == ReductionOption.VerifyTAPN && batchProcessingVerificationOptions.discreteInclusion())
				changedQuery.setDiscreteInclusion(true);
			
			simplifyQuery(changedQuery);
			return changedQuery;
		}
		
		return query;
	}
	
	private void simplifyQuery(pipe.dataLayer.TAPNQuery query) {
		SimplifyPropositionsVisitor visitor = new SimplifyPropositionsVisitor();
		visitor.FindAndReplaceTrueAndFalsePropositions(query.getProperty());
	}

	private boolean getSymmetryFromBatchProcessingOptions() {
		if(batchProcessingVerificationOptions.symmetry() == SymmetryOption.Yes)
			return true;
		else
			return false;
	}

	private Tuple<TimedArcPetriNet, NameMapping> composeModel(LoadedBatchProcessingModel model) {
		TAPNComposer composer = new TAPNComposer();
		Tuple<TimedArcPetriNet, NameMapping> composedModel = composer.transformModel(model.network());
		return composedModel;
	}

	private VerificationResult<TimedArcPetriNetTrace> verifyQuery(File file, Tuple<TimedArcPetriNet, NameMapping> composedModel, pipe.dataLayer.TAPNQuery query) throws Exception {
		fireStatusChanged("Verifying query: " + query.getName() + "...");
		
		VerificationResult<TimedArcPetriNetTrace> verificationResult = null;
		try {
			verificationResult = verify(composedModel, query);
		} catch(UnsupportedModelException e) {
			publishResult(file.getName(), query, "Skipped - model was not supported by verification method", 0);
			return null;
		} catch(UnsupportedQueryException e) {
			if(e.getMessage().toLowerCase().contains("discrete inclusion"))
				publishResult(file.getName(), query, "Skipped -discrete inclusion is enabled and query is not upward closed", 0);
			else
				publishResult(file.getName(), query, "Skipped - query was not supported by verification method", 0);
			return null;
		}
		return verificationResult;
	}

	private void processVerificationResult(File file, pipe.dataLayer.TAPNQuery query, VerificationResult<TimedArcPetriNetTrace> verificationResult) {
		if(!verificationResult.error()) {
			String queryResult = verificationResult.getQueryResult().isQuerySatisfied() ? "Satisfied" : "Not Satisfied";
			publishResult(file.getName(), query, queryResult,	verificationResult.verificationTime());
		} else if(skippingCurrentVerification) {
			publishResult(file.getName(), query, "Skipped by user", verificationResult.verificationTime());
			skippingCurrentVerification = false;
		} else if(timeoutCurrentVerification) {
			publishResult(file.getName(), query, "Skipped due to timeout", verificationResult.verificationTime());
			timeoutCurrentVerification = false;
		} else {
			publishResult(file.getName(), query, "Error during verification", verificationResult.verificationTime());
		}		
	}

	private void publishResult(String fileName, pipe.dataLayer.TAPNQuery query, String verificationResult, long verificationTime) {
		BatchProcessingVerificationResult result = new BatchProcessingVerificationResult(fileName, query, verificationResult,verificationTime);
		publish(result);
	}

	private VerificationResult<TimedArcPetriNetTrace> verify(Tuple<TimedArcPetriNet, NameMapping> composedModel, pipe.dataLayer.TAPNQuery query) throws Exception {
		TAPNQuery queryToVerify = getTAPNQuery(composedModel.value1(),query);
		MapQueryToNewNames(queryToVerify, composedModel.value2());
		
		VerificationOptions options = getVerificationOptionsFromQuery(query);
		modelChecker = getModelChecker(query);
		
		VerificationResult<TimedArcPetriNetTrace> verificationResult = modelChecker.verify(options, composedModel, queryToVerify);
		return verificationResult;
	}

	private TAPNQuery getTAPNQuery(TimedArcPetriNet model, pipe.dataLayer.TAPNQuery query) throws Exception {
		return new TAPNQuery(query.getProperty().copy(), query.getCapacity());
	}

	private TCTLAbstractProperty generateSearchWholeStateSpaceProperty(TimedArcPetriNet model) throws Exception {
		TimedPlace p = model.places().iterator().next();
		if(p == null)
			throw new Exception("Model contained no places. Should not happen.");
		
		return new TCTLAGNode(new TCTLTrueNode());
	}

	private ModelChecker getModelChecker(pipe.dataLayer.TAPNQuery query) {
		if(query.getReductionOption() == ReductionOption.VerifyTAPN)
			return getVerifyTAPN();
		else
			return getVerifyta();
	}

	private VerificationOptions getVerificationOptionsFromQuery(pipe.dataLayer.TAPNQuery query) {
		if(query.getReductionOption() == ReductionOption.VerifyTAPN)
			return new VerifyTAPNOptions(query.getCapacity(), TraceOption.NONE, query.getSearchOption(), query.useSymmetry(), query.discreteInclusion());
		else
			return new VerifytaOptions(TraceOption.NONE, query.getSearchOption(), false, query.getReductionOption(), query.useSymmetry());
	}
	
	private void MapQueryToNewNames(TAPNQuery query, NameMapping mapping) {
		RenameAllPlacesVisitor visitor = new RenameAllPlacesVisitor(mapping);
		query.getProperty().accept(visitor, null);
	}

	private Verifyta getVerifyta() {
		Verifyta verifyta = new Verifyta(new FileFinderImpl(), new MessengerImpl());
		verifyta.setup();
		return verifyta;
	}

	private static VerifyTAPN getVerifyTAPN() {
		VerifyTAPN verifytapn = new VerifyTAPN(new FileFinderImpl(), new MessengerImpl());
		verifytapn.setup();
		return verifytapn;
	}
	
	private LoadedBatchProcessingModel loadModel(File modelFile) {
		fireStatusChanged("Loading model...");
		
		BatchProcessingModelLoader loader = new BatchProcessingModelLoader();
		try {
			return loader.load(modelFile);
		}
		catch(Exception e) {
			publishResult(modelFile.getName(), null, "Error loading model",	0);
			fireVerificationTaskComplete();
			return null;
		}
	}

	@Override
	protected void process(List<BatchProcessingVerificationResult> chunks) {
		for(BatchProcessingVerificationResult result : chunks){
			tableModel.AddResult(result);
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