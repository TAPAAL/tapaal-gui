package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.math.BigDecimal;
import java.util.Map;

import static net.tapaal.gui.smoke.GuiHarness.place;
import static org.junit.jupiter.api.Assertions.*;

class SimulationRegressionUrgencyTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void firingTheLastUrgentTransitionUnblocksTime() throws Exception {
        var tab = gui.open("simulation-urgent/model.tapn");
        gui.menu("Simulator", "Simulation mode");
        var delay = gui.onEdt(() -> tab.getAnimationController().getOkButton());

        assertFalse(gui.onEdt(delay::isEnabled));
        assertTrue(gui.onEdt(() -> delay.getToolTipText().contains("Urgency.Immediate")));
        gui.keys("W");
        assertEquals("Initial Marking", gui.onEdt(() -> tab.getAnimationHistorySidePanel().getSelectedValue()));

        gui.clickObject(tab, "Immediate");
        assertTrue(gui.onEdt(delay::isEnabled));
        gui.keys("W");

        assertEquals(Map.of("Ready", 0, "Done", 1), gui.marking(tab));
        assertEquals(BigDecimal.ONE, gui.onEdt(() -> place(tab, "Done").underlyingPlace().tokens()
            .get(0).age().stripTrailingZeros()));
    }
}
