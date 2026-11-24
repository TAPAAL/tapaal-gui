package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Draws a transition and an arc with keyboard and menu tools, deletes a shift-click selection with the
 * Delete key, restores it from the toolbar and deletes a selected place from its popup menu.
 *
 * <p>Recipe for canvas editing: choose a tool, click canvas points and named objects, assert on names and arcs.
 */
class DrawSelectDeleteTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void drawSelectDeleteAndRestore() throws Exception {
        var tab = gui.open("one-place.tapn");

        gui.keys("T");
        assertTrue(gui.onEdt(() -> GuiHarness.tool(tab, "Transition").isSelected()), "The T key should choose the Transition tool");
        gui.clickCanvas(tab, 225, 45);
        assertEquals(List.of("T0"), gui.transitionNames(tab));

        gui.selectTool(tab, "Arc");
        gui.clickObject(tab, "Start");
        gui.clickObject(tab, "T0");
        assertEquals(List.of("Start -> T0"), gui.arcs(tab));

        gui.selectTool(tab, "Select");
        gui.clickObject(tab, "Start");
        gui.holding("shift", () -> gui.clickObject(tab, "T0"));
        gui.keys("DELETE");
        assertEquals(Map.of(), gui.marking(tab), "Delete should remove the selected place");
        assertEquals(List.of(), gui.transitionNames(tab), "Delete should remove the shift-clicked transition");
        assertEquals(List.of(), gui.arcs(tab));

        gui.toolbar("Undo");
        assertEquals(Map.of("Start", 1), gui.marking(tab));
        assertEquals(List.of("T0"), gui.transitionNames(tab));
        assertEquals(List.of("Start -> T0"), gui.arcs(tab), "Undo should restore the arc with its ends");

        // The popup's Delete acts on the selection and a right click does not select, so select Start first.
        gui.clickCanvas(tab, 450, 300);
        gui.clickObject(tab, "Start");
        gui.rightClickObject(tab, "Start");
        gui.clickPopupItem("Delete");
        assertEquals(Map.of(), gui.marking(tab));
        assertEquals(List.of("T0"), gui.transitionNames(tab), "Deleting a place keeps the transition");
        assertEquals(List.of(), gui.arcs(tab), "Deleting a place removes its arcs");
    }
}
