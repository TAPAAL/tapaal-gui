package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilesCloseConfirmNoTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void noClosesOnlyTheSelectedDirtyTabWhenBothHaveEdits() throws Exception {
        String firstFixture = "files-marking/counter.tapn";
        String secondFixture = "files-marking/reserve.tapn";
        var tabs = gui.openAll(firstFixture, secondFixture);
        var survivor = tabs.get(0);
        var discarded = tabs.get(1);
        gui.selectTab(survivor);
        gui.selectTool(survivor, "Add token");
        gui.clickObject(survivor, "Count");
        gui.selectTab(discarded);
        gui.selectTool(discarded, "Add token");
        gui.clickObject(discarded, "Reserve");
        assertTrue(gui.onEdt(discarded::getNetChanged));

        gui.menu("File", "Close");
        var prompt = gui.awaitDialog("Confirm Save Current File");
        gui.clickButton(prompt, "No");
        gui.awaitClosed(prompt);

        assertEquals(List.of(survivor), gui.tabs());
        assertSame(survivor, gui.currentTab());
        assertTrue(gui.onEdt(survivor::getNetChanged), "Discard applies only to the closing tab");
        assertEquals(Map.of("Count", 3), gui.marking(survivor));
        gui.assertFileMatches(gui.file(secondFixture), secondFixture, "No should discard the closing tab's edit");
        gui.assertSavedMatches(firstFixture, "No should not write the surviving tab's pending edit");
    }
}
