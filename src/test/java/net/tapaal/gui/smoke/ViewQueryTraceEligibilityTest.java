package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.swing.AbstractButton;
import javax.swing.JDialog;

import static org.junit.jupiter.api.Assertions.*;

class ViewQueryTraceEligibilityTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void fastestTraceIsEnabledForEFAndDisabledForEG() throws Exception {
        var tab = gui.open("view-query-fixtures/queries.tapn");
        gui.menu("View", "Show advanced workspace");
        var pane = gui.onEdt(tab::getQueryPane);
        var list = gui.onEdt(pane::getJList);

        gui.click(gui.centerOfRow(list, 0));
        gui.clickButton(pane, "Edit");
        var reachability = gui.awaitDialog("Edit Query");
        assertTrue(gui.onEdt(() -> GuiHarness.button(reachability, "Fastest trace").isEnabled()));
        gui.clickButton(reachability, "Cancel");
        gui.awaitClosed(reachability);

        gui.click(gui.centerOfRow(list, 1));
        gui.clickButton(pane, "Edit");
        var invariance = gui.awaitDialog("Edit Query");
        assertFalse(gui.onEdt(() -> GuiHarness.button(invariance, "Fastest trace").isEnabled()));
        assertTrue(gui.onEdt(() -> someTrace(invariance).isEnabled()),
            "An EG query can still request a non-fastest trace");
        gui.clickButton(invariance, "Cancel");
        gui.awaitClosed(invariance);
    }

    private AbstractButton someTrace(JDialog dialog) {
        return GuiHarness.find(dialog, AbstractButton.class,
            button -> button.getText() != null && "Some trace".equals(button.getText().trim()));
    }
}
