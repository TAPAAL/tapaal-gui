package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilesCloseConfirmCancelTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void cancelKeepsTheDirtyDocumentEditableAndLeavesItsFileAlone() throws Exception {
        String fixture = "files-marking/counter.tapn";
        var tab = gui.open(fixture);
        gui.selectTool(tab, "Add token");
        gui.clickObject(tab, "Count");

        gui.menu("File", "Close");
        var prompt = gui.awaitDialog("Confirm Save Current File");
        gui.clickButton(prompt, "Cancel");
        gui.awaitClosed(prompt);

        assertEquals(List.of(tab), gui.tabs());
        assertSame(tab, gui.currentTab());
        assertTrue(gui.onEdt(tab::getNetChanged));
        assertEquals(Map.of("Count", 3), gui.marking(tab));
        gui.assertSavedMatches(fixture, "Cancel should leave the file alone");

        gui.selectTool(tab, "Add token");
        gui.clickObject(tab, "Count");
        assertEquals(Map.of("Count", 4), gui.marking(tab), "The retained tab should still accept edits");
    }
}
