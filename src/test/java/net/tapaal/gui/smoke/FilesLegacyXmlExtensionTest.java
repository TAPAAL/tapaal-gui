package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.swing.JTextArea;
import java.util.Map;

import static net.tapaal.gui.smoke.GuiHarness.find;
import static net.tapaal.gui.smoke.GuiHarness.tabbedPane;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilesLegacyXmlExtensionTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void openingXmlAdvertisesTheNewExtensionAndKeepsTheSourceFile() throws Exception {
        String fixture = "files-legacy-xml/old-buffer.xml";
        gui.startEmpty();
        var source = gui.workingCopy(fixture);
        var tab = gui.openFiles(source).get(0);
        var notice = gui.awaitDialog("FILE CHANGED");
        String message = gui.onEdt(() -> find(notice, JTextArea.class, JTextArea::isShowing).getText());
        assertTrue(message.contains("automatically renamed to end with .tapn"));
        gui.clickButton(notice, "OK");
        gui.awaitClosed(notice);

        assertEquals("old-buffer.tapn", gui.onEdt(() -> tabbedPane().getTitleAt(0)));
        assertNull(gui.onEdt(tab::getFile), "The XML source should not be the target of a future Save");
        assertEquals(Map.of("Queued", 2), gui.marking(tab));
        gui.menu("File", "Close");
        assertTrue(gui.tabs().isEmpty());
        gui.assertFileMatches(source, fixture, "Opening and closing a legacy XML file should preserve it");
    }
}
