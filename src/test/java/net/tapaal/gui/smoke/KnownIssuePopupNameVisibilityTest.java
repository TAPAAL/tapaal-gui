package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.nio.file.Files;

import static net.tapaal.gui.smoke.GuiHarness.menuItem;
import static net.tapaal.gui.smoke.GuiHarness.place;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Known issue: Hide Place Name in a place's popup menu changes the saved document without marking the net as
 * changed or adding an undo step.
 *
 * <p>The popup item runs {@code ShowHideInfoAction}, which only calls {@code toggleAttributesVisible()}. The same
 * change made through Edit Place records a {@code ChangeAllNamesVisibilityCommand}. The {@code displayName}
 * attribute is written to the file, so closing the tab afterwards discards the change without the unsaved-changes
 * prompt, and Undo cannot revert it.
 */
@EnabledIfEnvironmentVariable(named = "TAPAAL_GUI_KNOWN_ISSUES", matches = "true",
    disabledReason = "Known-issue reproducer; see tools/validation/gui-known-issues.md")
class KnownIssuePopupNameVisibilityTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void hidingAPlaceNameFromThePopupIsAnUnsavedUndoableEdit() throws Exception {
        var tab = gui.open("known-issue-workbench/two-places.tapn");
        assertFalse(gui.onEdt(tab::getNetChanged), "A freshly opened net should be unchanged");
        assertFalse(gui.onEdt(() -> menuItem("Edit", "Undo").isEnabled()), "A freshly opened net has nothing to undo");
        assertTrue(gui.onEdt(() -> place(tab, "P0").getAttributesVisible()), "The fixture shows P0's name");

        gui.rightClickObject(tab, "P0");
        gui.clickPopupItem("Hide Place Name");
        assertFalse(gui.onEdt(() -> place(tab, "P0").getAttributesVisible()), "Hide Place Name should hide the name");

        String snapshot = Files.readString(gui.snapshot(tab));
        boolean persisted = snapshot.matches("(?s).*<place[^>]*displayName=\"false\"[^>]*id=\"P0\".*")
            || snapshot.matches("(?s).*<place[^>]*id=\"P0\"[^>]*displayName=\"false\".*");
        assertTrue(persisted, "Precondition: the written document should store P0 with displayName=\"false\"");

        boolean dirty = gui.onEdt(tab::getNetChanged);
        boolean undoEnabled = gui.onEdt(() -> menuItem("Edit", "Undo").isEnabled());
        System.out.println("After popup Hide Place Name: document stores displayName=false, netChanged=" + dirty
            + ", Edit>Undo enabled=" + undoEnabled);
        assertAll("Hiding P0's name from the popup changes the saved document, so it is an unsaved, undoable edit",
            () -> assertTrue(dirty, "expected netChanged=true, actual netChanged=" + dirty),
            () -> assertTrue(undoEnabled, "expected Edit > Undo enabled, actual enabled=" + undoEnabled));
    }
}
