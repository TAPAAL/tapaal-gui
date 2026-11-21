package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Map;

import static net.tapaal.gui.smoke.GuiHarness.transition;
import static org.junit.jupiter.api.Assertions.*;

class SimulationRegressionDisabledCanvasTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void clickingTheLosingBranchDoesNotFireOrAppendHistory() throws Exception {
        var tab = gui.open("simulation-branch/model.tapn");
        gui.menu("Simulator", "Simulation mode");
        gui.clickObject(tab, "GoLeft");
        var history = gui.onEdt(tab::getAnimationHistorySidePanel);
        assertEquals("Branch.GoLeft", gui.onEdt(history::getSelectedValue));
        assertFalse(gui.onEdt(() -> transition(tab, "GoRight").isTransitionEnabled()));

        gui.clickObject(tab, "GoRight");

        assertEquals(Map.of("Source", 0, "Left", 1, "Right", 0), gui.marking(tab));
        assertEquals(1, gui.onEdt(history::getSelectedIndex));
        assertEquals("Branch.GoLeft", gui.onEdt(history::getSelectedValue));
        assertEquals(3, gui.onEdt(() -> history.getModel().getSize()));
        assertTrue(gui.onEdt(() -> history.getElement(2).contains("Deadlock")));
    }
}
