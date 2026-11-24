package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Map;

import static net.tapaal.gui.smoke.GuiHarness.transition;
import static org.junit.jupiter.api.Assertions.*;

class SimulationRegressionInhibitorThresholdTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void oneFewerGuardTokenEnablesDeliveryWithoutConsumingTheGuard() throws Exception {
        var tab = gui.open("simulation-inhibitor/model.tapn");
        gui.menu("Simulator", "Simulation mode");
        assertFalse(gui.onEdt(() -> transition(tab, "Deliver").isTransitionEnabled()),
            "Two guard tokens meet the inhibitor threshold");

        gui.clickObject(tab, "ClearOne");
        assertEquals(Map.of("Guard", 1, "Supply", 1, "Delivered", 0), gui.marking(tab));
        assertTrue(gui.onEdt(() -> transition(tab, "Deliver").isTransitionEnabled()));

        gui.clickObject(tab, "Deliver");

        assertEquals(Map.of("Guard", 1, "Supply", 0, "Delivered", 1), gui.marking(tab));
    }
}
