package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import pipe.gui.TAPAALGUI;

import javax.swing.JComboBox;

import static org.junit.jupiter.api.Assertions.*;

class ViewQueryFeatureEligibilityTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void switchingTabsRefreshesTimeAndStochasticControls() throws Exception {
        var tabs = gui.openAll("view-query-fixtures/components.tapn", "view-query-fixtures/untimed.tapn");
        var timed = tabs.get(0);
        var untimed = tabs.get(1);

        gui.selectTab(timed);
        assertFeatureControls("Yes", true);
        gui.selectTab(untimed);
        assertFeatureControls("No", false);
        gui.selectTab(timed);
        assertFeatureControls("Yes", true);
    }

    private void assertFeatureControls(String timed, boolean stochasticEnabled) throws Exception {
        gui.onEdt(() -> {
            var frame = TAPAALGUI.getAppGui();
            var time = GuiHarness.labelled(frame, "Timed: ", JComboBox.class);
            var stochastic = GuiHarness.labelled(frame, "   Stochastic: ", JComboBox.class);
            assertEquals(timed, time.getSelectedItem());
            assertEquals("No", stochastic.getSelectedItem());
            assertEquals(stochasticEnabled, stochastic.isEnabled());
            return null;
        });
    }
}
