package net.tapaal.gui.petrinet.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;

class NetworkAnalysisTest {

    @Test
    void compositionMetricsAreAvailableWithoutPuttingComposerInTheDomainNetwork() {
        TimedArcPetriNetNetwork network = new TimedArcPetriNetNetwork();
        network.add(new TimedArcPetriNet("Template"));

        assertTrue(NetworkAnalysis.isDegree2(network));
        assertEquals(0, NetworkAnalysis.highestNetDegree(network));
    }

    @Test
    void drawablePolicyHonorsTheNetworkDisplayFlag() {
        TimedArcPetriNetNetwork network = new TimedArcPetriNetNetwork();
        assertTrue(NetworkDisplayPolicy.isDrawable(network));

        network.setPaintNet(false);
        assertFalse(NetworkDisplayPolicy.isDrawable(network));
    }
}
