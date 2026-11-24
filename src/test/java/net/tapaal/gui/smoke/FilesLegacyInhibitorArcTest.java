package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class FilesLegacyInhibitorArcTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void anOldInhibitorTagLoadsWithItsOriginalTypeAndDefaultWeight() throws Exception {
        var tab = gui.open("files-legacy-inhibitor/guard.tapn");

        assertEquals(Map.of("Guard", 2), gui.marking(tab));
        assertEquals(List.of("Release"), gui.transitionNames(tab));
        assertEquals(List.of("Guard -> Release"), gui.arcs(tab));
        var inhibitorWeights = gui.onEdt(() -> StreamSupport
            .stream(tab.currentTemplate().model().inhibitorArcs().spliterator(), false)
            .map(arc -> arc.getWeight().value()).toList());
        assertEquals(List.of(1), inhibitorWeights, "tapnInhibitor should load as an inhibitor arc with weight one");
        assertFalse(gui.onEdt(tab::getNetChanged));
    }
}
