package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import pipe.gui.TAPAALGUI;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilesTabSelectionTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void selectingTheMiddleTabUpdatesTheWindowTitleAndClosingSelectsItsSuccessor() throws Exception {
        var tabs = gui.openAll("files-marking/counter.tapn", "files-marking/reserve.tapn", "one-place.tapn");
        var first = tabs.get(0);
        var middle = tabs.get(1);
        var last = tabs.get(2);
        assertSame(last, gui.currentTab());

        gui.selectTab(middle);
        assertTrue(gui.onEdt(() -> TAPAALGUI.getAppGui().getTitle().endsWith(": reserve.tapn")));
        assertEquals(Map.of("Reserve", 4), gui.marking(gui.currentTab()));

        gui.menu("File", "Close");
        assertEquals(List.of(first, last), gui.tabs());
        assertSame(last, gui.currentTab(), "Removing the middle tab should select its successor");
        assertTrue(gui.onEdt(() -> TAPAALGUI.getAppGui().getTitle().endsWith(": one-place.tapn")));
    }
}
