package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import pipe.gui.petrinet.PetriNetTab;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Adds a place, undoes and redoes it, saves, closes and reopens the file.
 *
 * <p>Recipe for a workflow check: assert on editor state after each step instead of on the saved file.
 */
class GuiWorkflowSmokeTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void editUndoRedoSaveAndReopen() throws Exception {
        PetriNetTab original = gui.open("one-place.tapn");
        assertEquals(Map.of("Start", 1), gui.marking(original));

        gui.selectTool(original, "Place");
        gui.click(gui.centerOf(original::drawingSurface));

        var editedMarking = gui.marking(original);
        assertEquals(2, editedMarking.size(), "Clicking the canvas with the Place tool should add a place");
        assertEquals(1, editedMarking.get("Start"));
        assertTrue(editedMarking.containsValue(0));

        gui.menu("Edit", "Undo");
        assertEquals(Map.of("Start", 1), gui.marking(original));
        gui.menu("Edit", "Redo");
        assertEquals(editedMarking, gui.marking(original));
        gui.menu("File", "Save");
        assertFalse(gui.onEdt(original::getNetChanged), "Saving should clear the dirty state");
        gui.menu("File", "Close");
        assertTrue(gui.tabs().isEmpty(), "Close should remove the only tab");

        var reopened = gui.openFiles(gui.file()).get(0);
        assertNotSame(original, reopened);
        assertEquals(editedMarking, gui.marking(reopened));
        System.out.println("Opened, edited, undid, redid, saved and reopened marking " + editedMarking);
    }
}
