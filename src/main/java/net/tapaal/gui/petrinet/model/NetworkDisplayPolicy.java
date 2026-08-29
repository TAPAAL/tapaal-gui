package net.tapaal.gui.petrinet.model;

import pipe.gui.Constants;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;

/**
 * Presentation policy for deciding whether a network is safe to render.
 */
public final class NetworkDisplayPolicy {
    private NetworkDisplayPolicy() {
    }

    public static boolean isDrawable(TimedArcPetriNetNetwork network) {
        if (!network.paintNet()) {
            return false;
        }

        int totalSize = 0;
        for (var template : network.allTemplates()) {
            totalSize += template.places().size() + template.transitions().size();
        }
        return totalSize <= Constants.MAX_NET_SIZE;
    }
}
