package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

class FilesSaveCleanDocumentTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void savingAnUneditedDocumentKeepsItCleanAndIdentical() throws Exception {
        String fixture = "files-marking/counter.tapn";
        var tab = gui.open(fixture);
        assertFalse(gui.onEdt(tab::getNetChanged), "A loaded file should start clean");

        gui.menu("File", "Save");

        assertSame(tab, gui.currentTab());
        assertFalse(gui.onEdt(tab::getNetChanged));
        gui.assertSavedMatches(fixture, "Saving a clean document should preserve its contents");
    }
}
