package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Map;

import static net.tapaal.gui.smoke.GuiHarness.tool;
import static org.junit.jupiter.api.Assertions.*;

class SimulationRegressionDrawingLockTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void thePlaceShortcutCannotCreateAPlaceUntilReturningToTheEditor() throws Exception {
        var tab = gui.open("move-place/before.tapn");
        gui.selectTool(tab, "Place");
        gui.menu("Simulator", "Simulation mode");

        assertFalse(gui.onEdt(() -> tool(tab, "Place").isEnabled()));
        gui.keys("P");
        gui.clickCanvas(tab, 150, 225);

        assertEquals(Map.of("P0", 1, "P1", 0), gui.marking(tab));
        assertFalse(gui.onEdt(tab::getNetChanged));

        gui.menu("Simulator", "Simulation mode");
        assertTrue(gui.onEdt(() -> tool(tab, "Place").isEnabled()));

        gui.keys("P");
        gui.clickCanvas(tab, 150, 225);
        assertEquals(Map.of("P0", 1, "P1", 0, "P2", 0), gui.marking(tab));
        assertTrue(gui.onEdt(tab::getNetChanged));
    }
}
