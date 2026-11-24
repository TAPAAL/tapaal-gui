package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import net.tapaal.gui.petrinet.editor.ConstantsPane;
import pipe.gui.petrinet.PetriNetTab;

import static org.junit.jupiter.api.Assertions.*;

class ViewQueryWorkspaceTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void workspacePresetsUpdatePanelsAndViewMenuTogether() throws Exception {
        var tab = gui.open("view-query-fixtures/components.tapn");
        gui.menu("View", "Show simple workspace");
        assertWorkspace(tab, false);
        gui.menu("View", "Show advanced workspace");
        assertWorkspace(tab, true);
    }

    private void assertWorkspace(PetriNetTab tab, boolean advanced) throws Exception {
        gui.onEdt(() -> {
            assertEquals(advanced, tab.getTemplateExplorer().isShowing());
            assertEquals(advanced, tab.getSharedPlacesAndTransitionsPanel().isShowing());
            assertEquals(advanced,
                GuiHarness.findAll(tab, ConstantsPane.class, ConstantsPane::isShowing).size() == 1);
            assertTrue(tab.getQueryPane().isShowing());

            for (String option : new String[]{"Display components", "Display shared places/transitions", "Display constants"}) {
                assertEquals(advanced, GuiHarness.menuItem("View", option).isSelected(), option);
            }
            assertTrue(GuiHarness.menuItem("View", "Display queries").isSelected());
            return null;
        });
    }
}
