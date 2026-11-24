package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static net.tapaal.gui.smoke.GuiHarness.menuItem;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilesCloseLastCleanTabTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void closingTheLastCleanTabDisablesDocumentActions() throws Exception {
        String fixture = "files-marking/counter.tapn";
        var tab = gui.open(fixture);
        assertFalse(gui.onEdt(tab::getNetChanged));

        gui.keys("ctrl W");

        assertTrue(gui.tabs().isEmpty());
        assertNull(gui.currentTab());
        assertFalse(gui.onEdt(() -> menuItem("File", "Close").isEnabled()));
        assertFalse(gui.onEdt(() -> menuItem("File", "Save").isEnabled()));
        gui.assertSavedMatches(fixture, "Closing a clean tab should leave its file intact");

        var reopened = gui.openFiles(gui.file()).get(0);
        assertSame(reopened, gui.currentTab());
        assertTrue(gui.onEdt(() -> menuItem("File", "Close").isEnabled()));
        assertTrue(gui.onEdt(() -> menuItem("File", "Save").isEnabled()));
    }
}
