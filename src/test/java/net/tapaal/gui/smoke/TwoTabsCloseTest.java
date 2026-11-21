package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Starts with two files, edits the first tab after clicking its title, closes it without saving through the
 * unsaved-changes prompt and reopens it.
 *
 * <p>Recipe for several tabs: start with {@code openAll}, switch with {@code selectTab} and check that the
 * other tab is unchanged.
 */
class TwoTabsCloseTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void editingOneTabLeavesTheOtherAlone() throws Exception {
        var tabs = gui.openAll("one-place.tapn", "move-place/before.tapn");
        var first = tabs.get(0);
        var second = tabs.get(1);
        assertSame(second, gui.currentTab(), "The last file opened should be selected");

        gui.selectTab(first);
        gui.selectTool(first, "Add token");
        gui.clickObject(first, "Start");
        assertEquals(Map.of("Start", 2), gui.marking(first));
        assertEquals(Map.of("P0", 1, "P1", 0), gui.marking(second), "The other tab should be unchanged");
        assertFalse(gui.onEdt(second::getNetChanged));

        gui.menu("File", "Close");
        var prompt = gui.awaitDialog("Confirm Save Current File");
        assertEquals("The net has been modified. Save the current net?", gui.onEdt(() -> GuiHarness.dialogText(prompt)));
        gui.clickButton(prompt, "No");
        gui.awaitClosed(prompt);
        assertEquals(List.of(second), gui.tabs(), "Closing without saving should remove only the edited tab");
        gui.assertFileMatches(gui.file("one-place.tapn"), "one-place.tapn", "Closing without saving should leave the file alone");

        var reopened = gui.openFiles(gui.file("one-place.tapn")).get(0);
        assertEquals(Map.of("Start", 1), gui.marking(reopened));
        assertSame(reopened, gui.currentTab());
    }
}
