package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import pipe.gui.canvas.Grid;

import static org.junit.jupiter.api.Assertions.*;

class ViewQueryGridTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void cycleGridTraversesSpacingAndOffStates() throws Exception {
        var tab = gui.open("view-query-fixtures/components.tapn");
        assertTrue(gui.onEdt(Grid::isEnabled));
        assertEquals(15, gui.onEdt(Grid::getGridSpacing));

        gui.menu("View", "Cycle grid");
        assertTrue(gui.onEdt(Grid::isEnabled));
        assertEquals(30, gui.onEdt(Grid::getGridSpacing));
        gui.menu("View", "Cycle grid");
        assertFalse(gui.onEdt(Grid::isEnabled));
        assertEquals(101, gui.onEdt(() -> Grid.align(101, 100)), "Grid off should leave coordinates unsnapped");
        gui.menu("View", "Cycle grid");
        assertTrue(gui.onEdt(Grid::isEnabled));
        assertEquals(7, gui.onEdt(Grid::getGridSpacing));
        gui.menu("View", "Cycle grid");
        assertEquals(15, gui.onEdt(Grid::getGridSpacing));
        assertFalse(gui.onEdt(tab::getNetChanged));
    }
}
