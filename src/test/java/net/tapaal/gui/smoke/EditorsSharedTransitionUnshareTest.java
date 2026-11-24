package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.swing.JTextField;
import java.util.List;

import static net.tapaal.gui.smoke.GuiHarness.button;
import static net.tapaal.gui.smoke.GuiHarness.labelled;
import static net.tapaal.gui.smoke.GuiHarness.transition;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EditorsSharedTransitionUnshareTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void unsharesOneOccurrenceAndPreservesItsArcAndTheOtherOccurrence() throws Exception {
        var tab = gui.open("editors-dialogs/shared-components.tapn");
        var explorer = tab.getTemplateExplorer();
        gui.rightClickObject(tab, "T0");
        gui.clickPopupItem("Edit Transition");
        var dialog = gui.awaitDialog("Edit Transition");
        assertTrue(gui.onEdt(() -> button(dialog, "Shared").isSelected()));
        gui.clickButton(dialog, "Shared");
        gui.typeInto(gui.onEdt(() -> labelled(dialog, "Name:", JTextField.class)), "Dispatch");
        gui.clickButton(dialog, "OK");
        gui.awaitClosed(dialog);

        assertEquals(List.of("Dispatch"), gui.transitionNames(tab));
        assertEquals(List.of("Dispatch -> P1"), gui.arcs(tab));
        assertFalse(gui.onEdt(() -> transition(tab, "Dispatch").underlyingTransition().isShared()));

        gui.click(gui.centerOfRow(gui.onEdt(explorer::getJList), 1));
        assertEquals("Consumer", gui.onEdt(() -> tab.currentTemplate().model().name()));
        assertEquals(List.of("T0"), gui.transitionNames(tab));
        assertTrue(gui.onEdt(() -> transition(tab, "T0").underlyingTransition().isShared()));
    }
}
