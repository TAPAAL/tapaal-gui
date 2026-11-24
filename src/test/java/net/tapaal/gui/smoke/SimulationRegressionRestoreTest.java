package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.math.BigDecimal;
import java.util.Map;

import static net.tapaal.gui.smoke.GuiHarness.menuItem;
import static net.tapaal.gui.smoke.GuiHarness.place;
import static org.junit.jupiter.api.Assertions.*;

class SimulationRegressionRestoreTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void leavingRestoresTokenAgesAndReentryStartsFresh() throws Exception {
        var tab = gui.open("simulation-restore/model.tapn");
        assertEquals(new BigDecimal("2.5"), gui.onEdt(() -> place(tab, "Source").underlyingPlace().tokens()
            .get(0).age().stripTrailingZeros()));
        gui.menu("Simulator", "Simulation mode");
        gui.keys("W");
        assertEquals(new BigDecimal("3.5"), gui.onEdt(() -> place(tab, "Source").underlyingPlace().tokens()
            .get(0).age().stripTrailingZeros()));
        gui.clickObject(tab, "Transfer");
        assertEquals(Map.of("Source", 0, "Destination", 1), gui.marking(tab));

        gui.menu("Simulator", "Simulation mode");

        assertFalse(gui.onEdt(tab::isInAnimationMode));
        assertEquals(Map.of("Source", 1, "Destination", 0), gui.marking(tab));
        assertEquals(new BigDecimal("2.5"), gui.onEdt(() -> place(tab, "Source").underlyingPlace().tokens()
            .get(0).age().stripTrailingZeros()));
        assertFalse(gui.onEdt(tab::getNetChanged));

        gui.menu("Simulator", "Simulation mode");

        var history = gui.onEdt(tab::getAnimationHistorySidePanel);
        assertEquals("Initial Marking", gui.onEdt(history::getSelectedValue));
        assertEquals(1, gui.onEdt(() -> history.getModel().getSize()));
        assertFalse(gui.onEdt(() -> menuItem("Simulator", "Step forward").isEnabled()));
    }
}
