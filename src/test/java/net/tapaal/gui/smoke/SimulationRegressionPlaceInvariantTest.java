package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.math.BigDecimal;

import static net.tapaal.gui.smoke.GuiHarness.place;
import static org.junit.jupiter.api.Assertions.*;

class SimulationRegressionPlaceInvariantTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void theDelayShortcutCannotCrossAPlaceInvariant() throws Exception {
        var tab = gui.open("simulation-invariant/model.tapn");
        gui.menu("Simulator", "Simulation mode");
        var history = gui.onEdt(tab::getAnimationHistorySidePanel);
        var delay = gui.onEdt(() -> tab.getAnimationController().getOkButton());

        gui.keys("W");
        assertTrue(gui.onEdt(delay::isEnabled), "A delay reaching the closed bound should be possible");
        gui.keys("W");
        assertEquals(2, gui.onEdt(history::getSelectedIndex));
        assertFalse(gui.onEdt(delay::isEnabled));
        assertTrue(gui.onEdt(() -> delay.getToolTipText().contains("Deadline.Clock")));

        gui.keys("W");

        assertEquals(2, gui.onEdt(history::getSelectedIndex));
        assertEquals(3, gui.onEdt(() -> history.getModel().getSize()));
        assertEquals(BigDecimal.valueOf(2), gui.onEdt(() -> place(tab, "Clock").underlyingPlace().tokens()
            .get(0).age().stripTrailingZeros()));
    }
}
