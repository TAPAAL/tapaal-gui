package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.swing.JSpinner;
import java.util.Map;

import static net.tapaal.gui.smoke.GuiHarness.button;
import static net.tapaal.gui.smoke.GuiHarness.labelled;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EditorsSharedPlaceMarkingTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void showsTheEditedSharedMarkingInBothComponents() throws Exception {
        var tab = gui.open("editors-dialogs/shared-components.tapn");
        var explorer = tab.getTemplateExplorer();
        assertEquals(Map.of("P0", 1, "P1", 0), gui.marking(tab));
        gui.rightClickObject(tab, "P0");
        gui.clickPopupItem("Edit Place");
        var dialog = gui.awaitDialog("Edit Place");
        assertTrue(gui.onEdt(() -> button(dialog, "Shared").isSelected()));
        gui.typeInto(gui.onEdt(() -> labelled(dialog, "Marking:", JSpinner.class)), "4");
        gui.clickButton(dialog, "OK");
        gui.awaitClosed(dialog);
        assertEquals(Map.of("P0", 4, "P1", 0), gui.marking(tab));

        gui.click(gui.centerOfRow(gui.onEdt(explorer::getJList), 1));
        assertEquals("Consumer", gui.onEdt(() -> tab.currentTemplate().model().name()));
        assertEquals(Map.of("P0", 4), gui.marking(tab));
    }
}
