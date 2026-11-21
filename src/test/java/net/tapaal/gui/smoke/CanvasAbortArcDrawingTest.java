package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CanvasAbortArcDrawingTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void escapeDiscardsTheUnfinishedSourceAndItsBend() throws Exception {
        var tab = gui.open("canvas-workbench/before.tapn");

        gui.selectTool(tab, "Arc");
        gui.clickObject(tab, "P0");
        gui.clickCanvas(tab, 165, 150);
        gui.keys("ESCAPE");

        assertEquals(List.of(), gui.arcs(tab));
        assertFalse(gui.onEdt(tab::getNetChanged));
        assertFalse(gui.onEdt(() -> GuiHarness.menuItem("Edit", "Undo").isEnabled()));

        gui.clickObject(tab, "T0");
        assertEquals(List.of(), gui.arcs(tab), "T0 should start a fresh gesture");
        gui.clickObject(tab, "P1");
        assertEquals(List.of("T0 -> P1"), gui.arcs(tab));
        assertEquals(2, gui.onEdt(() -> tab.currentTemplate().guiModel().getArcs()[0]
            .getArcPath().getArcPathPoints().size()));
    }
}
