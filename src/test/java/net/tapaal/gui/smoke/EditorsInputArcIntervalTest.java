package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import pipe.gui.petrinet.PetriNetTab;

import javax.swing.JDialog;
import javax.swing.JSpinner;
import java.awt.Point;

import static net.tapaal.gui.smoke.GuiHarness.labelled;
import static org.junit.jupiter.api.Assertions.assertEquals;

class EditorsInputArcIntervalTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void commitsALowerBoundAndPreservesTheConstantUpperBound() throws Exception {
        var tab = gui.open("editors-dialogs/flow.tapn");
        var dialog = openArc(tab, "P0", "T0");
        gui.typeInto(gui.onEdt(() -> labelled(dialog, "Time Interval:", JSpinner.class)), "2");
        gui.clickButton(dialog, "OK");
        gui.awaitClosed(dialog);

        assertEquals("[2,deadline]", gui.onEdt(() -> tab.currentTemplate().model().inputArcs()
            .iterator().next().interval().toString()));
    }

    private JDialog openArc(PetriNetTab tab, String source, String target) throws Exception {
        // The fixture has straight, horizontal arcs between these node centers.
        var from = gui.objectPoint(tab, source);
        var to = gui.objectPoint(tab, target);
        gui.rightClick(new Point((from.x + to.x) / 2, from.y));
        gui.clickPopupItem("Properties");
        return gui.awaitDialog("Edit Arc");
    }
}
