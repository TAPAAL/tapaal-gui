package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.swing.JSpinner;
import javax.swing.JTextField;
import java.util.List;
import java.util.Map;

import static net.tapaal.gui.smoke.GuiHarness.labelled;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Edits a place through the dialog that a double click opens, then undoes and redoes the edit with the
 * keyboard and compares a snapshot of the net with the expected document.
 *
 * <p>Recipe for a dialog: open it with real input, find fields by their label, type, and press the button.
 */
class PlaceEditorDialogTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void doubleClickEditorRenamesAndMarksThePlace() throws Exception {
        var tab = gui.open("move-place/before.tapn");
        var before = Map.of("P0", 1, "P1", 0);
        assertEquals(before, gui.marking(tab));

        gui.doubleClickObject(tab, "P1");
        var dialog = gui.awaitDialog("Edit Place");
        gui.typeInto(gui.onEdt(() -> labelled(dialog, "Name:", JTextField.class)), "Done");
        gui.typeInto(gui.onEdt(() -> labelled(dialog, "Marking:", JSpinner.class)), "3");
        gui.clickButton(dialog, "OK");
        gui.awaitClosed(dialog);

        var after = Map.of("P0", 1, "Done", 3);
        assertEquals(after, gui.marking(tab));
        assertEquals(List.of("P0 -> T0", "T0 -> Done"), gui.arcs(tab), "Renaming keeps the arcs attached");
        assertTrue(gui.onEdt(tab::getNetChanged), "The edit should mark the net as changed");

        gui.keys("ctrl Z");
        assertEquals(before, gui.marking(tab), "Undo should revert the whole dialog edit");
        gui.keys("ctrl Y");
        assertEquals(after, gui.marking(tab), "Redo should reapply the whole dialog edit");

        gui.assertSnapshotMatches(tab, "place-editor/expected.tapn", "The edited net should be written with the new name and marking");
    }
}
