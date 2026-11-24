package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.swing.JTextField;
import java.util.Map;

import static net.tapaal.gui.smoke.GuiHarness.labelled;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Adds a component from the components panel, draws in it, switches back by clicking the first row and
 * compares a snapshot of the whole net, which holds both components.
 *
 * <p>Recipe for panels inside a tab: scope button lookups to the panel, such as {@code tab.getTemplateExplorer()}.
 */
class ComponentsPanelTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void newComponentIsEditedSeparately() throws Exception {
        var tab = gui.open("move-place/before.tapn");
        var explorer = tab.getTemplateExplorer();

        gui.clickButton(explorer, "New");
        var dialog = gui.awaitDialog("Enter Component Name");
        gui.typeInto(gui.onEdt(() -> labelled(dialog, "Name of component: ", JTextField.class)), "Second");
        gui.clickButton(dialog, "OK");
        gui.awaitClosed(dialog);
        assertEquals("Second", gui.onEdt(() -> tab.currentTemplate().model().name()), "The new component should be open");
        assertEquals(Map.of(), gui.marking(tab));

        gui.selectTool(tab, "Place");
        gui.clickCanvas(tab, 105, 105);
        assertEquals(1, gui.marking(tab).size(), "The place should be added to the new component");

        gui.click(gui.centerOfRow(gui.onEdt(explorer::getJList), 0));
        assertEquals("MovePlace", gui.onEdt(() -> tab.currentTemplate().model().name()));
        assertEquals(Map.of("P0", 1, "P1", 0), gui.marking(tab), "The first component should be unchanged");

        gui.assertSnapshotMatches(tab, "components/expected.tapn", "The net should hold both components");
    }
}
