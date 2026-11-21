package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CanvasDuplicateArcRejectionTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void duplicateInputShowsAnErrorAndLeavesUndoPointingAtTheOriginalArc() throws Exception {
        var tab = gui.open("canvas-workbench/before.tapn");

        gui.selectTool(tab, "Arc");
        gui.clickObject(tab, "P0");
        gui.clickObject(tab, "T0");
        assertEquals(List.of("P0 -> T0"), gui.arcs(tab));

        gui.clickObject(tab, "P0");
        gui.clickObject(tab, "T0");
        var error = gui.awaitDialog("Error");
        assertTrue(gui.onEdt(() -> GuiHarness.dialogText(error))
            .contains("There is already an arc between the selected place and transition"));
        gui.clickButton(error, "OK");
        gui.awaitClosed(error);

        assertEquals(List.of("P0 -> T0"), gui.arcs(tab));
        gui.menu("Edit", "Undo");
        assertEquals(List.of(), gui.arcs(tab), "Rejected arcs must not add an undo step");
        gui.menu("Edit", "Redo");
        assertEquals(List.of("P0 -> T0"), gui.arcs(tab));
    }
}
