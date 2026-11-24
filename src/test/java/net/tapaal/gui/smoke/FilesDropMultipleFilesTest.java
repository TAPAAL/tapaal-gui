package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilesDropMultipleFilesTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void droppingFilesAppendsThemInOrderAndPreservesAnOpenEdit() throws Exception {
        var existing = gui.open("files-marking/counter.tapn");
        gui.selectTool(existing, "Add token");
        gui.clickObject(existing, "Count");
        var firstPath = gui.workingCopy("files-marking/reserve.tapn");
        var secondPath = gui.workingCopy("one-place.tapn");

        var added = gui.openFiles(firstPath, secondPath);

        assertEquals(List.of(existing, added.get(0), added.get(1)), gui.tabs());
        assertEquals(firstPath, gui.onEdt(() -> added.get(0).getFile().toPath()));
        assertEquals(secondPath, gui.onEdt(() -> added.get(1).getFile().toPath()));
        assertEquals(Map.of("Reserve", 4), gui.marking(added.get(0)));
        assertEquals(Map.of("Start", 1), gui.marking(added.get(1)));
        assertSame(added.get(1), gui.currentTab(), "The last dropped file should receive focus");
        gui.selectTab(existing);
        assertTrue(gui.onEdt(existing::getNetChanged));
        assertEquals(Map.of("Count", 3), gui.marking(existing));
    }
}
