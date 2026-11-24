package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import dk.aau.cs.model.tapn.TimedToken;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CanvasAddTokensTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void addTokenClicksAffectOnlyTheTargetAndUndoOneClickAtATime() throws Exception {
        var tab = gui.open("canvas-workbench/before.tapn");

        gui.selectTool(tab, "Add token");
        gui.clickObject(tab, "P1");
        assertEquals(Map.of("P0", 1, "P1", 1), gui.marking(tab));
        gui.clickObject(tab, "P1");
        assertEquals(Map.of("P0", 1, "P1", 2), gui.marking(tab));
        assertEquals(List.of(BigDecimal.ZERO, BigDecimal.ZERO), gui.onEdt(() ->
            tab.currentTemplate().model().getPlaceByName("P1").tokens().stream().map(TimedToken::age).toList()));

        gui.menu("Edit", "Undo");
        assertEquals(Map.of("P0", 1, "P1", 1), gui.marking(tab));
        gui.menu("Edit", "Undo");
        assertEquals(Map.of("P0", 1, "P1", 0), gui.marking(tab));
    }
}
