package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static net.tapaal.gui.smoke.GuiHarness.button;
import static net.tapaal.gui.smoke.GuiHarness.dialogText;
import static net.tapaal.gui.smoke.GuiHarness.transition;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EditorsTransitionUrgencyValidationTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void rejectsUrgencyWithABoundedInputArc() throws Exception {
        var tab = gui.open("editors-dialogs/flow.tapn");
        gui.rightClickObject(tab, "T0");
        gui.clickPopupItem("Edit Transition");
        var dialog = gui.awaitDialog("Edit Transition");
        gui.clickButton(dialog, "Urgent");

        var error = gui.awaitDialog("Error");
        assertTrue(gui.onEdt(() -> dialogText(error).contains("Incoming arcs to urgent transitions")));
        gui.clickButton(error, "OK");
        gui.awaitClosed(error);
        assertFalse(gui.onEdt(() -> button(dialog, "Urgent").isSelected()));
        gui.clickButton(dialog, "OK");
        gui.awaitClosed(dialog);

        assertFalse(gui.onEdt(() -> transition(tab, "T0").underlyingTransition().isUrgent()));
    }
}
