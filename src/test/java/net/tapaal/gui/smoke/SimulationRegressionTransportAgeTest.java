package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.math.BigDecimal;
import java.util.Map;

import static net.tapaal.gui.smoke.GuiHarness.place;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SimulationRegressionTransportAgeTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void aTransportArcCarriesTheExistingAgeToItsDestination() throws Exception {
        var tab = gui.open("simulation-transport/model.tapn");
        gui.menu("Simulator", "Simulation mode");
        gui.keys("W");
        assertEquals(BigDecimal.ONE, gui.onEdt(() -> place(tab, "Source").underlyingPlace().tokens()
            .get(0).age().stripTrailingZeros()));

        gui.clickObject(tab, "Carry");

        assertEquals(Map.of("Source", 0, "Destination", 1), gui.marking(tab));
        assertEquals(BigDecimal.ONE, gui.onEdt(() -> place(tab, "Destination").underlyingPlace().tokens()
            .get(0).age().stripTrailingZeros()), "Transport must preserve the carried age");
    }
}
