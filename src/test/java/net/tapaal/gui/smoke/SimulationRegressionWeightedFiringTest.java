package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Map;

import static net.tapaal.gui.smoke.GuiHarness.transition;
import static org.junit.jupiter.api.Assertions.*;

class SimulationRegressionWeightedFiringTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void consumesAndProducesTheSeparateArcWeights() throws Exception {
        var tab = gui.open("simulation-weighted/model.tapn");
        assertEquals(Map.of("Supply", 3, "Delivered", 0), gui.marking(tab));
        gui.menu("Simulator", "Simulation mode");
        assertTrue(gui.onEdt(() -> transition(tab, "Ship").isTransitionEnabled()));

        gui.clickObject(tab, "Ship");

        assertEquals(Map.of("Supply", 1, "Delivered", 3), gui.marking(tab));
        assertFalse(gui.onEdt(() -> transition(tab, "Ship").isTransitionEnabled()),
            "One remaining token cannot satisfy an input weight of two");
    }
}
