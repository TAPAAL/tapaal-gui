package net.tapaal.gui.smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import pipe.gui.petrinet.PetriNetTab;

import java.util.List;

import static net.tapaal.gui.smoke.GuiHarness.place;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SimulationRegressionOutputAgeTest {
    @RegisterExtension
    final GuiHarness gui = new GuiHarness();

    @Test
    void ordinaryOutputStartsAtZeroWhileRemainingInputKeepsItsAge() throws Exception {
        var tab = gui.open("simulation-weighted/model.tapn");
        gui.menu("Simulator", "Simulation mode");
        gui.keys("W");
        assertEquals(List.of("1", "1", "1"), ages(tab, "Supply"));

        gui.clickObject(tab, "Ship");

        assertEquals(List.of("1"), ages(tab, "Supply"));
        assertEquals(List.of("0", "0", "0"), ages(tab, "Delivered"));
    }

    private List<String> ages(PetriNetTab tab, String name) throws Exception {
        return gui.onEdt(() -> place(tab, name).underlyingPlace().tokens().stream()
            .map(token -> token.age().stripTrailingZeros().toPlainString())
            .toList());
    }
}
