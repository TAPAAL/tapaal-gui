package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilesSaveSelectedTabTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void saveShortcutWritesOnlyTheSelectedDirtyFile() throws Exception {
        String firstFixture = "files-marking/counter.tapn";
        String secondFixture = "files-marking/reserve.tapn";
        var tabs = gui.openAll(firstFixture, secondFixture);
        var first = tabs.get(0);
        var second = tabs.get(1);
        gui.selectTool(second, "Add token");
        gui.clickObject(second, "Reserve");
        gui.selectTab(first);
        gui.selectTool(first, "Add token");
        gui.clickObject(first, "Count");
        assertTrue(gui.onEdt(first::getNetChanged));
        assertTrue(gui.onEdt(second::getNetChanged));

        gui.keys("ctrl S");

        assertFalse(gui.onEdt(first::getNetChanged), "The selected file should be clean after saving");
        assertTrue(gui.onEdt(second::getNetChanged), "Saving must leave the other file's edit pending");
        assertEquals(Map.of("Reserve", 5), gui.marking(second));
        gui.assertSavedMatches("files-marking/counter-saved.tapn");
        gui.assertFileMatches(gui.file(secondFixture), secondFixture, "The other file was never saved");
    }
}
