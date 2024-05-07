package net.tapaal.gui.petrinet.verification;

import javax.swing.JSpinner;

import dk.aau.cs.TCTL.*;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import net.tapaal.gui.petrinet.TAPNLens;
import pipe.gui.petrinet.dataLayer.DataLayer;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.util.Tuple;
import dk.aau.cs.verification.NameMapping;
import dk.aau.cs.verification.TAPNComposer;
import net.tapaal.gui.petrinet.verification.TAPNQuery.QueryReductionTime;
import net.tapaal.gui.petrinet.verification.TAPNQuery.SearchOption;
import net.tapaal.gui.petrinet.verification.TAPNQuery.TraceOption;
import net.tapaal.gui.petrinet.verification.TAPNQuery.AlgorithmOption;
import net.tapaal.gui.petrinet.verification.TAPNQuery.QueryCategory;
import pipe.gui.TAPAALGUI;
import pipe.gui.MessengerImpl;
import dk.aau.cs.Messenger;
import dk.aau.cs.model.tapn.TAPNQuery;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.verification.ModelChecker;
import dk.aau.cs.verification.VerifyTAPN.ModelReduction;
import dk.aau.cs.verification.VerifyTAPN.VerifyDTAPNOptions;
import dk.aau.cs.verification.VerifyTAPN.VerifyPN;
import dk.aau.cs.verification.VerifyTAPN.VerifyPNOptions;
import dk.aau.cs.verification.VerifyTAPN.VerifyTAPN;
import dk.aau.cs.verification.VerifyTAPN.VerifyDTAPN;
import dk.aau.cs.verification.VerifyTAPN.VerifyTAPNOptions;

import java.util.HashMap;
import java.util.ArrayList;

public class KBoundAnalyzer {
	protected final TimedArcPetriNetNetwork tapnNetwork;

    protected final int k;

    private final TAPNLens lens;
	private final ModelChecker modelChecker;
	private final Messenger messenger;
	private final JSpinner spinner;
	private final HashMap<TimedArcPetriNet, DataLayer> guiModels;
	private final net.tapaal.gui.petrinet.verification.TAPNQuery dataLayerQuery;

    private boolean useRawVerification = false;
    private String rawVerificationPrompt = "";

	public KBoundAnalyzer(TimedArcPetriNetNetwork tapnNetwork, TAPNLens lens, HashMap<TimedArcPetriNet, DataLayer> guiModels, int k,
                          ModelChecker modelChecker, Messenger messenger, JSpinner tokensControl, net.tapaal.gui.petrinet.verification.TAPNQuery query) {
        this.lens = lens;
        this.k = k;
		this.tapnNetwork = tapnNetwork;
		this.modelChecker = modelChecker;
        this.messenger = messenger;
        this.guiModels = guiModels;
		spinner = tokensControl;
        dataLayerQuery = query;

        if (dataLayerQuery != null) {
            useRawVerification = dataLayerQuery.getRawVerification();
            rawVerificationPrompt = dataLayerQuery.getRawVerificationPrompt();
        }
	}

	public void analyze() {
		analyze(verificationOptions(), false);
	}

    public void analyze(VerifyTAPNOptions options, boolean resultShown) {
        TAPNQuery query;
        if (modelChecker instanceof VerifyPN) {
            query = getPNBoundednessQuery();
        } else {
            query = getBoundednessQuery();
        }

        RunKBoundAnalysis analyzer = new RunKBoundAnalysis(modelChecker, messenger, guiModels, spinner, resultShown);
        RunningVerificationDialog dialog = new RunningVerificationDialog(TAPAALGUI.getApp(), analyzer);

        analyzer.execute(options, tapnNetwork, query, dataLayerQuery);
        dialog.setVisible(true);
    }

	protected VerifyTAPNOptions verificationOptions() {
		if(modelChecker instanceof VerifyPN){
			return new VerifyPNOptions(k, TraceOption.NONE, SearchOption.BFS, false, ModelReduction.BOUNDPRESERVING, false, false, 1, QueryCategory.CTL, AlgorithmOption.CERTAIN_ZERO, false, QueryReductionTime.NoTime,false, null, false, true, tapnNetwork.isColored(), false, true, true, true, useRawVerification, rawVerificationPrompt);
		} else if(modelChecker instanceof VerifyTAPN){
			return new VerifyTAPNOptions(k, TraceOption.NONE, SearchOption.BFS, true, false, true, false, false, 1, dataLayerQuery.getRawVerification(), dataLayerQuery.getRawVerificationPrompt());
		} else if(modelChecker instanceof VerifyDTAPN){
            //gdc and dart can be used together with game
            boolean gcd = !lens.isGame();
            boolean dart = !tapnNetwork.hasUrgentTransitions() && !lens.isGame() && !lens.isStochastic();

			return new VerifyDTAPNOptions(true, k, TraceOption.NONE, SearchOption.BFS, true, gcd, dart, true, false, false, 1, false, true, true, tapnNetwork.isColored(), useRawVerification, rawVerificationPrompt);
		}
		return null;
	}

	protected TAPNQuery getBoundednessQuery() {
		TCTLAbstractProperty property = new TCTLAGNode(new TCTLTrueNode());
		return new TAPNQuery(property, k);
	}

	protected TAPNQuery getPNBoundednessQuery() {
        int totalTokens = k + tapnNetwork.marking().size();

        TCTLAtomicPropositionNode child = new TCTLAtomicPropositionNode(new TCTLTermListNode(getFactors()), "<=", new TCTLConstNode(totalTokens));
        TCTLAbstractProperty property = new TCTLAGNode(child);

        TAPNQuery query = new TAPNQuery(property, k);
        query.setCategory(QueryCategory.CTL);

        return query;
    }

    private ArrayList<TCTLAbstractStateProperty> getFactors() {
        TimedArcPetriNet net = mergeNetComponents();
        ArrayList<TCTLAbstractStateProperty> factors = new ArrayList<>();

        tapnNetwork.sharedPlaces().forEach(o -> {
            if (net.getPlaceByName(o.name()) != null && !net.getPlaceByName(o.name()).name().contains("Shared_")) {
                net.getPlaceByName(o.name()).setName("Shared_" + o.name());
            }
        });
        tapnNetwork.allTemplates().forEach(o -> o.places().forEach(x -> {
            if (net.getPlaceByName(x.name()) != null) net.getPlaceByName(x.name()).setName(o.name() + "_" + x.name());
        }));

        for (TimedPlace place : net.places()) {
            factors.add(new TCTLPlaceNode(place.name()));
            factors.add(new AritmeticOperator("+"));
        }
        if (factors.get(factors.size()-1) instanceof AritmeticOperator) factors.remove(factors.size()-1);

        return factors;
    }

    private TimedArcPetriNet mergeNetComponents() {

        HashMap<TimedArcPetriNet, DataLayer> guiModels = this.guiModels;
        HashMap<TimedArcPetriNet, DataLayer> updatedModels = new HashMap<>();

        // FIXME: Fixed issue #1987383, error due to changes to collection while iterating iteration over it.
        //  however, this code seem way out of place and wrong, the way the the update is done is wired.
        //  I guess the update is done due to a de-sync between the tapnNetwork and gui model, but fixing it this way seems
        //  crazy. Better would be to make sure the correct gui model is passed.
        //  The loop looks even weirder now, but for the bugfix I just rewrote the loop to keep the same behaviour as before
        //  -- kyrke 2023-02-09
        for (TimedArcPetriNet net : guiModels.keySet()) {
            if (tapnNetwork.getTAPNByName(net.name()) != null) {
                DataLayer dl = guiModels.get(net);

                updatedModels.put(tapnNetwork.getTAPNByName(net.name()), dl);
            } else {
                updatedModels.put(net, guiModels.get(net));
            }
        }
        TAPNComposer composer = new TAPNComposer(new MessengerImpl(), updatedModels, null, true, true);

        Tuple<TimedArcPetriNet, NameMapping> transformedModel = composer.transformModel(tapnNetwork);

        return transformedModel.value1();
    }
}
