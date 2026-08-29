package dk.aau.cs.model.tapn;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import dk.aau.cs.model.CPN.ColorType;

class NetworkMarkingOwnershipTest {

    @Test
    void sharedPlaceDelayUsesItsOwningNetworkWithoutGuiState() {
        TimedArcPetriNetNetwork network = new TimedArcPetriNetNetwork();
        TimedArcPetriNet template = new TimedArcPetriNet("Template");
        SharedPlace sharedPlace = new SharedPlace("Shared", new TimeInvariant(true, new IntBound(1)));

        network.add(template);
        network.add(sharedPlace);
        template.add(sharedPlace, true);
        sharedPlace.addToken(new TimedToken(sharedPlace, BigDecimal.ZERO, ColorType.COLORTYPE_DOT.getFirstColor()));

        assertTrue(network.marking().isDelayPossible(BigDecimal.ONE));
        assertFalse(network.marking().isDelayPossible(new BigDecimal("1.1")));
        assertTrue(network.marking().getBlockingPlaces(new BigDecimal("1.1")).contains(sharedPlace));
    }

    @Test
    void sharedPlaceReportsComponentsFromItsOwningNetwork() {
        TimedArcPetriNetNetwork network = new TimedArcPetriNetNetwork();
        TimedArcPetriNet first = new TimedArcPetriNet("First");
        TimedArcPetriNet second = new TimedArcPetriNet("Second");
        SharedPlace sharedPlace = new SharedPlace("Shared");

        network.add(first);
        network.add(second);
        network.add(sharedPlace);
        first.add(sharedPlace, true);

        assertTrue(sharedPlace.getComponentsUsingThisPlace().contains("First"));
        assertFalse(sharedPlace.getComponentsUsingThisPlace().contains("Second"));
    }
}
