package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import pipe.gui.petrinet.PetriNetTab;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CanvasUrgentTransitionCreationTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void urgentToolCreatesAnUrgentTransitionAndRedoPreservesUrgency() throws Exception {
        var tab = gui.open("canvas-workbench/before.tapn");

        gui.selectTool(tab, "Urgent transition");
        gui.clickCanvas(tab, 240, 180);
        assertUrgentTransition(tab);

        gui.menu("Edit", "Undo");
        assertEquals(List.of("T0"), gui.transitionNames(tab));
        gui.menu("Edit", "Redo");
        assertUrgentTransition(tab);
    }

    private void assertUrgentTransition(PetriNetTab tab) throws Exception {
        assertEquals(List.of("T0", "T1"), gui.transitionNames(tab));
        gui.onEdt(() -> {
            var transition = GuiHarness.transition(tab, "T1");
            assertSame(tab.currentTemplate().model().getTransitionByName("T1"), transition.underlyingTransition());
            assertTrue(transition.isUrgent());
            assertFalse(GuiHarness.transition(tab, "T0").isUrgent());
            return null;
        });
    }
}
