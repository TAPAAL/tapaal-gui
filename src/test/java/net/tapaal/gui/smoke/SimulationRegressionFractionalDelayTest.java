package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.List;
import java.util.Map;

import static net.tapaal.gui.smoke.GuiHarness.place;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SimulationRegressionFractionalDelayTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void delayFieldAdvancesAllTokensAndRecordsOneExactFractionalStep() throws Exception {
        var tab = gui.open("simulation-weighted/model.tapn");
        gui.menu("Simulator", "Simulation mode");
        var controls = gui.onEdt(tab::getAnimationController);

        gui.typeInto(controls.TimeDelayField, "1.25");
        gui.clickButton(controls, "Time delay");

        assertEquals(Map.of("Supply", 3, "Delivered", 0), gui.marking(tab));
        assertEquals(List.of("1.25", "1.25", "1.25"), gui.onEdt(() ->
            place(tab, "Supply").underlyingPlace().tokens().stream()
                .map(token -> token.age().stripTrailingZeros().toPlainString())
                .toList()));
        var history = gui.onEdt(tab::getAnimationHistorySidePanel);
        assertEquals(1, gui.onEdt(history::getSelectedIndex));
        assertEquals("TimeDelay: 1.25", gui.onEdt(() -> history.getSelectedValue().replaceAll(" +", " ")));
    }
}
