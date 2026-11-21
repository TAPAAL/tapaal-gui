package net.tapaal.gui.smoke;

import dk.aau.cs.model.CPN.ColoredTimeInterval;
import dk.aau.cs.model.tapn.TimedInputArc;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import pipe.gui.petrinet.PetriNetTab;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import java.awt.Point;
import java.util.Arrays;

import static net.tapaal.gui.smoke.GuiHarness.find;
import static net.tapaal.gui.smoke.GuiHarness.labelled;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HistoricEditorsColoredArcGuardTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void removesTheLastColorSpecificTimeGuard() throws Exception {
        var tab = gui.open("historic-editors-colored-arc/cpn-packet.tapn");
        var original = gui.onEdt(() -> arcData(packetInputArc(tab)));

        gui.rightClick(gui.onEdt(() -> inputArcScreenPoint(tab)));
        gui.clickPopupItem("Properties");
        var dialog = gui.awaitDialog("Edit Arc");
        var guards = gui.onEdt(() -> find(dialog, JPanel.class, panel ->
            panel.getBorder() instanceof TitledBorder border && border.getTitle().equals("Color specific time intervals")));
        var list = gui.onEdt(() -> find(guards, JList.class, component -> true));
        assertEquals(0, gui.onEdt(() -> list.getModel().getSize()));

        gui.typeInto(gui.onEdt(() -> labelled(guards, "Time Interval:", JSpinner.class)), "2");
        gui.clickButton(guards, "Add");
        gui.onEdt(() -> {
            assertEquals(1, list.getModel().getSize());
            var guard = assertInstanceOf(ColoredTimeInterval.class, list.getSelectedValue());
            assertEquals("dot", guard.getColor().toString());
            assertEquals("[2,inf)", guard.getInterval());
            return null;
        });

        // Launchpad #1959906
        gui.clickButton(guards, "Remove");
        assertEquals(0, gui.onEdt(() -> list.getModel().getSize()));
        gui.clickButton(dialog, "OK");
        gui.awaitClosed(dialog);

        gui.onEdt(() -> {
            var arc = packetInputArc(tab);
            assertTrue(arc.getColorTimeIntervals().isEmpty());
            assertEquals(original, arcData(arc));
            return null;
        });
    }

    private Point inputArcScreenPoint(PetriNetTab tab) {
        var surface = tab.drawingSurface();
        var arc = Arrays.stream(surface.getGuiModel().getArcs())
            .filter(candidate -> candidate.getSource().getName().equals("P0")
                && candidate.getTarget().getName().equals("Enter"))
            .findFirst().orElseThrow();
        // Click halfway along the first segment, clear of the label and the return arc.
        var start = arc.getArcPath().getPoint(0);
        var bend = arc.getArcPath().getPoint(1);
        var point = new Point((int) Math.round((start.x + bend.x) / 2),
            (int) Math.round((start.y + bend.y) / 2));
        assertTrue(surface.getVisibleRect().contains(point));
        assertSame(arc, SwingUtilities.getDeepestComponentAt(surface, point.x, point.y));
        SwingUtilities.convertPointToScreen(point, surface);
        return point;
    }

    private TimedInputArc packetInputArc(PetriNetTab tab) {
        for (var arc : tab.currentTemplate().model().inputArcs()) {
            if (arc.source().name().equals("P0") && arc.destination().name().equals("Enter")) {
                return arc;
            }
        }
        throw new AssertionError("Missing the P0 to Enter input arc");
    }

    private record ArcData(String interval, int weight, String expression) {}

    private ArcData arcData(TimedInputArc arc) {
        return new ArcData(arc.interval().toString(), arc.getWeight().value(), arc.getArcExpression().toString());
    }
}
