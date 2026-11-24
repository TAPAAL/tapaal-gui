package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import pipe.gui.petrinet.PetriNetTab;
import java.awt.Point;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CanvasSnappedPlaceCreationTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void placesUseFreeNamesAndSnapTheirCentersToTheGrid() throws Exception {
        var tab = gui.open("canvas-workbench/before.tapn");

        gui.selectTool(tab, "Place");
        gui.clickCanvas(tab, 202, 154);
        gui.clickCanvas(tab, 298, 186);

        assertEquals(Map.of("P0", 1, "P1", 0, "P2", 0, "P3", 0), gui.marking(tab));
        assertEquals(new Point(180, 135), gui.onEdt(() -> position(tab, "P2")));
        assertEquals(new Point(285, 165), gui.onEdt(() -> position(tab, "P3")));
    }

    private static Point position(PetriNetTab tab, String name) {
        var place = GuiHarness.place(tab, name);
        return new Point(place.getOriginalX(), place.getOriginalY());
    }
}
