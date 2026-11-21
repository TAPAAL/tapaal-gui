package net.tapaal.gui.smoke;

import net.tapaal.gui.petrinet.verification.TAPNQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.swing.JTextField;

import static org.junit.jupiter.api.Assertions.*;

class ViewQueryNewTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void constructsAndSavesReachabilityQuery() throws Exception {
        var tab = gui.open("view-query-fixtures/queries.tapn");
        gui.menu("View", "Show simple workspace");
        gui.clickButton(gui.onEdt(tab::getQueryPane), "New");
        var dialog = gui.awaitDialog("Edit Query");
        assertFalse(gui.onEdt(() -> GuiHarness.button(dialog, "Save").isEnabled()),
            "An empty formula cannot be saved");

        gui.typeInto(gui.onEdt(() -> GuiHarness.labelled(dialog, "Query name: ", JTextField.class)),
            "Find empty start");
        gui.clickButton(dialog, "EF");
        gui.clickButton(dialog, "Add predicate to the query");
        gui.clickButton(dialog, "Save");
        gui.awaitClosed(dialog);

        gui.onEdt(() -> {
            var list = tab.getQueryPane().getJList();
            assertEquals(3, list.getModel().getSize());
            var added = (TAPNQuery) list.getSelectedValue();
            assertEquals("Find empty start", added.getName());
            assertEquals("EF Smoke.Start = 0", added.getQuery());
            assertTrue(tab.getNetChanged());
            return null;
        });
    }
}
