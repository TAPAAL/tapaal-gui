package pipe.gui;

import javax.swing.JSpinner;

import dk.aau.cs.TCTL.*;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.util.Tuple;
import dk.aau.cs.verification.NameMapping;
import dk.aau.cs.verification.TAPNComposer;
import pipe.dataLayer.DataLayer;
import pipe.dataLayer.TAPNQuery.SearchOption;
import pipe.dataLayer.TAPNQuery.TraceOption;
import pipe.dataLayer.TAPNQuery.AlgorithmOption;
import pipe.dataLayer.TAPNQuery.QueryCategory;
import pipe.gui.widgets.RunningVerificationDialog;
import dk.aau.cs.Messenger;
import dk.aau.cs.model.tapn.TAPNQuery;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.verification.ModelChecker;
import dk.aau.cs.verification.VerifyTAPN.ModelReduction;
import dk.aau.cs.verification.VerifyTAPN.VerifyDTAPNOptions;
import dk.aau.cs.verification.VerifyTAPN.VerifyPN;
import dk.aau.cs.verification.VerifyTAPN.VerifyPNOptions;
import dk.aau.cs.verification.VerifyTAPN.VerifyTAPN;
import dk.aau.cs.verification.VerifyTAPN.VerifyTAPNDiscreteVerification;
import dk.aau.cs.verification.VerifyTAPN.VerifyTAPNOptions;

import java.util.ArrayList;
import java.util.HashMap;

public class KBoundAnalyzer {
	protected TimedArcPetriNetNetwork tapnNetwork;
	protected int k;

	private final ModelChecker modelChecker;
	private final Messenger messenger;
	private final JSpinner spinner;

	public KBoundAnalyzer(TimedArcPetriNetNetwork tapnNetwork, int k,
			ModelChecker modelChecker, Messenger messenger, JSpinner tokensControl) {
		this.k = k;
		this.tapnNetwork = tapnNetwork;
		this.modelChecker = modelChecker;
		this.messenger = messenger;
		spinner = tokensControl;
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

        RunKBoundAnalysis analyzer = new RunKBoundAnalysis(modelChecker, messenger, spinner, resultShown);
        RunningVerificationDialog dialog = new RunningVerificationDialog(CreateGui.getApp(), analyzer);

        analyzer.execute(options, tapnNetwork, query, null);
        dialog.setVisible(true);
    }

	protected VerifyTAPNOptions verificationOptions() {
		if(modelChecker instanceof VerifyPN){
			return new VerifyPNOptions(k, TraceOption.NONE, SearchOption.BFS, false, ModelReduction.BOUNDPRESERVING, false, false, 1, QueryCategory.Default, AlgorithmOption.CERTAIN_ZERO, false, pipe.dataLayer.TAPNQuery.QueryReductionTime.UnlimitedTime, false, null, false, true);
		} else if(modelChecker instanceof VerifyTAPN){
			return new VerifyTAPNOptions(k, TraceOption.NONE, SearchOption.BFS, true, false, true, false, false, 1);
		} else if(modelChecker instanceof VerifyTAPNDiscreteVerification){
			return new VerifyDTAPNOptions(true, k, TraceOption.NONE, SearchOption.BFS, true, !tapnNetwork.hasUrgentTransitions(), true, false, false, 1, false);
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
        HashMap<TimedArcPetriNet, DataLayer> guiModels = CreateGui.getCurrentTab().getGuiModels();
        for (TimedArcPetriNet net : guiModels.keySet()) {
            if (tapnNetwork.getTAPNByName(net.name()) != null) {
                DataLayer dl = guiModels.get(net);
                guiModels.remove(net);
                guiModels.put(tapnNetwork.getTAPNByName(net.name()), dl);
            }
        }
        TAPNComposer composer = new TAPNComposer(new MessengerImpl(), guiModels, true, true);
        Tuple<TimedArcPetriNet, NameMapping> transformedModel = composer.transformModel(tapnNetwork);

        return transformedModel.value1();
    }
}
