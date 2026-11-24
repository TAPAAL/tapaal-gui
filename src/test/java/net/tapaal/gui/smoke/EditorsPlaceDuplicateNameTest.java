package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.swing.JTextField;
import java.util.List;
import java.util.Map;

import static net.tapaal.gui.smoke.GuiHarness.dialogText;
import static net.tapaal.gui.smoke.GuiHarness.labelled;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EditorsPlaceDuplicateNameTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void rejectsANameAlreadyUsedByATransition() throws Exception {
        var tab = gui.open("editors-dialogs/flow.tapn");
        gui.rightClickObject(tab, "P0");
        gui.clickPopupItem("Edit Place");
        var dialog = gui.awaitDialog("Edit Place");
        gui.typeInto(gui.onEdt(() -> labelled(dialog, "Name:", JTextField.class)), "T0");
        gui.clickButton(dialog, "OK");

        var error = gui.awaitDialog("Error");
        assertTrue(gui.onEdt(() -> dialogText(error).contains("already used by another place or transition")));
        gui.clickButton(error, "OK");
        gui.awaitClosed(error);
        assertEquals(Map.of("P0", 1, "P1", 0), gui.marking(tab));

        gui.typeInto(gui.onEdt(() -> labelled(dialog, "Name:", JTextField.class)), "Source");
        gui.clickButton(dialog, "OK");
        gui.awaitClosed(dialog);
        assertEquals(Map.of("Source", 1, "P1", 0), gui.marking(tab));
        assertEquals(List.of("Source -> T0", "T0 -> P1"), gui.arcs(tab));
    }
}
