package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FilesTabUndoHistoryTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void undoAndRedoFollowTheSelectedTabsOwnEditHistory() throws Exception {
        var tabs = gui.openAll("files-marking/counter.tapn", "files-marking/reserve.tapn");
        var first = tabs.get(0);
        var second = tabs.get(1);
        gui.selectTool(second, "Add token");
        gui.clickObject(second, "Reserve");
        gui.selectTab(first);
        gui.selectTool(first, "Add token");
        gui.clickObject(first, "Count");
        gui.clickObject(first, "Count");

        gui.menu("Edit", "Undo");
        assertEquals(Map.of("Count", 3), gui.marking(first));
        assertEquals(Map.of("Reserve", 5), gui.marking(second));

        gui.selectTab(second);
        gui.menu("Edit", "Undo");
        assertEquals(Map.of("Reserve", 4), gui.marking(second));
        assertEquals(Map.of("Count", 3), gui.marking(first));

        gui.selectTab(first);
        gui.menu("Edit", "Redo");
        assertEquals(Map.of("Count", 4), gui.marking(first));
        assertEquals(Map.of("Reserve", 4), gui.marking(second));
    }
}
