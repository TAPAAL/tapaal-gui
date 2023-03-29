package dk.aau.cs.verification.batchProcessing;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.SwingWorker;

import dk.aau.cs.verification.VerifyTAPN.*;
import net.tapaal.gui.petrinet.verification.TAPNQuery.SearchOption;
import net.tapaal.gui.petrinet.verification.TAPNQuery.TraceOption;
import net.tapaal.gui.petrinet.verification.TAPNQuery.WorkflowMode;
import net.tapaal.gui.petrinet.verification.TAPNQuery.QueryReductionTime;
import pipe.gui.TAPAALGUI;
import pipe.gui.FileFinder;
import pipe.gui.MessengerImpl;
import net.tapaal.gui.petrinet.verification.Verifier;
import net.tapaal.gui.petrinet.widgets.QueryPane;
import dk.aau.cs.Messenger;
import dk.aau.cs.TCTL.TCTLAGNode;
import dk.aau.cs.TCTL.TCTLEFNode;
import dk.aau.cs.TCTL.TCTLEGNode;
import dk.aau.cs.TCTL.TCTLAbstractProperty;
import dk.aau.cs.TCTL.TCTLTrueNode;
import dk.aau.cs.TCTL.TCTLDeadlockNode;
import dk.aau.cs.TCTL.visitors.RenameAllPlacesVisitor;
import dk.aau.cs.TCTL.visitors.RenameAllTransitionsVisitor;
import dk.aau.cs.approximation.ApproximationWorker;
import net.tapaal.gui.petrinet.dialog.BatchProcessingResultsTableModel;
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
import net.tapaal.gui.petrinet.verification.TAPNQuery.ExtrapolationOption;
import net.tapaal.gui.petrinet.verification.TAPNQuery.QueryCategory;

public class BatchProcessingWorker extends SwingWorker<Void, BatchProcessingVerificationResult> {
	private final List<File> files;
	private final BatchProcessingResultsTableModel tableModel;
	private final List<BatchProcessingVerificationOptions> options;
	private boolean isExiting = false;
	private ModelChecker modelChecker;
	final List<BatchProcessingListener> listeners = new ArrayList<>();
	private boolean skippingCurrentVerification = false;
	private boolean timeoutCurrentVerification = false;
	private boolean oomCurrentVerification = false;
	private int verificationTasksCompleted;
	private LoadedBatchProcessingModel model;
	private boolean isSoundnessCheck;
	private boolean isModelCheckOnly;
	private ArrayList<File> filesProcessed;

    public BatchProcessingWorker(List<File> files, BatchProcessingResultsTableModel tableModel, List<BatchProcessingVerificationOptions> options) {
        super();
        this.files = files;
        this.tableModel = tableModel;
        this.options = options;
    }

	public synchronized void notifyExiting() {
		isExiting = true;
	}
	
	private synchronized boolean exiting() {
		return isExiting;
	}
	
	public synchronized void notifySkipCurrentVerification() {
		skippingCurrentVerification = true;
		if (modelChecker != null) {
			modelChecker.kill();
		}
	}
	
	public synchronized void notifyTimeoutCurrentVerificationTask() {
		timeoutCurrentVerification = true;
		if (modelChecker != null) {
			modelChecker.kill();
		}
	}
	
	public synchronized void notifyOOMCurrentVerificationTask() {
		oomCurrentVerification = true;
		if (modelChecker != null) {
			modelChecker.kill();
		}
	}

    @Override
    protected Void doInBackground() throws Exception {
        isSoundnessCheck = false;
        filesProcessed = new ArrayList<>();
        for (File file : files) {
            fireFileChanged(file.getName());
            LoadedBatchProcessingModel model = loadModel(file);
            this.model = model;
            if (model != null) {
                Tuple<TimedArcPetriNet, NameMapping> composedModel = composeModel(model);
                for (net.tapaal.gui.petrinet.verification.TAPNQuery query : model.queries()) {
                    if (exiting()) {
                        return null;
                    }
                    /* For "Search whole state space", "Existence of deadlock", "Soundness" and "Strong Soundness"
                     * the file should only be checked once instead of checking every query
                     * TODO: LENA - måske slet?
                     */
                    if (isModelCheckOnly && filesProcessed.contains(file)) {
                        continue;
                    }

                    processQuery(file, composedModel, query);
                }
            }
        }
        fireFileChanged("");
        fireStatusChanged("");
        return null;
    }

	private void processQuery(File file, Tuple<TimedArcPetriNet, NameMapping> composedModel,
                              net.tapaal.gui.petrinet.verification.TAPNQuery queryToVerify) throws Exception {
        if (!queryToVerify.isActive()) {
            publishResult(file.getName(), queryToVerify, "Skipped - query is disabled because it contains propositions involving places from a deactivated component", 0, new NullStats());
        }
        for (BatchProcessingVerificationOptions option : options) {
            processQuery(file, composedModel, queryToVerify, option);
        }

		fireVerificationTaskComplete();
	}

	private void processQuery(File file, Tuple<TimedArcPetriNet, NameMapping> composedModel,
                              net.tapaal.gui.petrinet.verification.TAPNQuery queryToVerify,
                              BatchProcessingVerificationOptions option) throws Exception {
        VerificationResult<TimedArcPetriNetTrace> verificationResult = verifyQuery(file, composedModel, queryToVerify, option);
        if (verificationResult != null) {
            processVerificationResult(file, queryToVerify, verificationResult);
        }
    }

	/*private net.tapaal.gui.petrinet.verification.TAPNQuery createQueryFromQueryPropertyOption(TimedArcPetriNet model, QueryPropertyOption option, File fileToBeChecked) throws Exception {
		int capacity = batchProcessingVerificationOptions.capacity();
		ReductionOption reductionOption = model.isUntimed() ? ReductionOption.VerifyPN : ReductionOption.VerifyTAPN;
		if(option == QueryPropertyOption.ExistDeadlock) {
			filesProcessed.add(fileToBeChecked);
			return new net.tapaal.gui.petrinet.verification.TAPNQuery(
					"Existence of a deadlock", capacity,
							generateExistDeadlock(model), TraceOption.NONE,
							SearchOption.DEFAULT,
							reductionOption, true, true,
							false, true, false, null, ExtrapolationOption.AUTOMATIC,
							WorkflowMode.WORKFLOW_SOUNDNESS, model.isColored());
		}
		if(option == QueryPropertyOption.SearchWholeStateSpace) {
			filesProcessed.add(fileToBeChecked);
			return new net.tapaal.gui.petrinet.verification.TAPNQuery(
					"Search whole state space", capacity,
							generateSearchWholeStateSpaceProperty(model), TraceOption.NONE,
							SearchOption.DEFAULT,
							reductionOption, true, true,
							false, true, false, null, ExtrapolationOption.AUTOMATIC,
							WorkflowMode.WORKFLOW_SOUNDNESS, model.isColored());
		}
		if (option == QueryPropertyOption.Soundness) {
			isSoundnessCheck = true;
			filesProcessed.add(fileToBeChecked);
			return new net.tapaal.gui.petrinet.verification.TAPNQuery(
				"Workflow soundness check", capacity,
						new TCTLEFNode(new TCTLTrueNode()), TraceOption.SOME,
						SearchOption.DEFAULT,
						ReductionOption.VerifyDTAPN, true, true,
						false, true, false, null, ExtrapolationOption.AUTOMATIC,
						WorkflowMode.WORKFLOW_SOUNDNESS, model.isColored());
		}
		if(option == QueryPropertyOption.StrongSoundness) {
			isSoundnessCheck = true;
			filesProcessed.add(fileToBeChecked);
        	return new net.tapaal.gui.petrinet.verification.TAPNQuery(
					"Workflow soundness check", capacity,
							new TCTLEGNode(new TCTLTrueNode()), TraceOption.SOME,
							SearchOption.DEFAULT,
							ReductionOption.VerifyDTAPN, true, true,
							false, true, false, null, ExtrapolationOption.AUTOMATIC,
							WorkflowMode.WORKFLOW_STRONG_SOUNDNESS, model.isColored());
		}
		return null;
	}*/

	/*private boolean getSymmetryFromBatchProcessingOptions() {
		return batchProcessingVerificationOptions.symmetry() == SymmetryOption.Yes;
	}
        
	private boolean getStubbornReductionFromBatchProcessingOptions(){
		return batchProcessingVerificationOptions.stubbornReductionOption() == StubbornReductionOption.Yes;
	}*/

	private Tuple<TimedArcPetriNet, NameMapping> composeModel(LoadedBatchProcessingModel model) {
		ITAPNComposer composer = new TAPNComposer(new Messenger(){
			public void displayInfoMessage(String message) { }
			public void displayInfoMessage(String message, String title) {}
			public void displayErrorMessage(String message) {}
			public void displayErrorMessage(String message, String title) {}
			public void displayWrappedErrorMessage(String message, String title) {}
			
		}, false);
        return composer.transformModel(model.network());
	}

	private VerificationResult<TimedArcPetriNetTrace> verifyQuery(File file, Tuple<TimedArcPetriNet, NameMapping> composedModel, net.tapaal.gui.petrinet.verification.TAPNQuery query, BatchProcessingVerificationOptions option) throws Exception {
		fireStatusChanged(query.getName());

		VerificationResult<TimedArcPetriNetTrace> verificationResult;
		try {
			verificationResult = verify(composedModel, query, option);
		} catch (UnsupportedModelException e) {
            publishResult(file.getName(), query, "Skipped - model not supported by the verification method", 0, new NullStats());
			return null;
		} catch(UnsupportedQueryException e) {
			if (e.getMessage().toLowerCase().contains("discrete inclusion")) {
                publishResult(file.getName(), query, "Skipped - discrete inclusion is enabled and query is not upward closed", 0, new NullStats());
            } else {
                publishResult(file.getName(), query, "Skipped - query not supported by the verification method", 0, new NullStats());
            }
			return null;
		} 
		return verificationResult;
	}

	private void processVerificationResult(File file, net.tapaal.gui.petrinet.verification.TAPNQuery query, VerificationResult<TimedArcPetriNetTrace> verificationResult) {
		if (skippingCurrentVerification) {
			publishResult(file.getName(), query, "Skipped - by the user", verificationResult.verificationTime(), new NullStats());
			skippingCurrentVerification = false;
		} else if (timeoutCurrentVerification) {
			publishResult(file.getName(), query, "Skipped - due to timeout", verificationResult.verificationTime(), new NullStats());
			timeoutCurrentVerification = false;
		} else if (oomCurrentVerification) {
			publishResult(file.getName(), query, "Skipped - due to OOM", verificationResult.verificationTime(), new NullStats());
			oomCurrentVerification = false;
		} else if (!verificationResult.error()) {
			String queryResult = "";
			if (verificationResult.getQueryResult().isApproximationInconclusive())
			{
				queryResult = "Inconclusive";
			}
			else
			{
				queryResult = verificationResult.getQueryResult().isQuerySatisfied() ? "Satisfied" : "Not Satisfied";
				if (isSoundnessCheck && !verificationResult.isQuerySatisfied())
					queryResult = "Not Sound";
				if (isSoundnessCheck && verificationResult.isQuerySatisfied()) {
					if (query.getWorkflowMode() == WorkflowMode.WORKFLOW_STRONG_SOUNDNESS)
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
				if (query.getReductionOption().equals(ReductionOption.VerifyPNApprox) &&
						((query.queryType().equals(QueryType.EF) && verificationResult.getQueryResult().isQuerySatisfied()) ||
						(query.queryType().equals(QueryType.AG) && !verificationResult.getQueryResult().isQuerySatisfied()))) {
					queryResult = "Inconclusive";
				}
			publishResult(file.getName(), query, queryResult,	verificationResult.verificationTime(), verificationResult.stats());
		} else if (isSoundnessCheck && verificationResult.error()) {
			publishResult(file.getName(), query, "Skipped - model is not a workflow net. Try running workflow analysis from the menu.", verificationResult.verificationTime(), new NullStats());
		} else {
			publishResult(file.getName(), query, "Error during verification", verificationResult.verificationTime(), new NullStats());
		}		
	}

	private void publishResult(String fileName, net.tapaal.gui.petrinet.verification.TAPNQuery query, String verificationResult, long verificationTime, Stats stats) {
		BatchProcessingVerificationResult result;		
		if (QueryPane.getTemporaryFile() != null && fileName.equals(QueryPane.getTemporaryFile().getName())) {
			//removes numbers from tempFile so it looks good
			result = new BatchProcessingVerificationResult(TAPAALGUI.getAppGui().getCurrentTabName(), query, verificationResult, verificationTime, MemoryMonitor.getPeakMemory(), stats);
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

    private VerificationResult<TimedArcPetriNetTrace> verify(Tuple<TimedArcPetriNet, NameMapping> composedModel, net.tapaal.gui.petrinet.verification.TAPNQuery query, BatchProcessingVerificationOptions option) throws Exception {
        TAPNQuery queryToVerify = getTAPNQuery(composedModel.value1(), query);
        queryToVerify.setCategory(query.getCategory());
        MapQueryToNewNames(queryToVerify, composedModel.value2());

        TAPNQuery clonedQuery = new TAPNQuery(query.getProperty().copy(), queryToVerify.getExtraTokens());
        clonedQuery.setCategory(query.getCategory());
        MapQueryToNewNames(clonedQuery, composedModel.value2());

        fireVerificationTaskStarted();
        ApproximationWorker worker = new ApproximationWorker();

        if (option.getOptions().equals("Default")) {
            modelChecker = getModelChecker(query);
            return worker.batchWorker(composedModel, getVerificationOptionsFromQuery(query), query, model, modelChecker, queryToVerify, clonedQuery, this);
        } else {
            String options = option.getOptions();
            if (option.keepKBound()) {
                Pattern pattern = Pattern.compile("\\s*(-k|--k-bound)\\s*(\\d+)\\s*", Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(options);
                if (matcher.find()) {
                    options = options.replace(matcher.group(), matcher.group(1) + " " + query.getCapacity() + " ");
                }
            }
            if (option.getEngine() == null) {
                modelChecker = getModelChecker(query);
            } else {
                modelChecker = getModelChecker(option.getEngine());
            }
            return worker.batchWorker(composedModel, options, query, model, modelChecker, queryToVerify, clonedQuery, this);
        }
    }
	
	/*private VerificationResult<TimedArcPetriNetTrace> verify(Tuple<TimedArcPetriNet, NameMapping> composedModel, net.tapaal.gui.petrinet.verification.TAPNQuery query) throws Exception {
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
	}*/

	private TAPNQuery getTAPNQuery(TimedArcPetriNet model, net.tapaal.gui.petrinet.verification.TAPNQuery query) throws Exception {
		return new TAPNQuery(query.getProperty().copy(), query.getCapacity());
	}

	private TCTLAbstractProperty generateSearchWholeStateSpaceProperty(TimedArcPetriNet model) throws Exception {
		TimedPlace p = model.places().iterator().next();
		if (p == null)
			throw new Exception("Model contains no places. This may not happen.");
		
		return new TCTLAGNode(new TCTLTrueNode());
	}
        
	private TCTLAbstractProperty generateExistDeadlock(TimedArcPetriNet model) throws Exception {
		return new TCTLEFNode(new TCTLDeadlockNode()); 
	}
	
	private ModelChecker getModelChecker(net.tapaal.gui.petrinet.verification.TAPNQuery query) {
		if (query.getReductionOption() == ReductionOption.VerifyTAPN)
			return getVerifyTAPN();
		else if (query.getReductionOption() == ReductionOption.VerifyDTAPN)
			return getVerifyTAPNDiscreteVerification();
		else if (query.getReductionOption() == ReductionOption.VerifyPN || query.getReductionOption() == ReductionOption.VerifyPNApprox || query.getReductionOption() == ReductionOption.VerifyPNReduce)
			return getVerifyPN();
		else
			return getVerifyta();
	}

    private ModelChecker getModelChecker(ReductionOption reductionOption) {
        if (reductionOption == ReductionOption.VerifyTAPN)
            return getVerifyTAPN();
        else if (reductionOption == ReductionOption.VerifyDTAPN)
            return getVerifyTAPNDiscreteVerification();
        else if (reductionOption == ReductionOption.VerifyPN || reductionOption == ReductionOption.VerifyPNApprox || reductionOption == ReductionOption.VerifyPNReduce)
            return getVerifyPN();
        else
            return getVerifyta();
    }

	public VerificationOptions getVerificationOptionsFromQuery(net.tapaal.gui.petrinet.verification.TAPNQuery query) {
        if (query.getReductionOption() == ReductionOption.VerifyTAPN) {
            return new VerifyTAPNOptions(query.getCapacity(), TraceOption.NONE, query.getSearchOption(), query.useSymmetry(), false, query.discreteInclusion(), query.inclusionPlaces(), query.isOverApproximationEnabled(), query.isUnderApproximationEnabled(), query.approximationDenominator());    // XXX DISABLES OverApprox
        } else if (query.getReductionOption() == ReductionOption.VerifyDTAPN) {
            return new VerifyDTAPNOptions(query.getCapacity(), TraceOption.NONE, query.getSearchOption(), query.useSymmetry(), query.useGCD(), query.useTimeDarts(), query.usePTrie(), false, query.discreteInclusion(), query.inclusionPlaces(), query.getWorkflowMode(), 0, query.isOverApproximationEnabled(), query.isUnderApproximationEnabled(), query.approximationDenominator(), query.isStubbornReductionEnabled(), null, query.usePartitioning(), query.useColorFixpoint(), query.isColored());
        } else if (query.getReductionOption() == ReductionOption.VerifyPN || query.getReductionOption() == ReductionOption.VerifyPNApprox || query.getReductionOption() == ReductionOption.VerifyPNReduce) {
            return new VerifyPNOptions(query.getCapacity(), TraceOption.NONE, query.getSearchOption(), query.useOverApproximation(), query.useReduction() ? ModelReduction.AGGRESSIVE : ModelReduction.NO_REDUCTION, query.isOverApproximationEnabled(), query.isUnderApproximationEnabled(), query.approximationDenominator(), query.getCategory(), query.getAlgorithmOption(), query.isSiphontrapEnabled(), query.isQueryReductionEnabled() ? QueryReductionTime.UnlimitedTime : QueryReductionTime.NoTime, query.isStubbornReductionEnabled(), null, query.isTarOptionEnabled(), query.isTarjan(), query.isColored(), query.usePartitioning(), query.useColorFixpoint(), query.useSymmetricVars());
        } else {
            return new VerifytaOptions(TraceOption.NONE, query.getSearchOption(), false, query.getReductionOption(), query.useSymmetry(), false, query.isOverApproximationEnabled(), query.isUnderApproximationEnabled(), query.approximationDenominator());
        }
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
	
	private static VerifyDTAPN getVerifyTAPNDiscreteVerification() {
		VerifyDTAPN verifytapnDiscreteVerification = new VerifyDTAPN(new FileFinder(), new MessengerImpl());
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
		for (BatchProcessingVerificationResult result : chunks) {
			tableModel.addResult(result);
		}
	}
	
	@Override
	protected void done() {
		if (isCancelled()) {
			if (modelChecker != null)
				modelChecker.kill();
		}
	}
	
	public void addBatchProcessingListener(BatchProcessingListener listener) {
		Require.that(listener != null, "Listener cannot be null");
		listeners.add(listener);
	}

	public void removeBatchProcessingListener(BatchProcessingListener listener) {
		Require.that(listener != null, "Listener cannot be null");
		listeners.remove(listener);
	}
	
	private void fireStatusChanged(String status) {
		for (BatchProcessingListener listener : listeners)
			listener.fireStatusChanged(new StatusChangedEvent(status));
	}
	
	private void fireFileChanged(String fileName) {
		for (BatchProcessingListener listener : listeners)
			listener.fireFileChanged(new FileChangedEvent(fileName));
	}
	
	private void fireVerificationTaskComplete() {
		verificationTasksCompleted++;
		for (BatchProcessingListener listener : listeners)
			listener.fireVerificationTaskComplete(new VerificationTaskCompleteEvent(verificationTasksCompleted));
	}
	
	private void fireVerificationTaskStarted() {
		for (BatchProcessingListener listener : listeners)
			listener.fireVerificationTaskStarted();
	}
}
