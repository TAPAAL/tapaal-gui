package net.tapaal.gui.smoke;

import net.tapaal.TAPAAL;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.opentest4j.AssertionFailedError;

import javax.swing.SwingUtilities;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Checks the harness itself: a menu item that opens a modal dialog returns control to the test, menu paths
 * reach submenus, lookups explain what they found, exceptions that escape to the event thread fail the next
 * step, the application cannot start twice, and each JVM has its own preferences.
 */
class HarnessSelfCheckTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void harnessReportsWhatATestNeedsToKnow() throws Exception {
        gui.startEmpty();

        gui.menu("Help", "About");
        var about = gui.awaitDialog("About " + TAPAAL.getProgramName());
        assertTrue(gui.onEdt(() -> GuiHarness.dialogText(about)).contains("TAPAAL is a tool for editing"));
        gui.clickButton(about, "OK");
        gui.awaitClosed(about);

        assertEquals("PNML untimed net", gui.onEdt(() -> GuiHarness.menuItem("File", "Import", "PNML untimed net").getText()),
            "Menu paths should reach items in submenus");
        var missing = assertThrows(AssertionError.class, () -> gui.menu("File", "Frobnicate"));
        assertTrue(missing.getMessage().contains("it has [New, Open, Close"), missing.getMessage());

        SwingUtilities.invokeLater(() -> {
            throw new IllegalStateException("probe from the event thread");
        });
        var escaped = assertThrows(AssertionFailedError.class, gui::settle);
        assertTrue(escaped.getMessage().contains("probe from the event thread"), escaped.getMessage());

        var twice = assertThrows(AssertionFailedError.class, gui::startEmpty);
        assertTrue(twice.getMessage().contains("already running"), twice.getMessage());

        var preferences = Path.of(System.getProperty("java.util.prefs.userRoot"));
        assertEquals(Path.of(System.getProperty("tapaal.smoke.preferences")), preferences.getParent(),
            "Each JVM should get its own preferences directory under the task's directory");
        var node = java.util.prefs.Preferences.userRoot().node("tapaal-smoke-check");
        node.put("written", "yes");
        node.flush();
        assertTrue(Files.exists(preferences.resolve(".java/.userPrefs/tapaal-smoke-check/prefs.xml")),
            "Preferences should be written below " + preferences);
    }
}
