package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.List;
import java.util.Map;

import static net.tapaal.gui.smoke.GuiHarness.transition;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EditorsComponentRemoveTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void removesTheSelectedComponentAndRestoresItOnUndo() throws Exception {
        var tab = gui.open("editors-dialogs/shared-components.tapn");
        var explorer = tab.getTemplateExplorer();
        gui.click(gui.centerOfRow(gui.onEdt(explorer::getJList), 1));
        assertEquals("Consumer", gui.onEdt(() -> tab.currentTemplate().model().name()));
        gui.clickButton(explorer, "Remove");

        assertEquals(1, gui.onEdt(() -> explorer.getJList().getModel().getSize()));
        gui.click(gui.centerOfRow(gui.onEdt(explorer::getJList), 0));
        assertEquals("Producer", gui.onEdt(() -> tab.currentTemplate().model().name()));
        assertEquals(List.of("T0 -> P1"), gui.arcs(tab));

        gui.keys("ctrl Z");
        assertEquals(2, gui.onEdt(() -> explorer.getJList().getModel().getSize()));
        gui.click(gui.centerOfRow(gui.onEdt(explorer::getJList), 1));
        assertEquals("Consumer", gui.onEdt(() -> tab.currentTemplate().model().name()));
        assertEquals(Map.of("P0", 1), gui.marking(tab));
        assertEquals(List.of("T0"), gui.transitionNames(tab));
        assertTrue(gui.onEdt(() -> transition(tab, "T0").underlyingTransition().isShared()));
    }
}
