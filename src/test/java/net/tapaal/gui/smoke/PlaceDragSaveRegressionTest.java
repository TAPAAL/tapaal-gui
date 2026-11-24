package net.tapaal.gui.smoke;

import dk.aau.cs.io.TimedArcPetriNetNetworkWriter;
import net.tapaal.gui.UnavailableTransformerFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import pipe.gui.petrinet.PetriNetTab;

import javax.xml.transform.TransformerConfigurationException;
import java.awt.Point;

import static net.tapaal.gui.smoke.GuiHarness.place;
import static net.tapaal.gui.smoke.GuiHarness.tool;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Drags a place with the mouse, saves through the File menu and compares the saved document.
 *
 * <p>Recipe for a saved-document regression: put {@code before.tapn} and {@code expected.tapn} in
 * {@code src/test/resources/validation/<case>/}, drive the editor, save, and compare.
 */
class PlaceDragSaveRegressionTest {
    // Multiples of the 15-pixel editor grid at 100% zoom, so grid snapping cannot change the result.
    private static final int DRAG_X = 90;
    private static final int DRAG_Y = 60;

    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void draggedPlaceIsSavedAtItsNewPosition() throws Exception {
        PetriNetTab tab = gui.open("move-place/before.tapn");
        assertTrue(gui.onEdt(() -> tool(tab, "Select").isSelected()), "The editor should start with the Select tool");

        gui.dragPlace(tab, "P0", DRAG_X, DRAG_Y);
        assertEquals(new Point(60 + DRAG_X, 60 + DRAG_Y), gui.onEdt(() -> position(tab, "P0")),
            "The mouse drag should move P0 on the canvas");
        assertTrue(gui.onEdt(tab::getNetChanged), "The drag should mark the net as changed");

        gui.menu("File", "Save");
        assertFalse(gui.onEdt(tab::getNetChanged), "Saving should clear the dirty state");
        gui.assertSavedMatches("move-place/expected.tapn");

        // Make the writer fail inside its own catch block, then save a second move.
        gui.dragPlace(tab, "P0", DRAG_X, DRAG_Y);
        try (var ignored = UnavailableTransformerFactory.select()) {
            gui.menu("File", "Save");
        }
        var saveFailures = gui.caught().take(TimedArcPetriNetNetworkWriter.SAVE_FILE_SITE);
        assertEquals(1, saveFailures.size(), "The writer should report the failure it swallows: " + saveFailures);
        var failure = saveFailures.get(0).failure();
        assertInstanceOf(TransformerConfigurationException.class, failure);
        assertEquals(UnavailableTransformerFactory.MESSAGE, failure.getMessage());
        gui.assertSavedMatches("move-place/expected.tapn", "The failed save should leave the previous file in place");
        System.out.println("Observed swallowed save failure " + saveFailures.get(0)
            + "; editor dirty after failed save: " + gui.onEdt(tab::getNetChanged));
    }

    private static Point position(PetriNetTab tab, String name) {
        var place = place(tab, name);
        return new Point(place.getOriginalX(), place.getOriginalY());
    }
}
