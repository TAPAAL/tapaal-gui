package pipe.gui;

import java.awt.Dialog;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileSystemView;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.PNMLWriter;
import pipe.dataLayer.Template;
import pipe.dataLayer.TAPNQuery.SearchOption;
import dk.aau.cs.Messenger;
import dk.aau.cs.TCTL.visitors.RenameAllPlacesVisitor;
import dk.aau.cs.approximation.OverApproximation;
import dk.aau.cs.approximation.UnderApproximation;
import dk.aau.cs.io.TimedArcPetriNetNetworkWriter;
import dk.aau.cs.model.tapn.Constant;
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
import dk.aau.cs.verification.QueryResult;
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
		if (this.guiModels != null) {
			composer = new TAPNComposerExtended(messenger, guiModels);
		} else {
			composer = new TAPNComposer(messenger);			
		}
		
		Tuple<TimedArcPetriNet, NameMapping> transformedModel = composer.transformModel(model);
		
		// If selected, save the composed net before any approximations are being made
		if (dataLayerQuery.shouldSaveComposedNet()) {
			ArrayList<Template> templates = new ArrayList<Template>(1);
			templates.add(new Template(transformedModel.value1(), ((TAPNComposerExtended) composer).getGuiModel(), new Zoomer()));
			
			TimedArcPetriNetNetwork network = new TimedArcPetriNetNetwork();
			network.add(transformedModel.value1());
			
			PNMLWriter tapnWriter = new TimedArcPetriNetNetworkWriter(network, templates, new ArrayList<pipe.dataLayer.TAPNQuery>(0), new ArrayList<Constant>(0));
	
			try {
				FileFinder fileFinder = new FileFinderImpl();
				File choosenFile = fileFinder.ShowFileBrowserDialog("Choose where to save composed net", ".xml", null);
				if (choosenFile != null) {
					tapnWriter.savePNML(choosenFile);					
				} else {
					JOptionPane.showMessageDialog(null, "The composed net was not saved");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
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
		
		// If selected, save the composed net after approximations have been made
		if (false) {
			ArrayList<Template> templates = new ArrayList<Template>(1);
			templates.add(new Template(transformedModel.value1(), ((TAPNComposerExtended) composer).getGuiModel(), new Zoomer()));
			
			TimedArcPetriNetNetwork network = new TimedArcPetriNetNetwork();
			network.add(transformedModel.value1());
			
			PNMLWriter tapnWriter = new TimedArcPetriNetNetworkWriter(network, templates, new ArrayList<pipe.dataLayer.TAPNQuery>(0), new ArrayList<Constant>(0));
	
			try {
				FileFinder fileFinder = new FileFinderImpl();
				File choosenFile = fileFinder.ShowFileBrowserDialog("Choose where to save approximated net", ".xml", null);
				if (choosenFile != null) {
					tapnWriter.savePNML(choosenFile);					
				} else {
					JOptionPane.showMessageDialog(null, "The approximated net was not saved");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
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

		else if (dataLayerQuery != null && dataLayerQuery.isOverApproximationEnabled() && ((result.getQueryResult().queryType() == QueryType.EF && result.getQueryResult().isQuerySatisfied()) || (result.getQueryResult().queryType() == QueryType.AG && !result.getQueryResult().isQuerySatisfied()))) {
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
			

			//Create trace TAPN from the trace
			Tuple<TimedArcPetriNet, NameMapping> transformedOriginalModel = composer.transformModel(model);
			overaprx.makeTraceTAPN(transformedOriginalModel, value);

			//run model checker again for trace TAPN
			result = modelChecker.verify(options, transformedOriginalModel, clonedQuery);
			if (isCancelled()) {
				firePropertyChange("state", StateValue.PENDING, StateValue.DONE);
			}
			if (result.error()) {
				return new VerificationResult<TAPNNetworkTrace>(result.errorMessage(), result.verificationTime());
			}
			//Create the result from trace TAPN
			removeTraceTransitions(result.getTrace());
			removeTraceTransitions(result.getSecondaryTrace());
			QueryResult queryResult= result.getQueryResult();
			if ((queryResult.queryType() == QueryType.EF && !queryResult.isQuerySatisfied()) || (queryResult.queryType() == QueryType.AG && queryResult.isQuerySatisfied()))
				queryResult.setApproximationInconclusive(true);
			value = new VerificationResult<TAPNNetworkTrace>(
					queryResult,
					decomposeTrace(result.getTrace(), transformedModel.value2()),
					decomposeTrace(result.getSecondaryTrace(), transformedModel.value2()),
					approxResult.verificationTime(),
					approxResult.stats());
			value.setNameMapping(transformedModel.value2());
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
		
		return value;
	}
	
	private void removeTraceTransitions(TimedArcPetriNetTrace trace) {
		if (trace != null)
			trace.removeTransitionsByNameMatch("TTRACE");
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
