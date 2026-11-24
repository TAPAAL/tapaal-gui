package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import pipe.gui.petrinet.graphicElements.tapn.TimedOutputArcComponent;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

class CanvasBentOutputArcTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void drawingThroughEmptyCanvasPreservesTheOutputArcBend() throws Exception {
        var tab = gui.open("canvas-workbench/before.tapn");

        gui.selectTool(tab, "Arc");
        gui.clickObject(tab, "T0");
        gui.clickCanvas(tab, 345, 165);
        gui.clickObject(tab, "P1");

        assertEquals(List.of("T0 -> P1"), gui.arcs(tab));
        gui.onEdt(() -> {
            var arc = assertInstanceOf(TimedOutputArcComponent.class, tab.currentTemplate().guiModel().getArcs()[0]);
            var model = tab.currentTemplate().model();
            assertSame(arc.underlyingArc(), model.getOutputArcFromTransitionAndPlace(
                model.getTransitionByName("T0"), model.getPlaceByName("P1")));
            assertEquals(3, arc.getArcPath().getArcPathPoints().size(), "The two endpoints and the bend must be present");
            var bend = arc.getArcPath().getArcPathPoint(1).getPoint();
            assertEquals(345.0, bend.getX(), 0.01);
            assertEquals(165.0, bend.getY(), 0.01);
            return null;
        });
        gui.assertSnapshotMatches(tab, "canvas-workbench/bent-output.tapn",
            "The written arc should have the intended direction, weight and bend");
    }
}
