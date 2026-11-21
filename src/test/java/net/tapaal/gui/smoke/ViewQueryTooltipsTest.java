package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.swing.ToolTipManager;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ViewQueryTooltipsTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void tooltipMenuControlsSwingTooltips() throws Exception {
        gui.open("view-query-fixtures/components.tapn");
        gui.menu("View", "Show simple workspace");
        assertTooltips(true);
        gui.menu("View", "Display tool tips");
        assertTooltips(false);
        gui.menu("View", "Display tool tips");
        assertTooltips(true);
    }

    private void assertTooltips(boolean enabled) throws Exception {
        gui.onEdt(() -> {
            assertEquals(enabled, GuiHarness.menuItem("View", "Display tool tips").isSelected());
            assertEquals(enabled, ToolTipManager.sharedInstance().isEnabled());
            return null;
        });
    }
}
