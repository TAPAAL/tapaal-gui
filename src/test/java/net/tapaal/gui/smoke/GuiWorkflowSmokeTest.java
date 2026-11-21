package net.tapaal.gui.smoke;

import dk.aau.cs.model.tapn.TimedPlace;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import pipe.gui.TAPAALGUI;
import pipe.gui.petrinet.PetriNetTab;
import pipe.gui.petrinet.graphicElements.tapn.TimedPlaceComponent;

import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.tapaal.gui.smoke.GuiHarness.tool;
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
        assertEquals(Map.of("Start", 1), marking(original));

        gui.runOnEdt(() -> {
            var place = tool(original, "Place");
            assertTrue(place.isEnabled(), "The Place tool must be enabled");
            place.actionPerformed(new ActionEvent(original.drawingSurface(), ActionEvent.ACTION_PERFORMED, "Place"));
        });
        gui.click(gui.centerOnScreen(original::drawingSurface));

        var editedMarking = marking(original);
        assertEquals(2, editedMarking.size(), "Clicking the canvas with the Place tool should add a place");
        assertEquals(1, editedMarking.get("Start"));
        assertTrue(editedMarking.containsValue(0));

        gui.menu("Edit", "Undo");
        assertEquals(Map.of("Start", 1), marking(original));
        gui.menu("Edit", "Redo");
        assertEquals(editedMarking, marking(original));
        gui.menu("File", "Save");
        assertFalse(gui.onEdt(original::getNetChanged), "Saving should clear the dirty state");
        gui.menu("File", "Close");

        gui.runOnEdt(() -> {
            var controller = TAPAALGUI.getAppGuiController();
            assertTrue(controller.getTabs().isEmpty(), "Close should remove the only tab");
            controller.openFiles(List.of(gui.file().toFile()));
        });
        var reopened = gui.await("the reopened file's tab",
            () -> TAPAALGUI.getAppGuiController().getTabs().stream().findFirst().orElse(null));
        assertNotSame(original, reopened);
        assertEquals(editedMarking, marking(reopened));
        System.out.println("Opened, edited, undid, redid, saved and reopened marking " + editedMarking);
    }

    /** Place name to token count, after checking that the canvas and the model agree. */
    private Map<String, Integer> marking(PetriNetTab tab) throws Exception {
        return gui.onEdt(() -> {
            var modelMarking = tab.currentTemplate().model().places().stream()
                .collect(Collectors.toMap(TimedPlace::name, TimedPlace::numberOfTokens));
            var canvasMarking = Arrays.stream(tab.drawingSurface().getGuiModel().getPlaces())
                .map(TimedPlaceComponent.class::cast)
                .collect(Collectors.toMap(TimedPlaceComponent::getName, TimedPlaceComponent::getNumberOfTokens));
            assertEquals(modelMarking, canvasMarking, "Canvas and model markings must agree");
            return modelMarking;
        });
    }
}
