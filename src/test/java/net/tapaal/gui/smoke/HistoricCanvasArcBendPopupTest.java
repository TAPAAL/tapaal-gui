package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import pipe.gui.petrinet.graphicElements.ArcPath;
import pipe.gui.petrinet.graphicElements.ArcPathPoint;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HistoricCanvasArcBendPopupTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void rightClickOnTheInteriorBendCanChangeItsPointType() throws Exception {
        // Launchpad 1947517 restored the popup menu on the point itself.
        var tab = gui.open("historic-canvas-arc-bend/before.tapn");
        var connection = List.of("T0 -> P0");
        assertEquals(connection, gui.arcs(tab));
        var arc = gui.onEdt(() -> tab.currentTemplate().guiModel().getArcs()[0]);
        var path = gui.onEdt(arc::getArcPath);
        var before = gui.onEdt(() -> geometry(path));
        assertEquals(4, before.size());
        assertEquals(new PointState(new Point2D.Double(195, 165), ArcPathPoint.STRAIGHT), before.get(1));

        // Select all to show the handles and keep them visible as the pointer moves.
        gui.menu("Edit", "Select all");
        var bend = gui.onEdt(() -> path.getArcPathPoint(1));
        gui.rightClick(gui.centerOf(() -> bend));
        gui.clickPopupItem("Change to Curved");

        var curved = new ArrayList<>(before);
        curved.set(1, new PointState(before.get(1).position(), ArcPathPoint.CURVED));
        assertEquals(curved, gui.onEdt(() -> geometry(path)), "Only the clicked bend should become curved");
        assertEquals(connection, gui.arcs(tab));
        assertEquals(1, gui.onEdt(() -> arc.getWeight().value()));

        gui.menu("Edit", "Undo");
        assertEquals(before, gui.onEdt(() -> geometry(path)));
        gui.menu("Edit", "Redo");
        assertEquals(curved, gui.onEdt(() -> geometry(path)));
    }

    private static List<PointState> geometry(ArcPath path) {
        return path.getArcPathPoints().stream()
            .map(point -> new PointState(point.getRealPoint(), point.getPointType()))
            .toList();
    }

    private record PointState(Point2D.Double position, boolean curved) {}
}
