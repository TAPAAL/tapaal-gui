package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CanvasRemoveTokensTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void removingFromAnEmptyPlaceDoesNotCreateAnUndoStep() throws Exception {
        var tab = gui.open("canvas-workbench/before.tapn");

        gui.selectTool(tab, "Delete token");
        gui.clickObject(tab, "P1");
        assertEquals(Map.of("P0", 1, "P1", 0), gui.marking(tab));
        assertFalse(gui.onEdt(tab::getNetChanged));
        assertFalse(gui.onEdt(() -> GuiHarness.menuItem("Edit", "Undo").isEnabled()));

        gui.clickObject(tab, "P0");
        gui.clickObject(tab, "P0");
        assertEquals(Map.of("P0", 0, "P1", 0), gui.marking(tab));
        gui.menu("Edit", "Undo");
        assertEquals(Map.of("P0", 1, "P1", 0), gui.marking(tab));
        gui.menu("Edit", "Redo");
        assertEquals(Map.of("P0", 0, "P1", 0), gui.marking(tab));
    }
}
