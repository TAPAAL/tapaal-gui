package dk.aau.cs.verification.VerifyTAPN;

import java.util.function.Function;

import dk.aau.cs.io.NamePurifier;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedTransition;
import pipe.gui.petrinet.dataLayer.DataLayer;
import pipe.gui.petrinet.graphicElements.Transition;

final class EngineNameResolver {
    private EngineNameResolver() {}

    static TimedTransition findTransition(TimedArcPetriNet net, String id) {
        return find(net::getTransitionByName, id);
    }

    static Transition findTransition(DataLayer net, String id) {
        return find(net::getTransitionByName, id);
    }

    static TimedPlace findPlace(TimedArcPetriNet net, String id) {
        return find(net::getPlaceByName, id);
    }

    private static <T> T find(Function<String, T> lookup, String id) {
        T node = lookup.apply(id);
        return node != null ? node : lookup.apply(NamePurifier.purify(id));
    }
}
