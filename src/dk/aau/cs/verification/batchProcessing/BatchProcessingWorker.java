package dk.aau.cs.verification.batchProcessing;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingWorker;

import pipe.dataLayer.TAPNQuery.SearchOption;
import pipe.dataLayer.TAPNQuery.TraceOption;
import pipe.gui.FileFinderImpl;
import pipe.gui.MessengerImpl;

import dk.aau.cs.TCTL.visitors.RenameAllPlacesVisitor;
import dk.aau.cs.gui.components.BatchProcessingResultsTableModel;
import dk.aau.cs.io.batchProcessing.BatchProcessingModelLoader;
import dk.aau.cs.io.batchProcessing.LoadedBatchProcessingModel;
import dk.aau.cs.model.tapn.TAPNQuery;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
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


public class BatchProcessingWorker extends SwingWorker<Void, BatchProcessingVerificationResult> {
	private List<File> files;
	private final BatchProcessingResultsTableModel tableModel;
	private BatchProcessingVerificationOptions batchProcessingVerificationOptions;
	private boolean isExiting = false;
	private ModelChecker modelChecker;
	int fileNumber = 0;
	List<BatchProcessingListener> listeners = new ArrayList<BatchProcessingListener>();
	private boolean skippingCurrentVerification = false;
	

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
	
	
	@Override
	protected Void doInBackground() throws Exception {
		for(File file : files){
			if(exiting()) {
				setProgress(100);
				return null;
			}
			
			fireFileChanged(file.getName());
			
			fileNumber++;
			
			LoadedBatchProcessingModel model = null;
			try {
				model = loadModel(file);
			} catch(Exception e) {
				continue;
			}
			
			if(model != null) {
				TAPNComposer composer = new TAPNComposer();
				Tuple<TimedArcPetriNet, NameMapping> composedModel = composer.transformModel(model.network());
				
				for(pipe.dataLayer.TAPNQuery query : model.queries()) {
					fireStatusChanged("Verifying query: " + query.getName() + "...");
					
					VerificationResult<TimedArcPetriNetTrace> verificationResult = null;
					try {
						verificationResult = verify(composedModel, query);
					} catch(UnsupportedModelException e) {
						publishResult(file.getName(), query, "Skipped - model was not supported by reduction", 0);
						continue;
					} catch(UnsupportedQueryException e) {
						publishResult(file.getName(), query, "Skipped - query was not supported by reduction", 0);
						continue;
					}
					
					if(verificationResult != null) {
						if(!verificationResult.error()) {
							String queryResult = verificationResult.getQueryResult().isQuerySatisfied() ? "Satisfied" : "Not Satisfied";
							publishResult(file.getName(), query, queryResult,	verificationResult.verificationTime());
						} else if(skippingCurrentVerification) {
							publishResult(file.getName(), query, "Skipped by user", verificationResult.verificationTime());
							skippingCurrentVerification = false;
						} else {
							publishResult(file.getName(), query, "Error during verification", verificationResult.verificationTime());
						}
					}
				}				
			}
			
			int progress = 100 * fileNumber / files.size();
			setProgress(progress);
		}
		fireFileChanged("");
		fireStatusChanged("Done");
		setProgress(100);
		return null;
	}

	private void publishResult(String fileName, pipe.dataLayer.TAPNQuery query, String verificationResult, long verificationTime) {
		BatchProcessingVerificationResult result = new BatchProcessingVerificationResult(fileName, query, verificationResult,verificationTime);
		publish(result);
	}

	private VerificationResult<TimedArcPetriNetTrace> verify(Tuple<TimedArcPetriNet, NameMapping> composedModel, pipe.dataLayer.TAPNQuery query) throws Exception {
		TAPNQuery clonedQuery = new TAPNQuery(query.getProperty().copy(), query.getCapacity());
		MapQueryToNewNames(clonedQuery, composedModel.value2());
		
		VerificationOptions options = getVerificationOptions(query);
		modelChecker = getModelChecker(query);
		
		VerificationResult<TimedArcPetriNetTrace> verificationResult = modelChecker.verify(options, composedModel, clonedQuery);
		return verificationResult;
	}

	private ModelChecker getModelChecker(pipe.dataLayer.TAPNQuery query) {
		if(query.getReductionOption() == ReductionOption.VerifyTAPN)
			return getVerifyTAPN();
		else
			return getVerifyta();
	}

	private VerificationOptions getVerificationOptions(pipe.dataLayer.TAPNQuery query) {
		if(batchProcessingVerificationOptions != null) {
			SearchOption search = batchProcessingVerificationOptions.searchOption() == SearchOption.BatchProcessingKeepQueryOption ? query.getSearchOption() : batchProcessingVerificationOptions.searchOption();
			ReductionOption option = batchProcessingVerificationOptions.reductionOption() == ReductionOption.BatchProcessingKeepQueryOption ? query.getReductionOption() : batchProcessingVerificationOptions.reductionOption();
			
			if(batchProcessingVerificationOptions.reductionOption() == ReductionOption.VerifyTAPN)
				return new VerifyTAPNOptions(query.getCapacity(), TraceOption.NONE, search);
			else
				return new VerifytaOptions(TraceOption.NONE, search, false, option);
		} else {
			if(query.getReductionOption() == ReductionOption.VerifyTAPN)
				return new VerifyTAPNOptions(query.getCapacity(), TraceOption.NONE, query.getSearchOption());
			else
				return new VerifytaOptions(TraceOption.NONE, query.getSearchOption(), false, query.getReductionOption());
		}
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
}