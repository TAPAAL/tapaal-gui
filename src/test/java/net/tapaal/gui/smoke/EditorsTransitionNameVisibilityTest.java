package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.List;

import static net.tapaal.gui.smoke.GuiHarness.button;
import static net.tapaal.gui.smoke.GuiHarness.transition;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EditorsTransitionNameVisibilityTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void hidesTheTransitionLabelAndKeepsItsName() throws Exception {
        var tab = gui.open("editors-dialogs/flow.tapn");
        assertTrue(gui.onEdt(() -> transition(tab, "T0").getNameLabel().isShowing()));
        gui.rightClickObject(tab, "T0");
        gui.clickPopupItem("Edit Transition");
        var dialog = gui.awaitDialog("Edit Transition");
        gui.clickButton(dialog, "Show transition name");
        gui.clickButton(dialog, "OK");
        gui.awaitClosed(dialog);

        assertFalse(gui.onEdt(() -> transition(tab, "T0").getNameLabel().isVisible()));
        assertEquals(List.of("T0"), gui.transitionNames(tab));

        gui.rightClickObject(tab, "T0");
        gui.clickPopupItem("Edit Transition");
        var reopened = gui.awaitDialog("Edit Transition");
        assertFalse(gui.onEdt(() -> button(reopened, "Show transition name").isSelected()));
        gui.clickButton(reopened, "Cancel");
        gui.awaitClosed(reopened);
    }
}
