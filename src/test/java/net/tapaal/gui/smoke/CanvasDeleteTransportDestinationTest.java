package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CanvasDeleteTransportDestinationTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void deletingTheDestinationRemovesBothTransportHalvesAndUndoRestoresThem() throws Exception {
        var tab = gui.open("canvas-transport/before.tapn");
        assertEquals(List.of("P0 -> T0", "T0 -> P1"), gui.arcs(tab));

        gui.clickObject(tab, "P1");
        gui.keys("DELETE");
        assertEquals(Map.of("P0", 1), gui.marking(tab));
        assertEquals(List.of("T0"), gui.transitionNames(tab));
        assertEquals(List.of(), gui.arcs(tab));

        gui.menu("Edit", "Undo");
        assertEquals(Map.of("P0", 1, "P1", 0), gui.marking(tab));
        assertEquals(List.of("P0 -> T0", "T0 -> P1"), gui.arcs(tab));
        gui.onEdt(() -> {
            var model = tab.currentTemplate().model();
            var transport = model.getTransportArcFromPlaceTransitionAndPlace(
                model.getPlaceByName("P0"), model.getTransitionByName("T0"), model.getPlaceByName("P1"));
            assertEquals("[2,5]", transport.interval().toString());
            assertEquals(2, transport.getWeight().value());
            return null;
        });

        gui.menu("Edit", "Redo");
        assertEquals(Map.of("P0", 1), gui.marking(tab));
        assertEquals(List.of(), gui.arcs(tab));
    }
}
