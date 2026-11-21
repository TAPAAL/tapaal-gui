package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Map;

import static net.tapaal.gui.smoke.GuiHarness.transition;
import static org.junit.jupiter.api.Assertions.*;

class SimulationRegressionIntervalBoundsTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void anOpenLowerBoundAndClosedUpperBoundUseDifferentRules() throws Exception {
        var tab = gui.open("simulation-window/model.tapn");
        gui.menu("Simulator", "Simulation mode");

        gui.keys("W");
        assertFalse(gui.onEdt(() -> transition(tab, "Inside").isTransitionEnabled()),
            "Age one is excluded by the open lower bound");

        gui.keys("W");
        assertTrue(gui.onEdt(() -> transition(tab, "Inside").isTransitionEnabled()),
            "Age two is included by the closed upper bound");

        gui.keys("W");
        assertFalse(gui.onEdt(() -> transition(tab, "Inside").isTransitionEnabled()),
            "Age three is outside the firing window");
        assertEquals(Map.of("Source", 1, "Destination", 0), gui.marking(tab));
    }
}
