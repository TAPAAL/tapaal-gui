package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.awt.Container;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;

import static net.tapaal.gui.smoke.GuiHarness.find;
import static net.tapaal.gui.smoke.GuiHarness.tabbedPane;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilesCloseBackgroundCleanTabTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void closingAnInactiveCleanTabKeepsTheDirtyTabSelected() throws Exception {
        var tabs = gui.openAll("files-marking/counter.tapn", "files-marking/reserve.tapn");
        var background = tabs.get(0);
        var active = tabs.get(1);
        gui.selectTool(active, "Add token");
        gui.clickObject(active, "Reserve");
        assertFalse(gui.onEdt(background::getNetChanged));
        assertTrue(gui.onEdt(active::getNetChanged));

        gui.click(gui.centerOf(() -> {
            var pane = tabbedPane();
            var header = (Container) pane.getTabComponentAt(pane.indexOfComponent(background));
            return find(header, JButton.class, button -> "Close this tab".equals(button.getToolTipText()));
        }));

        assertEquals(List.of(active), gui.tabs());
        assertSame(active, gui.currentTab());
        assertEquals(Map.of("Reserve", 5), gui.marking(active));
        assertTrue(gui.onEdt(active::getNetChanged));
    }
}
