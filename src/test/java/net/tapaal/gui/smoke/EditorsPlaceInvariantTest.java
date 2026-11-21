package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.border.TitledBorder;

import static net.tapaal.gui.smoke.GuiHarness.find;
import static net.tapaal.gui.smoke.GuiHarness.place;
import static org.junit.jupiter.api.Assertions.assertEquals;

class EditorsPlaceInvariantTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void commitsAFiniteBoundAndShowsItOnTheCanvas() throws Exception {
        var tab = gui.open("editors-dialogs/flow.tapn");
        gui.rightClickObject(tab, "P0");
        gui.clickPopupItem("Edit Place");
        var dialog = gui.awaitDialog("Edit Place");
        gui.clickButton(dialog, "∞");
        var bound = gui.onEdt(() -> {
            var panel = find(dialog, JPanel.class, candidate ->
                candidate.getBorder() instanceof TitledBorder border && "Age Invariant".equals(border.getTitle()));
            return find(panel, JSpinner.class, JSpinner::isEnabled);
        });
        gui.typeInto(bound, "6");
        gui.clickButton(dialog, "OK");
        gui.awaitClosed(dialog);

        assertEquals("<= 6", gui.onEdt(() -> place(tab, "P0").underlyingPlace().invariant().toString()));
        assertEquals("Inv: <= 6", gui.onEdt(() -> place(tab, "P0").getNameLabel().getText().trim()));
    }
}
