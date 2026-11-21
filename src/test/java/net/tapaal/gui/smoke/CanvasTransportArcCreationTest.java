package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import pipe.gui.petrinet.graphicElements.tapn.TimedTransportArcComponent;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class CanvasTransportArcCreationTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void threeClicksCreateOneTransportArcWithTwoLinkedCanvasHalves() throws Exception {
        var tab = gui.open("canvas-workbench/before.tapn");

        gui.selectTool(tab, "Transport arc");
        gui.clickObject(tab, "P0");
        gui.clickObject(tab, "T0");
        assertEquals(List.of(), gui.arcs(tab), "The first two clicks must not commit either half");
        gui.clickObject(tab, "P1");

        assertEquals(List.of("P0 -> T0", "T0 -> P1"), gui.arcs(tab));
        gui.onEdt(() -> {
            var arcs = tab.currentTemplate().guiModel().getArcs();
            var first = assertInstanceOf(TimedTransportArcComponent.class, arcs[0]);
            var second = assertInstanceOf(TimedTransportArcComponent.class, arcs[1]);
            assertNotEquals(first.isInPreSet(), second.isInPreSet());
            assertSame(second, first.getConnectedTo());
            assertSame(first, second.getConnectedTo());

            var model = tab.currentTemplate().model();
            var transport = model.getTransportArcFromPlaceTransitionAndPlace(
                model.getPlaceByName("P0"), model.getTransitionByName("T0"), model.getPlaceByName("P1"));
            assertNotNull(transport);
            assertSame(transport, first.underlyingTransportArc());
            assertSame(transport, second.underlyingTransportArc());
            return null;
        });
    }
}
