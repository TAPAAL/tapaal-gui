package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import net.tapaal.gui.petrinet.verification.TAPNQuery;

import javax.swing.JTextField;

import static org.junit.jupiter.api.Assertions.*;

class ViewQueryCancelEditTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void cancelKeepsOriginalQueryAndCleanDocument() throws Exception {
        var tab = gui.open("view-query-fixtures/queries.tapn");
        gui.menu("View", "Show simple workspace");
        var pane = gui.onEdt(tab::getQueryPane);
        gui.click(gui.centerOfRow(gui.onEdt(pane::getJList), 1));
        gui.clickButton(pane, "Edit");
        var dialog = gui.awaitDialog("Edit Query");
        var name = gui.onEdt(() -> GuiHarness.labelled(dialog, "Query name: ", JTextField.class));
        assertEquals("Avoid empty", gui.onEdt(name::getText));
        gui.typeInto(name, "Discard this rename");
        gui.clickButton(dialog, "Cancel");
        gui.awaitClosed(dialog);

        gui.onEdt(() -> {
            var list = pane.getJList();
            assertEquals(2, list.getModel().getSize());
            var query = (TAPNQuery) list.getSelectedValue();
            assertEquals("Avoid empty", query.getName());
            assertEquals("EG Smoke.Start = 1", query.getQuery());
            assertEquals(8, query.getCapacity());
            assertFalse(tab.getNetChanged());
            return null;
        });
    }
}
