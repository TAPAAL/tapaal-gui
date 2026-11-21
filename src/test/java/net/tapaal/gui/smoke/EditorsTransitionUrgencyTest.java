package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static net.tapaal.gui.smoke.GuiHarness.button;
import static net.tapaal.gui.smoke.GuiHarness.transition;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EditorsTransitionUrgencyTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void commitsUrgencyWithAnUnboundedInputArc() throws Exception {
        var tab = gui.open("editors-dialogs/unbounded-input.tapn");
        gui.rightClickObject(tab, "T0");
        gui.clickPopupItem("Edit Transition");
        var dialog = gui.awaitDialog("Edit Transition");
        gui.clickButton(dialog, "Urgent");
        assertTrue(gui.onEdt(() -> button(dialog, "Urgent").isSelected()));
        gui.clickButton(dialog, "OK");
        gui.awaitClosed(dialog);

        assertTrue(gui.onEdt(() -> transition(tab, "T0").underlyingTransition().isUrgent()));
    }
}
