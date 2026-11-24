package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import net.tapaal.gui.petrinet.verification.TAPNQuery;
import pipe.gui.TAPAALGUI;
import pipe.gui.petrinet.PetriNetTab;

import javax.swing.JComboBox;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import static org.junit.jupiter.api.Assertions.*;

class ViewQueryDuplicateTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void gameConversionCopiesCompatibleQueriesIndependently() throws Exception {
        var original = gui.open("view-query-fixtures/game-conversion.tapn");
        gui.menu("View", "Show simple workspace");
        gui.click(gui.centerOf(() -> GuiHarness.labelled(TAPAALGUI.getAppGui(), "   Game: ", JComboBox.class)));
        gui.keys("END");
        var notice = gui.awaitDialog("Information");
        var message = gui.onEdt(() -> GuiHarness.find(notice, JTextArea.class, area -> true).getText());
        assertTrue(message.contains("The following queries will be removed in the conversion:"), message);
        assertTrue(message.contains("Reach Start"), message);
        gui.clickButton(notice, "OK");
        gui.awaitClosed(notice);

        var copy = gui.currentTab();
        assertEquals(2, gui.tabs().size());
        assertNotNull(copy);
        assertNotSame(original, copy);
        assertTrue(gui.onEdt(() -> copy.getLens().isGame()));
        assertFalse(gui.onEdt(() -> original.getLens().isGame()));
        assertEquals(1, gui.onEdt(() -> copy.getQueryPane().getJList().getModel().getSize()));
        assertEquals("Keep Start marked", gui.onEdt(() -> firstQuery(copy).getName()));
        assertEquals("AG Smoke.Start = 1", gui.onEdt(() -> firstQuery(copy).getQuery()));
        assertEquals(5, gui.onEdt(() -> firstQuery(copy).getCapacity()));

        var pane = gui.onEdt(copy::getQueryPane);
        gui.click(gui.centerOfRow(gui.onEdt(pane::getJList), 0));
        gui.clickButton(pane, "Edit");
        var dialog = gui.awaitDialog("Edit Query");
        gui.typeInto(gui.onEdt(() -> GuiHarness.labelled(dialog, "Query name: ", JTextField.class)), "Game safety");
        gui.clickButton(dialog, "Save");
        gui.awaitClosed(dialog);
        assertEquals("Game safety", gui.onEdt(() -> firstQuery(copy).getName()));

        gui.selectTab(original);
        assertEquals(2, gui.onEdt(() -> original.getQueryPane().getJList().getModel().getSize()));
        assertEquals("Keep Start marked", gui.onEdt(() -> firstQuery(original).getName()));
        assertFalse(gui.onEdt(original::getNetChanged));
    }

    private TAPNQuery firstQuery(PetriNetTab tab) {
        return (TAPNQuery) tab.getQueryPane().getJList().getModel().getElementAt(0);
    }
}
