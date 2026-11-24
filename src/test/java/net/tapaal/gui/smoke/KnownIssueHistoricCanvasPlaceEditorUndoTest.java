package net.tapaal.gui.smoke;

import dk.aau.cs.model.tapn.TimedToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.extension.RegisterExtension;
import pipe.gui.petrinet.PetriNetTab;

import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import static net.tapaal.gui.smoke.GuiHarness.find;
import static net.tapaal.gui.smoke.GuiHarness.menuItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIfEnvironmentVariable(named = "TAPAAL_GUI_KNOWN_ISSUES", matches = "true")
class KnownIssueHistoricCanvasPlaceEditorUndoTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void acceptingAnUnchangedPlaceEditorDoesNotAddAnUndoStep() throws Exception {
        var tab = gui.open("historic-canvas-place-editor-undo/before.tapn");
        assertTrue(gui.onEdt(() -> tab.getLens().isColored()));
        var before = Map.of("P0", 0);
        var after = Map.of("P0", 1);
        assertEquals(before, gui.marking(tab));

        gui.doubleClickObject(tab, "P0");
        var editor = gui.awaitDialog("Edit Place");
        var tokenPanel = gui.onEdt(() -> find(editor, JPanel.class, panel ->
            panel.getBorder() instanceof TitledBorder border && "Tokens".equals(border.getTitle())));
        gui.clickButton(tokenPanel, "Add");
        gui.clickButton(editor, "OK");
        gui.awaitClosed(editor);
        assertEquals(after, gui.marking(tab));
        var editedTokens = gui.onEdt(() -> tokens(tab));
        var editedDocument = gui.snapshot(tab);

        gui.doubleClickObject(tab, "P0");
        var reopened = gui.awaitDialog("Edit Place");
        gui.clickButton(reopened, "OK");
        gui.awaitClosed(reopened);
        assertEquals(after, gui.marking(tab));
        assertEquals(editedTokens, gui.onEdt(() -> tokens(tab)), "The unchanged OK must preserve the colored tokens");
        assertEquals(-1L, Files.mismatch(editedDocument, gui.snapshot(tab)),
            "The unchanged OK must preserve the serialized model");

        gui.keys("ctrl Z");
        assertTrue(gui.onEdt(() -> menuItem("Edit", "Redo").isEnabled()), "Undo must create a redo step");
        assertEquals(before, gui.marking(tab), "One Undo must remove the token added in the first dialog");
        assertFalse(gui.onEdt(() -> menuItem("Edit", "Undo").isEnabled()), "The marking edit was the only edit");

        gui.keys("ctrl Y");
        assertEquals(after, gui.marking(tab));
        assertFalse(gui.onEdt(() -> menuItem("Edit", "Redo").isEnabled()), "There is no unchanged edit left to redo");
    }

    private static List<TimedToken> tokens(PetriNetTab tab) {
        return tab.currentTemplate().model().getPlaceByName("P0").tokens().stream().map(TimedToken::clone).toList();
    }
}
