package net.tapaal.gui.petrinet.verification;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import javax.swing.*;

import net.tapaal.gui.petrinet.TAPNLens;
import dk.aau.cs.verification.*;
import pipe.gui.petrinet.dataLayer.DataLayer;
import dk.aau.cs.verification.VerifyTAPN.VerifyTAPNOptions;
import net.tapaal.gui.petrinet.verification.TAPNQuery.SearchOption;
import net.tapaal.gui.petrinet.verification.TAPNQuery.QueryReductionTime;
import dk.aau.cs.Messenger;
import dk.aau.cs.TCTL.visitors.RenameAllPlacesVisitor;
import dk.aau.cs.TCTL.visitors.RenameAllTransitionsVisitor;
import dk.aau.cs.approximation.ApproximationWorker;
import dk.aau.cs.approximation.OverApproximation;
import dk.aau.cs.approximation.UnderApproximation;
import dk.aau.cs.model.tapn.TAPNQuery;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.model.tapn.simulation.TAPNNetworkTrace;
import dk.aau.cs.model.tapn.simulation.TimedArcPetriNetTrace;
import dk.aau.cs.util.Tuple;
import dk.aau.cs.util.UnsupportedModelException;
import dk.aau.cs.verification.VerifyTAPN.ModelReduction;
import dk.aau.cs.verification.VerifyTAPN.VerifyPN;
import dk.aau.cs.verification.VerifyTAPN.VerifyPNOptions;
import pipe.gui.TAPAALGUI;
import pipe.gui.FileFinder;
import pipe.gui.MessengerImpl;

public abstract class RunVerificationBase extends SwingWorker<VerificationResult<TAPNNetworkTrace>, Void> {

	protected final ModelChecker modelChecker;

	protected VerificationOptions options;
	protected TimedArcPetriNetNetwork model;
	protected DataLayer guiModel;
	protected TAPNQuery query;
	protected TAPNQuery clonedQuery;
	protected net.tapaal.gui.petrinet.verification.TAPNQuery dataLayerQuery;
    protected HashMap<TimedArcPetriNet, DataLayer> guiModels;
	protected final String reducedNetFilePath;
	protected final boolean reduceNetOnly;
	protected boolean reducedNetOpened = false;
	protected final JSpinner spinner;
	protected final Messenger messenger;
    TAPNLens lens;

    public RunVerificationBase(ModelChecker modelChecker, Messenger messenger, HashMap<TimedArcPetriNet, DataLayer> guiModels, String reducedNetFilePath, boolean reduceNetOnly, JSpinner spinner) {
		super();
		this.modelChecker = modelChecker;
		this.messenger = messenger;
		this.guiModels = guiModels;
		this.reducedNetFilePath = reducedNetFilePath;
		this.reduceNetOnly = reduceNetOnly;
        this.spinner = spinner;
    }

    public void execute(VerificationOptions options, TimedArcPetriNetNetwork model, TAPNQuery query, net.tapaal.gui.petrinet.verification.TAPNQuery dataLayerQuery, TAPNLens lens) {
        this.model = model;
        this.options = options;
        this.query = query;
        this.dataLayerQuery = dataLayerQuery;
        this.lens = lens;
        execute();
    }
    public void execute(VerificationOptions options, TimedArcPetriNetNetwork model, TAPNQuery query, net.tapaal.gui.petrinet.verification.TAPNQuery dataLayerQuery) {
        execute(options, model, query, dataLayerQuery, null);
    }

	@Override
	protected VerificationResult<TAPNNetworkTrace> doInBackground() throws Exception {
        ITAPNComposer composer = new TAPNComposer(messenger, guiModels, lens, false, true);
        Tuple<TimedArcPetriNet, NameMapping> transformedModel = composer.transformModel(model);
        guiModel = composer.getGuiModel();
        if (options.enabledOverApproximation()) {
            OverApproximation overaprx = new OverApproximation();
            overaprx.modifyTAPN(transformedModel.value1(), options.approximationDenominator());
        } else if (options.enabledUnderApproximation()) {
            UnderApproximation underaprx = new UnderApproximation();
            underaprx.modifyTAPN(transformedModel.value1(), options.approximationDenominator());
        }

        clonedQuery = new TAPNQuery(query.getProperty().copy(), query.getExtraTokens(), query.getSMCSettings());
        MapQueryToNewNames(clonedQuery, transformedModel.value2());

        if (dataLayerQuery != null) {
            clonedQuery.setCategory(dataLayerQuery.getCategory()); // Used by the CTL engine
            clonedQuery.setVerificationType(dataLayerQuery.getVerificationType());
            clonedQuery.setTraceList(dataLayerQuery.getTraceList());
            clonedQuery.setSMCSettings(dataLayerQuery.getSmcSettings());
        }
        
        if (options.enabledStateequationsCheck()) {
                if ((query.queryType() == QueryType.EF || query.queryType() == QueryType.AG) && !query.hasDeadlock() &&
                !(options instanceof VerifyPNOptions)) {

                VerifyPN verifypn = new VerifyPN(new FileFinder(), new MessengerImpl());

                verifypn.supportsModel(transformedModel.value1(), options);// Skip over-approximation if model is not supported.
                // Prevents verification from displaying error.

                if (!verifypn.setup()) {
                    messenger.displayInfoMessage("Over-approximation check is skipped because VerifyPN is not available.", "VerifyPN unavailable");
                } else {
                    VerificationResult<TimedArcPetriNetTrace> skeletonAnalysisResult = null;
                    if (dataLayerQuery != null) {
                        skeletonAnalysisResult = verifypn.verify(
                            new VerifyPNOptions(
                                options.extraTokens(),
                                net.tapaal.gui.petrinet.verification.TAPNQuery.TraceOption.NONE,
                                SearchOption.OVERAPPROXIMATE,
                                true,
                                ModelReduction.AGGRESSIVE,
                                options.enabledOverApproximation(),
                                options.enabledUnderApproximation(),
                                options.approximationDenominator(),
                                dataLayerQuery.getCategory(),
                                dataLayerQuery.getAlgorithmOption(),
                                dataLayerQuery.isSiphontrapEnabled(),
                                dataLayerQuery.isQueryReductionEnabled() ? QueryReductionTime.UnlimitedTime : QueryReductionTime.NoTime,
                                dataLayerQuery.isStubbornReductionEnabled(),
                                reducedNetFilePath,
                                dataLayerQuery.isTarOptionEnabled(),
                                dataLayerQuery.isTarjan(),
                                model.isColored(),
                                model.isColored() && (!model.isUntimed() || options.traceOption() != net.tapaal.gui.petrinet.verification.TAPNQuery.TraceOption.NONE),
                                dataLayerQuery.usePartitioning(),
                                dataLayerQuery.useColorFixpoint(),
                                dataLayerQuery.useSymmetricVars(),
                                dataLayerQuery.useExplicitSearch()
                            ),
                            transformedModel,
                            clonedQuery,
                            composer.getGuiModel(),
                            dataLayerQuery,
                            null);
                    } else { // TODO: FIX! If datalayer is null then we can't check datalayer's values...
                        skeletonAnalysisResult = verifypn.verify(
                            new VerifyPNOptions(
                                options.extraTokens(),
                                net.tapaal.gui.petrinet.verification.TAPNQuery.TraceOption.NONE,
                                SearchOption.OVERAPPROXIMATE,
                                true,
                                ModelReduction.AGGRESSIVE,
                                options.enabledOverApproximation(),
                                options.enabledUnderApproximation(),
                                options.approximationDenominator(),
                                net.tapaal.gui.petrinet.verification.TAPNQuery.QueryCategory.Default,
                                net.tapaal.gui.petrinet.verification.TAPNQuery.AlgorithmOption.CERTAIN_ZERO,
                                false,
                                QueryReductionTime.UnlimitedTime,
                                false,
                                reducedNetFilePath,
                                false,
                                true,
                                model.isColored(),
                                model.isColored() && (!model.isUntimed() || options.traceOption() != net.tapaal.gui.petrinet.verification.TAPNQuery.TraceOption.NONE),
                                true,
                                true,
                                options.useExplicitSearch()
                            ),
                            transformedModel,
                            clonedQuery,
                            composer.getGuiModel(),
                            dataLayerQuery,
                            null);
                    }

                    if (skeletonAnalysisResult.getQueryResult() != null) {
                        if (!skeletonAnalysisResult.error() &&
                            (
                                (model.isUntimed() && (options.traceOption() == net.tapaal.gui.petrinet.verification.TAPNQuery.TraceOption.NONE)) ||
                                ((query.queryType() == QueryType.EF && !skeletonAnalysisResult.getQueryResult().isQuerySatisfied()) || (query.queryType() == QueryType.AG && skeletonAnalysisResult.getQueryResult().isQuerySatisfied())
                            )
                        )
                        ) {
                            VerificationResult<TAPNNetworkTrace> value = new VerificationResult<TAPNNetworkTrace>(
                                skeletonAnalysisResult.getQueryResult(),
                                decomposeTrace(skeletonAnalysisResult.getTrace(), transformedModel.value2()),
                                skeletonAnalysisResult.verificationTime(),
                                skeletonAnalysisResult.stats(),
                                skeletonAnalysisResult.getRawOutput()
                            );
                            value.setResolvedUsingSkeletonAnalysisPreprocessor(true);
                            value.setNameMapping(transformedModel.value2());
                            return value;
                        }
                    }
                }
            }
        }
        
        ApproximationWorker worker = new ApproximationWorker();
        return worker.normalWorker(options, modelChecker, transformedModel, composer, clonedQuery, this, model, guiModel, dataLayerQuery, lens);
    }

    private TAPNNetworkTrace decomposeTrace(TimedArcPetriNetTrace trace, NameMapping mapping) {
		if (trace == null) {
			return null;
		}

		TAPNTraceDecomposer decomposer = new TAPNTraceDecomposer(trace, model, mapping);
		return decomposer.decompose();
	}

	private void MapQueryToNewNames(TAPNQuery query, NameMapping mapping) {
		RenameAllPlacesVisitor placeVisitor = new RenameAllPlacesVisitor(mapping);
		RenameAllTransitionsVisitor transitionVisitor = new RenameAllTransitionsVisitor(mapping);
		query.getProperty().accept(placeVisitor, null);
		query.getProperty().accept(transitionVisitor, null);
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
         
			if (result == null) return;
			if (showResult(result) && spinner != null) {
                QueryReductionTime reductionTime = QueryReductionTime.UnlimitedTime;
			    if (dataLayerQuery != null) {
                    reductionTime = dataLayerQuery.isQueryReductionEnabled() ? QueryReductionTime.UnlimitedTime : QueryReductionTime.NoTime;
                }
			    options = new VerifyPNOptions(options.extraTokens(), net.tapaal.gui.petrinet.verification.TAPNQuery.TraceOption.NONE, SearchOption.BFS, false, ModelReduction.BOUNDPRESERVING, false, false, 1, net.tapaal.gui.petrinet.verification.TAPNQuery.QueryCategory.CTL, net.tapaal.gui.petrinet.verification.TAPNQuery.AlgorithmOption.CERTAIN_ZERO, false, reductionTime, false, null, false, false, false, false, false, false, false, options.useExplicitSearch());
                // XXX: needs refactoring, will only work if the model verified in the one on top (using getCurrentTab)
                KBoundAnalyzer optimizer = new KBoundAnalyzer(model, TAPAALGUI.getCurrentTab().lens, guiModels, options.extraTokens(), modelChecker, new MessengerImpl(), spinner, dataLayerQuery);
                optimizer.analyze((VerifyTAPNOptions) options, true);
            }
            if (result.getQueryResult() != null && result.getQueryResult().isQuerySatisfied() && result.getTrace() != null) {
                firePropertyChange("unfolding", StateValue.PENDING, StateValue.DONE);
            }
		} else {
			modelChecker.kill();
			messenger.displayInfoMessage("Verification was interrupted by the user. No result found!", "Verification Cancelled");

		}
	}

    private String error;
	private void showErrorMessage(String errorMessage) {
		error = errorMessage;
		//The invoke later will make sure all the verification is finished before showing the error
		SwingUtilities.invokeLater(() -> {
			messenger.displayErrorMessage("The engine selected in the query dialog cannot verify this model.\nPlease choose another engine.\n" + error);
			TAPAALGUI.getCurrentTab().editSelectedQuery();
		});
	}

	protected abstract boolean showResult(VerificationResult<TAPNNetworkTrace> result);
}
