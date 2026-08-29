package net.tapaal.gui.petrinet.model;

import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.util.Tuple;
import dk.aau.cs.verification.NameMapping;
import dk.aau.cs.verification.TAPNModelComposer;

/**
 * Application-layer queries that require composing a network. Composition is
 * intentionally kept out of the domain network itself. This path uses the
 * model-only composer and does not construct a diagram.
 */
public final class NetworkAnalysis {
    private NetworkAnalysis() {
    }

    public static boolean isDegree2(TimedArcPetriNetNetwork network) {
        return compose(network).value1().isDegree2();
    }

    public static int highestNetDegree(TimedArcPetriNetNetwork network) {
        return compose(network).value1().getHighestNetDegree();
    }

    private static Tuple<TimedArcPetriNet, NameMapping> compose(TimedArcPetriNetNetwork network) {
        TAPNModelComposer composer = new TAPNModelComposer(false);
        return composer.transformModel(network);
    }
}
