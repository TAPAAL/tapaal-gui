package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import pipe.gui.TAPAALGUI;
import pipe.gui.petrinet.PetriNetTab;

import static org.junit.jupiter.api.Assertions.*;

class ViewQueryZoomTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void zoomMenuUpdatesSliderAndCanvasScale() throws Exception {
        var tab = gui.open("view-query-fixtures/components.tapn");
        assertScale(tab, 100, 100);
        gui.menu("View", "Zoom in");
        assertScale(tab, 110, 110);
        gui.menu("View", "Zoom out");
        assertScale(tab, 100, 100);
    }

    private void assertScale(PetriNetTab tab, int percent, int pixelDistance) throws Exception {
        assertEquals(percent, gui.onEdt(() -> TAPAALGUI.getAppGui().getZoomSlider().getValue()));
        var left = gui.canvasPoint(tab, 150, 220);
        var right = gui.canvasPoint(tab, 250, 220);
        assertEquals(pixelDistance, right.x - left.x);
        assertEquals(left.y, right.y);
        assertEquals(180 * percent / 100,
            gui.objectPoint(tab, "Ready").x - gui.objectPoint(tab, "Start").x,
            "The rendered place positions should scale with the canvas");
    }
}
