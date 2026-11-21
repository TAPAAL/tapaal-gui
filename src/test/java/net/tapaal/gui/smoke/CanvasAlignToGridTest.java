package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import pipe.gui.petrinet.PetriNetTab;
import java.awt.Point;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CanvasAlignToGridTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void aligningAnOffGridNetIsOneReversibleEdit() throws Exception {
        var tab = gui.open("canvas-grid/before.tapn");
        var before = List.of(new Point(75, 75), new Point(225, 75), new Point(405, 75));
        var aligned = List.of(new Point(90, 90), new Point(240, 90), new Point(420, 90));
        assertEquals(before, positions(tab));

        gui.menu("View", "Cycle grid");
        assertEquals(before, positions(tab));

        gui.menu("View", "Align To Grid");
        assertEquals(aligned, positions(tab));

        gui.menu("Edit", "Undo");
        assertEquals(before, positions(tab));
        gui.menu("Edit", "Redo");
        assertEquals(aligned, positions(tab));
    }

    private List<Point> positions(PetriNetTab tab) throws Exception {
        return gui.onEdt(() -> List.of("P0", "T0", "P1").stream().map(name -> {
            var object = GuiHarness.object(tab, name);
            return new Point(object.getOriginalX(), object.getOriginalY());
        }).toList());
    }
}
