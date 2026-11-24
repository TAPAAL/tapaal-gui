package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Map;

import static net.tapaal.gui.smoke.GuiHarness.menuItem;
import static org.junit.jupiter.api.Assertions.*;

class SimulationRegressionBranchHistoryTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void firingADifferentBranchReplacesTheAbandonedFuture() throws Exception {
        var tab = gui.open("simulation-branch/model.tapn");
        gui.menu("Simulator", "Simulation mode");
        gui.clickObject(tab, "GoLeft");
        gui.menu("Simulator", "Step backward");
        assertTrue(gui.onEdt(() -> menuItem("Simulator", "Step forward").isEnabled()));

        gui.clickObject(tab, "GoRight");

        var history = gui.onEdt(tab::getAnimationHistorySidePanel);
        assertEquals(3, gui.onEdt(() -> history.getModel().getSize()));
        assertEquals("Initial Marking", gui.onEdt(() -> history.getElement(0)));
        assertTrue(gui.onEdt(() -> history.getElement(2).contains("Deadlock")));
        assertEquals("Branch.GoRight", gui.onEdt(history::getSelectedValue));
        assertFalse(gui.onEdt(() -> menuItem("Simulator", "Step forward").isEnabled()));

        gui.menu("Simulator", "Step backward");
        gui.menu("Simulator", "Step forward");
        assertEquals(Map.of("Source", 0, "Left", 0, "Right", 1), gui.marking(tab));
    }
}
