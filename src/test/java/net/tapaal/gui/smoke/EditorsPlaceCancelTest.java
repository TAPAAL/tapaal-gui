package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.swing.JSpinner;
import javax.swing.JTextField;
import java.util.Map;

import static net.tapaal.gui.smoke.GuiHarness.labelled;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class EditorsPlaceCancelTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void cancelDiscardsNameAndMarkingEdits() throws Exception {
        var tab = gui.open("editors-dialogs/flow.tapn");
        gui.rightClickObject(tab, "P0");
        gui.clickPopupItem("Edit Place");
        var dialog = gui.awaitDialog("Edit Place");
        gui.typeInto(gui.onEdt(() -> labelled(dialog, "Name:", JTextField.class)), "Draft");
        gui.typeInto(gui.onEdt(() -> labelled(dialog, "Marking:", JSpinner.class)), "7");
        gui.clickButton(dialog, "Cancel");
        gui.awaitClosed(dialog);

        assertEquals(Map.of("P0", 1, "P1", 0), gui.marking(tab));
        assertFalse(gui.onEdt(tab::getNetChanged));
    }
}
