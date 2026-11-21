package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.swing.JTextPane;

import static org.junit.jupiter.api.Assertions.*;

class ViewQueryBuilderHistoryTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void undoAndRedoRestoreFormulaAndSaveEligibility() throws Exception {
        var tab = gui.open("view-query-fixtures/queries.tapn");
        gui.menu("View", "Show simple workspace");
        gui.clickButton(gui.onEdt(tab::getQueryPane), "New");
        var dialog = gui.awaitDialog("Edit Query");
        var formula = gui.onEdt(() -> GuiHarness.find(dialog, JTextPane.class, field -> !field.isEditable()));
        gui.clickButton(dialog, "EF");
        String incomplete = gui.onEdt(formula::getText);
        assertFalse(gui.onEdt(() -> GuiHarness.button(dialog, "Save").isEnabled()));

        gui.clickButton(dialog, "True");
        assertEquals("EF true", gui.onEdt(formula::getText));
        assertTrue(gui.onEdt(() -> GuiHarness.button(dialog, "Save").isEnabled()));
        gui.clickButton(dialog, "Undo");
        assertEquals(incomplete, gui.onEdt(formula::getText));
        assertFalse(gui.onEdt(() -> GuiHarness.button(dialog, "Save").isEnabled()));
        gui.clickButton(dialog, "Redo");
        assertEquals("EF true", gui.onEdt(formula::getText));
        assertTrue(gui.onEdt(() -> GuiHarness.button(dialog, "Save").isEnabled()));

        gui.clickButton(dialog, "Cancel");
        gui.awaitClosed(dialog);
        assertFalse(gui.onEdt(tab::getNetChanged));
    }
}
