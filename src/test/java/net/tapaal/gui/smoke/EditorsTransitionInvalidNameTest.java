package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.swing.JTextField;
import java.util.List;

import static net.tapaal.gui.smoke.GuiHarness.dialogText;
import static net.tapaal.gui.smoke.GuiHarness.labelled;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EditorsTransitionInvalidNameTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void rejectsANameStartingWithADigit() throws Exception {
        var tab = gui.open("editors-dialogs/flow.tapn");
        gui.rightClickObject(tab, "T0");
        gui.clickPopupItem("Edit Transition");
        var dialog = gui.awaitDialog("Edit Transition");
        gui.typeInto(gui.onEdt(() -> labelled(dialog, "Name:", JTextField.class)), "9Dispatch");
        gui.clickButton(dialog, "OK");

        var error = gui.awaitDialog("Error");
        assertTrue(gui.onEdt(() -> dialogText(error).contains("Acceptable names for transitions")));
        gui.clickButton(error, "OK");
        gui.awaitClosed(error);
        assertTrue(gui.onEdt(dialog::isShowing));
        gui.clickButton(dialog, "Cancel");
        gui.awaitClosed(dialog);

        assertEquals(List.of("T0"), gui.transitionNames(tab));
    }
}
