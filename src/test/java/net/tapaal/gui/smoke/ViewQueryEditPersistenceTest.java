package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import net.tapaal.gui.petrinet.verification.TAPNQuery;

import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import static org.junit.jupiter.api.Assertions.*;

class ViewQueryEditPersistenceTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void editedNameFormulaAndTokenBoundSurviveReopen() throws Exception {
        var tab = gui.open("view-query-fixtures/queries.tapn");
        gui.menu("View", "Show simple workspace");
        var pane = gui.onEdt(tab::getQueryPane);
        gui.click(gui.centerOfRow(gui.onEdt(pane::getJList), 0));
        gui.clickButton(pane, "Edit");
        var dialog = gui.awaitDialog("Edit Query");
        var name = gui.onEdt(() -> GuiHarness.labelled(dialog, "Query name: ", JTextField.class));
        var tokens = gui.onEdt(() -> GuiHarness.labelled(dialog, " Number of extra tokens:  ", JSpinner.class));
        assertEquals("Reach Start", gui.onEdt(name::getText));
        assertEquals(5, gui.onEdt(tokens::getValue));

        gui.typeInto(name, "Reach two");
        gui.clickButton(dialog, "Edit query");
        var formula = gui.onEdt(() -> GuiHarness.find(dialog, JTextPane.class, JTextPane::isEditable));
        gui.typeInto(formula, "EF Smoke.Start = 2");
        assertFalse(gui.onEdt(() -> GuiHarness.button(dialog, "Save").isEnabled()),
            "Unparsed text cannot be saved");
        gui.clickButton(dialog, "Parse query");
        gui.typeInto(tokens, "12");
        gui.clickButton(dialog, "Save");
        gui.awaitClosed(dialog);
        assertTrue(gui.onEdt(tab::getNetChanged));

        gui.menu("File", "Save");
        gui.menu("File", "Close");
        var reopened = gui.openFiles(gui.file()).get(0);
        gui.onEdt(() -> {
            var model = reopened.getQueryPane().getJList().getModel();
            assertEquals(2, model.getSize());
            var edited = (TAPNQuery) model.getElementAt(0);
            assertEquals("Reach two", edited.getName());
            assertEquals("EF Smoke.Start = 2", edited.getQuery());
            assertEquals(12, edited.getCapacity());
            var untouched = (TAPNQuery) model.getElementAt(1);
            assertEquals("Avoid empty", untouched.getName());
            assertEquals("EG Smoke.Start = 1", untouched.getQuery());
            assertEquals(8, untouched.getCapacity());
            return null;
        });
        gui.assertSavedMatches("view-query-fixtures/edited-query.tapn", "Save should preserve the net and both queries");
    }
}
