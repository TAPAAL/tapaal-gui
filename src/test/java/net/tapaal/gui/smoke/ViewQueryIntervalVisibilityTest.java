package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import pipe.gui.petrinet.PetriNetTab;
import pipe.gui.petrinet.graphicElements.tapn.TimedInputArcComponent;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class ViewQueryIntervalVisibilityTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void hidesOnlyUnrestrictedIntervalLabels() throws Exception {
        var tab = gui.open("view-query-fixtures/components.tapn");
        String menu = "Display intervals [0,∞)";
        gui.menu("View", "Show advanced workspace");
        assertTrue(gui.onEdt(() -> GuiHarness.menuItem("View", menu).isSelected()));
        assertEquals(Map.of("Start", "[0,∞)", "Ready", "[0,4]"), inputLabels(tab));

        gui.menu("View", menu);
        assertFalse(gui.onEdt(() -> GuiHarness.menuItem("View", menu).isSelected()));
        assertEquals(Map.of("Start", "", "Ready", "[0,4]"), inputLabels(tab));

        gui.menu("View", menu);
        assertTrue(gui.onEdt(() -> GuiHarness.menuItem("View", menu).isSelected()));
        assertEquals(Map.of("Start", "[0,∞)", "Ready", "[0,4]"), inputLabels(tab));
        assertFalse(gui.onEdt(tab::getNetChanged));
    }

    private Map<String, String> inputLabels(PetriNetTab tab) throws Exception {
        return gui.onEdt(() -> Arrays.stream(tab.drawingSurface().getGuiModel().getArcs())
            .filter(TimedInputArcComponent.class::isInstance)
            .map(TimedInputArcComponent.class::cast)
            .collect(Collectors.toMap(arc -> arc.getSource().getName(), arc -> arc.getNameLabel().getText().trim())));
    }
}
