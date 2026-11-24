package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.border.TitledBorder;

import static net.tapaal.gui.smoke.GuiHarness.dialogText;
import static net.tapaal.gui.smoke.GuiHarness.find;
import static net.tapaal.gui.smoke.GuiHarness.place;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EditorsPlaceInvariantValidationTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void rejectsABoundBelowAnExistingTokenAge() throws Exception {
        var tab = gui.open("editors-dialogs/aged-token.tapn");
        gui.rightClickObject(tab, "P0");
        gui.clickPopupItem("Edit Place");
        var dialog = gui.awaitDialog("Edit Place");
        gui.clickButton(dialog, "∞");
        var bound = gui.onEdt(() -> {
            var panel = find(dialog, JPanel.class, candidate ->
                candidate.getBorder() instanceof TitledBorder border && "Age Invariant".equals(border.getTitle()));
            return find(panel, JSpinner.class, JSpinner::isEnabled);
        });
        gui.typeInto(bound, "3");
        gui.clickButton(dialog, "OK");

        var error = gui.awaitDialog("Invalid token age");
        assertTrue(gui.onEdt(() -> dialogText(error).contains("Age 4 violates the invariant")));
        gui.clickButton(error, "OK");
        gui.awaitClosed(error);
        gui.clickButton(dialog, "Cancel");
        gui.awaitClosed(dialog);

        assertEquals("< inf", gui.onEdt(() -> place(tab, "P0").underlyingPlace().invariant().toString()));
        assertEquals(4, gui.onEdt(() -> place(tab, "P0").underlyingPlace().tokens().get(0).age().intValueExact()));
    }
}
