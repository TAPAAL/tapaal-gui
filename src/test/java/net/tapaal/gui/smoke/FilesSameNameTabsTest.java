package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.List;
import java.util.Map;

import static net.tapaal.gui.smoke.GuiHarness.tabbedPane;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

class FilesSameNameTabsTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void matchingTabTitlesKeepDistinctFileIdentities() throws Exception {
        String leftFixture = "files-same-name/left/model.tapn";
        String rightFixture = "files-same-name/right/model.tapn";
        var tabs = gui.openAll(leftFixture, rightFixture);
        var left = tabs.get(0);
        var right = tabs.get(1);
        assertNotSame(left, right);
        assertEquals(List.of("model.tapn", "model.tapn"), gui.onEdt(() -> {
            var pane = tabbedPane();
            return List.of(pane.getTitleAt(0), pane.getTitleAt(1));
        }));
        assertEquals(gui.file(leftFixture), gui.onEdt(() -> left.getFile().toPath()));
        assertEquals(gui.file(rightFixture), gui.onEdt(() -> right.getFile().toPath()));

        gui.selectTab(left);
        assertEquals(Map.of("Left", 1), gui.marking(gui.currentTab()));
        gui.selectTab(right);
        assertEquals(Map.of("Right", 2), gui.marking(gui.currentTab()));
        assertSame(right, gui.currentTab());
    }
}
