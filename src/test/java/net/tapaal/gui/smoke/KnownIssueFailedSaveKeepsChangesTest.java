package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.extension.RegisterExtension;
import pipe.gui.petrinet.PetriNetTab;

import java.awt.Point;
import java.nio.file.Files;

import static net.tapaal.gui.smoke.GuiHarness.dialogText;
import static net.tapaal.gui.smoke.GuiHarness.menuItem;
import static net.tapaal.gui.smoke.GuiHarness.place;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Known issue: File &gt; Save reports a write failure but then marks the net as saved.
 *
 * <p>{@code PetriNetTab.saveNet} calls {@code writeNetToFile}, which catches the failure and shows "File Output
 * Error". {@code saveNet} then clears the dirty flag and the undo history as if the file had been written. The
 * test makes the opened file read-only, which is an ordinary file-system failure and needs no production hook.
 * Expected after the error: the net is still marked changed and Undo still restores the edit.
 */
@EnabledIfEnvironmentVariable(named = "TAPAAL_GUI_KNOWN_ISSUES", matches = "true",
    disabledReason = "Known-issue reproducer; see tools/validation/gui-known-issues.md")
class KnownIssueFailedSaveKeepsChangesTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void failedSaveLeavesTheNetChangedAndUndoable() throws Exception {
        PetriNetTab tab = gui.open("known-issue-workbench/two-places.tapn");
        var file = gui.file();
        byte[] original = Files.readAllBytes(file);

        gui.dragPlace(tab, "P0", 90, 60);
        assertEquals(new Point(150, 120), gui.onEdt(() -> position(tab, "P0")), "The drag should move P0");
        assertTrue(gui.onEdt(tab::getNetChanged), "The drag should mark the net as changed");
        assertTrue(gui.onEdt(() -> menuItem("Edit", "Undo").isEnabled()), "The drag should be undoable");

        assertTrue(file.toFile().setWritable(false), "Could not make the working copy read-only");
        assertFalse(Files.isWritable(file), "The working copy is still writable; run the test as a non-root user");
        try {
            gui.menu("File", "Save");
            var error = gui.awaitDialog("File Output Error");
            String message = gui.onEdt(() -> dialogText(error));
            System.out.println("Save failure dialog: " + message);
            gui.clickButton(error, "OK");
            gui.awaitClosed(error);
        } finally {
            file.toFile().setWritable(true);
        }

        assertArrayEquals(original, Files.readAllBytes(file), "The failed save must not have written the file");
        boolean dirty = gui.onEdt(tab::getNetChanged);
        boolean undoEnabled = gui.onEdt(() -> menuItem("Edit", "Undo").isEnabled());
        System.out.println("After failed save: netChanged=" + dirty + ", Edit>Undo enabled=" + undoEnabled);
        assertAll("After Save reported \"File Output Error\" the unsaved edit must not be treated as saved",
            () -> assertTrue(dirty, "expected the net to stay marked changed (netChanged=true), actual netChanged="
                + dirty),
            () -> assertTrue(undoEnabled, "expected Edit > Undo to stay enabled for the unsaved drag, actual enabled="
                + undoEnabled));
    }

    private static Point position(PetriNetTab tab, String name) {
        var place = place(tab, name);
        return new Point(place.getOriginalX(), place.getOriginalY());
    }
}
