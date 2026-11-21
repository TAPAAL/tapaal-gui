package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CanvasDeleteConnectedTransitionTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void deletingATransitionRemovesOnlyItsIncidentArcsAndRestoresTheirProperties() throws Exception {
        var tab = gui.open("canvas-connected/before.tapn");

        gui.clickObject(tab, "T0");
        gui.keys("DELETE");
        assertEquals(List.of("T1"), gui.transitionNames(tab));
        assertEquals(List.of("P0 -> T1"), gui.arcs(tab));
        assertEquals(Map.of("P0", 1, "P1", 0), gui.marking(tab));

        gui.menu("Edit", "Undo");
        assertEquals(List.of("T0", "T1"), gui.transitionNames(tab));
        assertEquals(List.of("P0 -> T0", "P0 -> T1", "T0 -> P1"), gui.arcs(tab));
        gui.onEdt(() -> {
            var model = tab.currentTemplate().model();
            var transition = model.getTransitionByName("T0");
            var input = model.getInputArcFromPlaceToTransition(model.getPlaceByName("P0"), transition);
            var output = model.getOutputArcFromTransitionAndPlace(transition, model.getPlaceByName("P1"));
            assertEquals("[1,4]", input.interval().toString());
            assertEquals(2, input.getWeight().value());
            assertEquals(3, output.getWeight().value());
            return null;
        });
        gui.menu("Edit", "Redo");
        assertEquals(List.of("T1"), gui.transitionNames(tab));
        assertEquals(List.of("P0 -> T1"), gui.arcs(tab));
    }
}
