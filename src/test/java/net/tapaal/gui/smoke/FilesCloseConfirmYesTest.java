package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.awt.Container;
import java.util.List;
import javax.swing.JButton;

import static net.tapaal.gui.smoke.GuiHarness.find;
import static net.tapaal.gui.smoke.GuiHarness.tabbedPane;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilesCloseConfirmYesTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void yesSavesTheFileOfTheInactiveTabBeingClosed() throws Exception {
        String firstFixture = "files-marking/counter.tapn";
        String secondFixture = "files-marking/reserve.tapn";
        var tabs = gui.openAll(firstFixture, secondFixture);
        var closing = tabs.get(0);
        var other = tabs.get(1);
        gui.selectTab(closing);
        gui.selectTool(closing, "Add token");
        gui.clickObject(closing, "Count");
        gui.selectTab(other);
        assertTrue(gui.onEdt(closing::getNetChanged));
        assertSame(other, gui.currentTab());

        gui.click(gui.centerOf(() -> {
            var pane = tabbedPane();
            var header = (Container) pane.getTabComponentAt(pane.indexOfComponent(closing));
            return find(header, JButton.class, button -> "Close this tab".equals(button.getToolTipText()));
        }));
        var prompt = gui.awaitDialog("Confirm Save Current File");
        assertSame(closing, gui.currentTab(), "The prompt should show the document being closed");
        gui.clickButton(prompt, "Yes");
        gui.awaitClosed(prompt);

        assertEquals(List.of(other), gui.tabs());
        assertSame(other, gui.currentTab());
        gui.assertSavedMatches("files-marking/counter-saved.tapn", "Yes should write the closing tab's edit");
        gui.assertFileMatches(gui.file(secondFixture), secondFixture, "The previously selected file should stay untouched");
    }
}
