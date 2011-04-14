package dk.aau.cs.verification.batchProcessing;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingWorker;

import pipe.dataLayer.TAPNQuery.TraceOption;
import pipe.gui.FileFinderImpl;
import pipe.gui.MessengerImpl;

import dk.aau.cs.TCTL.visitors.RenameAllPlacesVisitor;
import dk.aau.cs.gui.components.TableModel;
import dk.aau.cs.io.batchProcessing.BatchProcessingModelLoader;
import dk.aau.cs.io.batchProcessing.LoadedBatchProcessingModel;
import dk.aau.cs.model.tapn.TAPNQuery;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.simulation.TimedArcPetriNetTrace;
import dk.aau.cs.translations.ReductionOption;
import dk.aau.cs.util.Require;
import dk.aau.cs.util.Tuple;
import dk.aau.cs.verification.ModelChecker;
import dk.aau.cs.verification.NameMapping;
import dk.aau.cs.verification.ProcessRunner;
import dk.aau.cs.verification.TAPNComposer;
import dk.aau.cs.verification.VerificationOptions;
import dk.aau.cs.verification.VerificationResult;
import dk.aau.cs.verification.UPPAAL.Verifyta;
import dk.aau.cs.verification.UPPAAL.VerifytaOptions;
import dk.aau.cs.verification.VerifyTAPN.VerifyTAPN;
import dk.aau.cs.verification.VerifyTAPN.VerifyTAPNOptions;


public class BatchProcessingWorker extends SwingWorker<Void, BatchProcessingVerificationResult> {
	private final TableModel tableModel;
	private List<File> files;
	private ProcessRunner currentRunner;
	private boolean isExiting = false;
	int fileNumber = 0;
	List<BatchProcessingListener> listeners = new ArrayList<BatchProcessingListener>();
	

	public BatchProcessingWorker(List<File> files, TableModel tableModel) {
		super();
		this.files = files;
		this.tableModel = tableModel;
		
	}

	public synchronized void notifyExiting(){
		isExiting = true;
	}
	
	private synchronized boolean exiting(){
		return isExiting;
	}
	
	@Override
	protected Void doInBackground() throws Exception {
		for(File file : files){
			if(exiting()) return null;
			
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
					fireStatusChanged("Verifying query " + query.getName() + "...");
					VerificationResult<TimedArcPetriNetTrace> verificationResult = verify(composedModel, query);
					if(verificationResult != null && !verificationResult.error()) {
						BatchProcessingVerificationResult result = new BatchProcessingVerificationResult(file.getName(), query, verificationResult);
						publish(result);
					}
				}				
			}
			
			int progress = 100 * fileNumber / files.size();
			setProgress(progress);
		}
		fireFileChanged("");
		fireStatusChanged("done");
		return null;
	}

	private VerificationResult<TimedArcPetriNetTrace> verify(Tuple<TimedArcPetriNet, NameMapping> composedModel, pipe.dataLayer.TAPNQuery query) {
		VerificationResult<TimedArcPetriNetTrace> verificationResult = null;
		
		TAPNQuery clonedQuery = new TAPNQuery(query.getProperty().copy(), query.getCapacity());
		MapQueryToNewNames(clonedQuery, composedModel.value2());
		
		if(query.getReductionOption() == ReductionOption.VerifyTAPN) {
			VerificationOptions options = new VerifyTAPNOptions(query.getCapacity(), TraceOption.NONE, query.getSearchOption());
			verificationResult = runVerifyTAPNVerification(options, composedModel, clonedQuery);
		} else { 
			VerificationOptions options = new VerifytaOptions(TraceOption.NONE, query.getSearchOption(), false, query.getReductionOption());
			verificationResult = runUPPAALVerification(options, composedModel, clonedQuery);
		}
		return verificationResult;
	}
	
	private void MapQueryToNewNames(TAPNQuery query, NameMapping mapping) {
		RenameAllPlacesVisitor visitor = new RenameAllPlacesVisitor(mapping);
		query.getProperty().accept(visitor, null);
	}
	
	private VerificationResult<TimedArcPetriNetTrace> runUPPAALVerification(VerificationOptions options, Tuple<TimedArcPetriNet, NameMapping> composedModel, TAPNQuery query) {
		ModelChecker verifyta = getVerifyta();
		return verifyta.verify(options, composedModel, query);
	}

	private Verifyta getVerifyta() {
		Verifyta verifyta = new Verifyta(new FileFinderImpl(), new MessengerImpl());
		verifyta.setup();
		return verifyta;
	}

	private VerificationResult<TimedArcPetriNetTrace> runVerifyTAPNVerification(VerificationOptions options, Tuple<TimedArcPetriNet, NameMapping> composedModel, TAPNQuery query) {
		ModelChecker verifyTAPN = getVerifyTAPN();
		return verifyTAPN.verify(options, composedModel, query);
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
			// TODO: warn user that models/queries which cannot be loaded are skipped?
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
			currentRunner.kill();
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