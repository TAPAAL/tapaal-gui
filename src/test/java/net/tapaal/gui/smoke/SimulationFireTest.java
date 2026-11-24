package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Enters simulation mode, fires a transition by clicking it, steps back and forward, and leaves simulation
 * mode with the initial marking restored.
 *
 * <p>Recipe for the simulator. It needs no verification engine; verifying queries does and is out of scope.
 */
class SimulationFireTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void clickingAnEnabledTransitionFiresIt() throws Exception {
        var tab = gui.open("move-place/before.tapn");
        var initial = Map.of("P0", 1, "P1", 0);
        var fired = Map.of("P0", 0, "P1", 1);

        gui.menu("Simulator", "Simulation mode");
        assertTrue(gui.onEdt(tab::isInAnimationMode));

        gui.clickObject(tab, "T0");
        assertEquals(fired, gui.marking(tab), "Clicking the enabled transition should fire it");
        gui.menu("Simulator", "Step backward");
        assertEquals(initial, gui.marking(tab));
        gui.menu("Simulator", "Step forward");
        assertEquals(fired, gui.marking(tab));

        gui.keys("M");
        assertFalse(gui.onEdt(tab::isInAnimationMode), "M should leave simulation mode");
        assertEquals(initial, gui.marking(tab), "Leaving simulation mode should restore the initial marking");
        assertFalse(gui.onEdt(tab::getNetChanged), "Simulating should not change the net");
    }
}
