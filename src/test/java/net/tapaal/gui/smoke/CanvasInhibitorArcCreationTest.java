package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import pipe.gui.petrinet.graphicElements.tapn.TimedInhibitorArcComponent;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class CanvasInhibitorArcCreationTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void inhibitorToolCreatesAnInhibitorInsteadOfAConsumingInput() throws Exception {
        var tab = gui.open("canvas-workbench/before.tapn");

        gui.selectTool(tab, "Inhibitor arc");
        gui.clickObject(tab, "T0");
        assertEquals(List.of(), gui.arcs(tab), "A transition cannot start an inhibitor arc");
        gui.clickObject(tab, "P0");
        gui.clickObject(tab, "T0");

        assertEquals(List.of("P0 -> T0"), gui.arcs(tab));
        gui.onEdt(() -> {
            var arc = assertInstanceOf(TimedInhibitorArcComponent.class, tab.currentTemplate().guiModel().getArcs()[0]);
            var model = tab.currentTemplate().model();
            var source = model.getPlaceByName("P0");
            var target = model.getTransitionByName("T0");
            assertSame(arc.underlyingTimedInhibitorArc(), model.getInhibitorArcFromPlaceAndTransition(source, target));
            assertNull(model.getInputArcFromPlaceToTransition(source, target));
            return null;
        });
    }
}
