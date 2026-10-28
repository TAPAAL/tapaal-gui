package dk.aau.cs.model.tapn;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class TimedTransitionDelayTest {

    @Test
    void delayIntervalUsesNetworkSimulationStateInsteadOfAnimatorGlobalState() {
        TimedArcPetriNetNetwork network = new TimedArcPetriNetNetwork();
        TimedArcPetriNet template = new TimedArcPetriNet("Template");
        TimedTransition transition = new TimedTransition("Transition");
        network.add(template);
        template.add(transition);

        assertTrue(transition.calculateDInterval().isIncluded(BigDecimal.ONE));

        network.setUrgentTransitionEnabled(true);
        TimeInterval urgentInterval = transition.calculateDInterval();
        assertTrue(urgentInterval.isIncluded(BigDecimal.ZERO));
        assertFalse(urgentInterval.isIncluded(new BigDecimal("0.1")));
    }
}
