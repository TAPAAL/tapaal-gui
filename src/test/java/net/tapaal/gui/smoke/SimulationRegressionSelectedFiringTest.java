package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.swing.JList;
import java.util.Map;

import static net.tapaal.gui.smoke.GuiHarness.find;
import static org.junit.jupiter.api.Assertions.*;

class SimulationRegressionSelectedFiringTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void firesTheTransitionSelectedInTheSidePanel() throws Exception {
        var tab = gui.open("simulation-branch/model.tapn");

        gui.toolbar("Simulation mode");
        assertTrue(gui.onEdt(tab::isInAnimationMode));
        var history = gui.onEdt(tab::getAnimationHistorySidePanel);
        assertEquals("Initial Marking", gui.onEdt(history::getSelectedValue));
        var enabled = gui.onEdt(() -> find(tab.getTransitionFiringComponent(), JList.class, list -> true));
        assertEquals(2, gui.onEdt(() -> enabled.getModel().getSize()));
        assertTrue(gui.onEdt(() -> enabled.getModel().getElementAt(1).toString().endsWith("Branch.GoRight")));

        gui.click(gui.centerOfRow(enabled, 1));
        gui.menu("Simulator", "Delay and fire");

        assertEquals(Map.of("Source", 0, "Left", 0, "Right", 1), gui.marking(tab));
        assertEquals("Branch.GoRight", gui.onEdt(history::getSelectedValue));
    }
}
