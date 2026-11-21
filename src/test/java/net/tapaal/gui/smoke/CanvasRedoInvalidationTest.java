package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CanvasRedoInvalidationTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void aNewCanvasEditAfterUndoReplacesTheRedoBranch() throws Exception {
        var tab = gui.open("canvas-workbench/before.tapn");

        gui.selectTool(tab, "Transition");
        gui.clickCanvas(tab, 240, 180);
        assertEquals(List.of("T0", "T1"), gui.transitionNames(tab));

        gui.menu("Edit", "Undo");
        assertEquals(List.of("T0"), gui.transitionNames(tab));
        assertTrue(gui.onEdt(() -> GuiHarness.menuItem("Edit", "Redo").isEnabled()));

        gui.selectTool(tab, "Add token");
        gui.clickObject(tab, "P0");
        assertEquals(Map.of("P0", 2, "P1", 0), gui.marking(tab));
        assertFalse(gui.onEdt(() -> GuiHarness.menuItem("Edit", "Redo").isEnabled()));

        gui.menu("Edit", "Undo");
        assertEquals(Map.of("P0", 1, "P1", 0), gui.marking(tab));
        gui.menu("Edit", "Redo");
        assertEquals(Map.of("P0", 2, "P1", 0), gui.marking(tab));
        assertEquals(List.of("T0"), gui.transitionNames(tab), "Redo must only replay the replacement edit");
    }
}
