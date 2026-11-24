package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.swing.JSpinner;
import java.util.List;
import java.util.Map;

import static net.tapaal.gui.smoke.GuiHarness.labelled;
import static org.junit.jupiter.api.Assertions.assertEquals;

class EditorsComponentCopyTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void copiesMarkingAndArcsIntoAnIndependentlyEditableComponent() throws Exception {
        var tab = gui.open("editors-dialogs/flow.tapn");
        var explorer = tab.getTemplateExplorer();
        gui.clickButton(explorer, "Copy");
        gui.click(gui.centerOfRow(gui.onEdt(explorer::getJList), 1));

        assertEquals("FlowCopy", gui.onEdt(() -> tab.currentTemplate().model().name()));
        assertEquals(Map.of("P0", 1, "P1", 0), gui.marking(tab));
        assertEquals(List.of("P0 -> T0", "T0 -> P1"), gui.arcs(tab));

        gui.rightClickObject(tab, "P0");
        gui.clickPopupItem("Edit Place");
        var dialog = gui.awaitDialog("Edit Place");
        gui.typeInto(gui.onEdt(() -> labelled(dialog, "Marking:", JSpinner.class)), "3");
        gui.clickButton(dialog, "OK");
        gui.awaitClosed(dialog);
        assertEquals(Map.of("P0", 3, "P1", 0), gui.marking(tab));

        gui.click(gui.centerOfRow(gui.onEdt(explorer::getJList), 0));
        assertEquals("Flow", gui.onEdt(() -> tab.currentTemplate().model().name()));
        assertEquals(Map.of("P0", 1, "P1", 0), gui.marking(tab));
    }
}
