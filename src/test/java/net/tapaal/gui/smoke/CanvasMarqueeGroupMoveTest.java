package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import pipe.gui.petrinet.PetriNetTab;
import pipe.gui.petrinet.graphicElements.PetriNetObject;
import java.awt.Point;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CanvasMarqueeGroupMoveTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void draggingABoxSelectionMovesOnlyEnclosedObjectsInOneUndoStep() throws Exception {
        var tab = gui.open("canvas-workbench/before.tapn");

        gui.drag(gui.canvasPoint(tab, 30, 30), 255, 90);
        assertEquals(List.of("P0", "T0"), gui.onEdt(() -> tab.drawingSurface().getSelectionObject()
            .getSelection().stream().map(PetriNetObject::getName).sorted().toList()));

        gui.dragObject(tab, "P0", 60, 90);
        assertEquals(List.of(new Point(120, 150), new Point(300, 150), new Point(420, 60)), positions(tab));

        gui.menu("Edit", "Undo");
        assertEquals(List.of(new Point(60, 60), new Point(240, 60), new Point(420, 60)), positions(tab));
        gui.menu("Edit", "Redo");
        assertEquals(List.of(new Point(120, 150), new Point(300, 150), new Point(420, 60)), positions(tab));
    }

    private List<Point> positions(PetriNetTab tab) throws Exception {
        return gui.onEdt(() -> List.of("P0", "T0", "P1").stream().map(name -> {
            var object = GuiHarness.object(tab, name);
            return new Point(object.getOriginalX(), object.getOriginalY());
        }).toList());
    }
}
