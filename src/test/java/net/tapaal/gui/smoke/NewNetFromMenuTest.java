package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.swing.JTextField;
import java.util.Map;

import static net.tapaal.gui.smoke.GuiHarness.labelled;
import static net.tapaal.gui.smoke.GuiHarness.menuItem;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Starts without a file, creates a net through File &gt; New and draws a place in it.
 *
 * <p>Recipe for a net built from scratch. Saving it would open the native Save As chooser, which the harness
 * does not drive; compare a snapshot instead.
 */
class NewNetFromMenuTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void newNetDialogOpensAnEditableTab() throws Exception {
        gui.startEmpty();
        assertTrue(gui.tabs().isEmpty());
        assertFalse(gui.onEdt(() -> menuItem("File", "Save").isEnabled()), "Save needs an open net");

        gui.menu("File", "New");
        var dialog = gui.awaitDialog("New Net");
        gui.typeInto(gui.onEdt(() -> labelled(dialog, "Name:", JTextField.class)), "Fresh");
        gui.keys("ENTER");
        gui.awaitClosed(dialog);

        var tab = gui.currentTab();
        assertNotNull(tab, "OK should open the new net");
        assertEquals("Fresh.tapn", gui.onEdt(tab::getTabTitle));
        assertTrue(gui.onEdt(() -> menuItem("File", "Save").isEnabled()), "Save should be enabled with a net open");

        gui.keys("P");
        assertTrue(gui.onEdt(() -> GuiHarness.tool(tab, "Place").isSelected()), "The P key should choose the Place tool");
        gui.clickCanvas(tab, 105, 105);
        assertEquals(Map.of("P0", 0), gui.marking(tab));
    }
}
