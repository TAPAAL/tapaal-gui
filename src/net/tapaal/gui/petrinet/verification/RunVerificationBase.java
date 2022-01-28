package net.tapaal.gui.petrinet.verification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.io.File;
import java.util.concurrent.ExecutionException;

import javax.swing.*;

import pipe.gui.petrinet.PetriNetTab;
import dk.aau.cs.io.queries.XMLQueryLoader;
import dk.aau.cs.verification.*;
import pipe.gui.petrinet.dataLayer.DataLayer;
import dk.aau.cs.verification.VerifyTAPN.VerifyTAPNOptions;
import net.tapaal.gui.petrinet.verification.TAPNQuery.SearchOption;
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
    PetriNetTab.TAPNLens lens;

    public RunVerificationBase(ModelChecker modelChecker, Messenger messenger, HashMap<TimedArcPetriNet, DataLayer> guiModels, String reducedNetFilePath, boolean reduceNetOnly, JSpinner spinner) {
		super();
		this.modelChecker = modelChecker;
		this.messenger = messenger;
		this.guiModels = guiModels;
		this.reducedNetFilePath = reducedNetFilePath;
		this.reduceNetOnly = reduceNetOnly;
        this.spinner = spinner;
    }

    public void execute(VerificationOptions options, TimedArcPetriNetNetwork model, TAPNQuery query, net.tapaal.gui.petrinet.verification.TAPNQuery dataLayerQuery, PetriNetTab.TAPNLens lens) {
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

        clonedQuery = new TAPNQuery(query.getProperty().copy(), query.getExtraTokens());
        MapQueryToNewNames(clonedQuery, transformedModel.value2());

        if (dataLayerQuery != null) {
            clonedQuery.setCategory(dataLayerQuery.getCategory()); // Used by the CTL engine
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
                    VerificationResult<TimedArcPetriNetTrace> overapprox_result = null;
                    if (dataLayerQuery != null) {
                        overapprox_result = verifypn.verify(
                            new VerifyPNOptions(
                                options.extraTokens(),
                                options.traceOption(),
                                SearchOption.OVERAPPROXIMATE,
                                true,
                                ModelReduction.AGGRESSIVE,
                                options.enabledOverApproximation(),
                                options.enabledUnderApproximation(),
                                options.approximationDenominator(),
                                dataLayerQuery.getCategory(),
                                dataLayerQuery.getAlgorithmOption(),
                                dataLayerQuery.isSiphontrapEnabled(),
                                dataLayerQuery.isQueryReductionEnabled() ? net.tapaal.gui.petrinet.verification.TAPNQuery.QueryReductionTime.UnlimitedTime : net.tapaal.gui.petrinet.verification.TAPNQuery.QueryReductionTime.NoTime,
                                dataLayerQuery.isStubbornReductionEnabled(),
                                reducedNetFilePath,
                                dataLayerQuery.isTarOptionEnabled(),
                                dataLayerQuery.isTarjan(),
                                model.isColored(),
                                model.isColored() && (!model.isUntimed() || options.traceOption() != net.tapaal.gui.petrinet.verification.TAPNQuery.TraceOption.NONE),
                                dataLayerQuery.usePartitioning(),
                                dataLayerQuery.useColorFixpoint(),
                                dataLayerQuery.useSymmetricVars()
                            ),
                            transformedModel,
                            clonedQuery,
                            composer.getGuiModel(),
                            dataLayerQuery,
                            null);
                    } else { // TODO: FIX! If datalayer is null then we can't check datalayer's values...
                        overapprox_result = verifypn.verify(
                            new VerifyPNOptions(
                                options.extraTokens(),
                                options.traceOption(),
                                SearchOption.OVERAPPROXIMATE,
                                true,
                                ModelReduction.AGGRESSIVE,
                                options.enabledOverApproximation(),
                                options.enabledUnderApproximation(),
                                options.approximationDenominator(),
                                net.tapaal.gui.petrinet.verification.TAPNQuery.QueryCategory.Default,
                                net.tapaal.gui.petrinet.verification.TAPNQuery.AlgorithmOption.CERTAIN_ZERO,
                                false,
                                net.tapaal.gui.petrinet.verification.TAPNQuery.QueryReductionTime.UnlimitedTime,
                                false,
                                reducedNetFilePath,
                                false,
                                true,
                                model.isColored(),
                                model.isColored() && (!model.isUntimed() || options.traceOption() != net.tapaal.gui.petrinet.verification.TAPNQuery.TraceOption.NONE),
                                dataLayerQuery.usePartitioning(),
                                dataLayerQuery.useColorFixpoint(),
                                dataLayerQuery.useSymmetricVars()
                            ),
                            transformedModel,
                            clonedQuery,
                            composer.getGuiModel(),
                            dataLayerQuery,
                            null);
                    }

                    if (overapprox_result.getQueryResult() != null) {
                        if (!overapprox_result.error() && model.isUntimed() || (
                            (query.queryType() == QueryType.EF && !overapprox_result.getQueryResult().isQuerySatisfied()) ||
                                (query.queryType() == QueryType.AG && overapprox_result.getQueryResult().isQuerySatisfied()))
                        ) {
                            VerificationResult<TAPNNetworkTrace> value = new VerificationResult<TAPNNetworkTrace>(
                                overapprox_result.getQueryResult(),
                                decomposeTrace(overapprox_result.getTrace(), transformedModel.value2()),
                                overapprox_result.verificationTime(),
                                overapprox_result.stats(),
                                true
                            );
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

    private static net.tapaal.gui.petrinet.verification.TAPNQuery getQuery(File queryFile, TimedArcPetriNetNetwork network) {
        XMLQueryLoader queryLoader = new XMLQueryLoader(queryFile, network);
        List<net.tapaal.gui.petrinet.verification.TAPNQuery> queries = new ArrayList<net.tapaal.gui.petrinet.verification.TAPNQuery>();
        queries.addAll(queryLoader.parseQueries().getQueries());
        return queries.get(0);
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

			if (showResult(result) && spinner != null) {
			    options = new VerifyPNOptions(options.extraTokens(), net.tapaal.gui.petrinet.verification.TAPNQuery.TraceOption.NONE, SearchOption.BFS, false, ModelReduction.BOUNDPRESERVING, false, false, 1, net.tapaal.gui.petrinet.verification.TAPNQuery.QueryCategory.Default, net.tapaal.gui.petrinet.verification.TAPNQuery.AlgorithmOption.CERTAIN_ZERO, false, net.tapaal.gui.petrinet.verification.TAPNQuery.QueryReductionTime.NoTime, false, null, false, false, false, false, false, false, false);
                // XXX: needs refactoring, will only work if the model verified in the one on top (using getCurrentTab)
                KBoundAnalyzer optimizer = new KBoundAnalyzer(model, TAPAALGUI.getCurrentTab().lens, guiModels, options.extraTokens(), modelChecker, new MessengerImpl(), spinner);
                optimizer.analyze((VerifyTAPNOptions) options, true);
            }
            if(result.getQueryResult().isQuerySatisfied() && result.getTrace() != null){
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
