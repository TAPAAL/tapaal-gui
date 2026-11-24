package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilesSaveNewBaselineTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void anEditAfterToolbarSaveCanBeDiscardedBackToTheSavedVersion() throws Exception {
        var tab = gui.open("files-marking/counter.tapn");
        gui.selectTool(tab, "Add token");
        gui.clickObject(tab, "Count");
        gui.toolbar("Save");
        assertFalse(gui.onEdt(tab::getNetChanged));
        gui.assertSavedMatches("files-marking/counter-saved.tapn");

        gui.clickObject(tab, "Count");
        assertEquals(Map.of("Count", 4), gui.marking(tab));
        assertTrue(gui.onEdt(tab::getNetChanged), "An edit after saving should dirty the tab again");
        gui.menu("File", "Close");
        var prompt = gui.awaitDialog("Confirm Save Current File");
        gui.clickButton(prompt, "No");
        gui.awaitClosed(prompt);

        assertTrue(gui.tabs().isEmpty());
        gui.assertSavedMatches("files-marking/counter-saved.tapn", "Discard should retain the latest saved version");
        var reopened = gui.openFiles(gui.file()).get(0);
        assertEquals(Map.of("Count", 3), gui.marking(reopened));
    }
}
