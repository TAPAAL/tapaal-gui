package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import pipe.gui.petrinet.PetriNetTab;

import javax.swing.JDialog;
import java.awt.Point;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EditorsOutputArcWeightTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void usesTheSelectedConstantAsTheOutputMultiplicity() throws Exception {
        var tab = gui.open("editors-dialogs/flow.tapn");
        var dialog = openArc(tab, "T0", "P1");
        gui.clickButton(dialog, "Use Constant");
        gui.clickButton(dialog, "OK");
        gui.awaitClosed(dialog);

        var outputWeight = gui.onEdt(() -> {
            var weight = tab.currentTemplate().model().outputArcs().iterator().next().getWeight();
            return Map.entry(weight.nameForSaving(true), weight.value());
        });
        assertEquals(Map.entry("deadline", 5), outputWeight);
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
