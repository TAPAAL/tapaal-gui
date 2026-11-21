package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.math.BigDecimal;
import java.util.Map;

import static net.tapaal.gui.smoke.GuiHarness.menuItem;
import static net.tapaal.gui.smoke.GuiHarness.place;
import static org.junit.jupiter.api.Assertions.*;

class SimulationRegressionTimedReplayTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void steppingAcrossADelayRestoresItsClockAndButtonState() throws Exception {
        var tab = gui.open("move-place/before.tapn");
        gui.menu("Simulator", "Simulation mode");
        var history = gui.onEdt(tab::getAnimationHistorySidePanel);
        assertFalse(gui.onEdt(() -> menuItem("Simulator", "Step backward").isEnabled()));
        assertFalse(gui.onEdt(() -> menuItem("Simulator", "Step forward").isEnabled()));

        gui.keys("W");
        gui.clickObject(tab, "T0");
        gui.menu("Simulator", "Step backward");
        assertEquals(Map.of("P0", 1, "P1", 0), gui.marking(tab));
        assertEquals(1, gui.onEdt(history::getSelectedIndex));
        assertEquals(BigDecimal.ONE, gui.onEdt(() -> place(tab, "P0").underlyingPlace().tokens()
            .get(0).age().stripTrailingZeros()));

        gui.menu("Simulator", "Step backward");
        assertEquals("Initial Marking", gui.onEdt(history::getSelectedValue));
        assertFalse(gui.onEdt(() -> menuItem("Simulator", "Step backward").isEnabled()));
        assertTrue(gui.onEdt(() -> menuItem("Simulator", "Step forward").isEnabled()));
        assertEquals(BigDecimal.ZERO, gui.onEdt(() -> place(tab, "P0").underlyingPlace().tokens()
            .get(0).age().stripTrailingZeros()));

        gui.menu("Simulator", "Step forward");

        assertEquals(1, gui.onEdt(history::getSelectedIndex));
        assertEquals(BigDecimal.ONE, gui.onEdt(() -> place(tab, "P0").underlyingPlace().tokens()
            .get(0).age().stripTrailingZeros()));
    }
}
