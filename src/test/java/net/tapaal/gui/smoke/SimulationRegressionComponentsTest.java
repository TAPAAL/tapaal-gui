package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SimulationRegressionComponentsTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void theSelectedComponentOwnsItsFiringAndTheEditorSelectionReturns() throws Exception {
        var tab = gui.open("simulation-components/model.tapn");
        var editorComponents = gui.onEdt(() -> tab.getTemplateExplorer().getJList());
        gui.click(gui.centerOfRow(editorComponents, 1));
        assertEquals("Second", gui.onEdt(() -> tab.currentTemplate().model().name()));

        gui.menu("Simulator", "Simulation mode");
        assertEquals("Second", gui.onEdt(() -> tab.currentTemplate().model().name()));
        gui.clickObject(tab, "Advance");
        assertEquals(Map.of("Independent", 1, "Finished", 1), gui.marking(tab));
        assertEquals("Second.Advance", gui.onEdt(() -> tab.getAnimationHistorySidePanel().getSelectedValue()));

        gui.menu("Simulator", "Previous component");
        assertEquals("First", gui.onEdt(() -> tab.currentTemplate().model().name()));
        assertEquals(Map.of("Ready", 1, "Done", 0), gui.marking(tab));

        gui.menu("Simulator", "Simulation mode");
        assertEquals("Second", gui.onEdt(() -> tab.currentTemplate().model().name()));
        assertEquals(Map.of("Independent", 2, "Finished", 0), gui.marking(tab));
    }
}
