package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilesReopenComponentsTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void reopeningASavedFileRestoresTheEditInItsSecondComponent() throws Exception {
        var tab = gui.open("files-components/buffer.tapn");
        assertEquals(Map.of("Ready", 1), gui.marking(tab));
        var components = gui.onEdt(() -> tab.getTemplateExplorer().getJList());
        gui.click(gui.centerOfRow(components, 1));
        assertEquals("Consumer", gui.onEdt(() -> tab.currentTemplate().model().name()));
        gui.selectTool(tab, "Add token");
        gui.clickObject(tab, "Waiting");
        assertEquals(Map.of("Waiting", 2), gui.marking(tab));

        gui.menu("File", "Save");
        gui.assertSavedMatches("files-components/buffer-saved.tapn");
        gui.menu("File", "Close");
        assertTrue(gui.tabs().isEmpty());

        var reopened = gui.openFiles(gui.file()).get(0);
        assertNotSame(tab, reopened);
        assertFalse(gui.onEdt(reopened::getNetChanged));
        assertEquals("Producer", gui.onEdt(() -> reopened.currentTemplate().model().name()));
        assertEquals(Map.of("Ready", 1), gui.marking(reopened));
        var reopenedComponents = gui.onEdt(() -> reopened.getTemplateExplorer().getJList());
        gui.click(gui.centerOfRow(reopenedComponents, 1));
        assertEquals("Consumer", gui.onEdt(() -> reopened.currentTemplate().model().name()));
        assertEquals(Map.of("Waiting", 2), gui.marking(reopened));
    }
}
