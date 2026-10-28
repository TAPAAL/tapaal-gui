package dk.aau.cs.model.tapn;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Vector;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import dk.aau.cs.model.CPN.Expressions.NumberOfExpression;
import dk.aau.cs.model.CPN.Expressions.DotConstantExpression;
import dk.aau.cs.verification.TAPNModelComposer;

import org.junit.jupiter.api.Test;

import dk.aau.cs.model.CPN.ColorType;

class NetworkMarkingOwnershipTest {

    @Test
    void sharedPlaceDelayUsesItsOwningNetworkWithoutGuiState() {
        TimedArcPetriNetNetwork network = new TimedArcPetriNetNetwork();
        TimedArcPetriNet template = new TimedArcPetriNet("Template");
        SharedPlace sharedPlace = new SharedPlace("Shared", new TimeInvariant(true, new IntBound(1)));
        // Exercise the ordinary invariant without a wildcard color override.
        sharedPlace.setCtiList(new java.util.ArrayList<>());

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
    @Test
    void replacingMarkingPreservesInactiveComponentTokensAndOwnership() {
        var network = new TimedArcPetriNetNetwork();
        var template = new TimedArcPetriNet("Inactive");
        network.add(template);
        var place = new LocalTimedPlace("P");
        template.add(place);
        place.addToken(new TimedToken(place, new BigDecimal("2"), ColorType.COLORTYPE_DOT.getFirstColor()));
        template.setActive(false);
        var oldLocalMarking = network.marking().getMarkingFor(template);

        network.setMarking(new NetworkMarking());

        assertEquals(new BigDecimal("2"), place.tokens().get(0).age());
        assertNotSame(oldLocalMarking, network.marking().getMarkingFor(template));
    }

    @Test
    void markingXmlUsesExplicitTimeModeWithoutDependingOnGuiLens() throws Exception {
        var network = new TimedArcPetriNetNetwork();
        var template = new TimedArcPetriNet("Template");
        network.add(template);
        var place = new LocalTimedPlace("P");
        template.add(place);
        place.addToken(new TimedToken(place, new BigDecimal("2"), ColorType.COLORTYPE_DOT.getFirstColor()));
        place.setTokenExpression(new NumberOfExpression(1, new Vector<>(List.of(new DotConstantExpression()))));
        var composer = new TAPNModelComposer(false);
        var document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

        var timed = network.marking().toXmlElement(document, composer, true);
        var untimed = network.marking().toXmlElement(document, composer, false);

        assertEquals(1, timed.getElementsByTagName("token").getLength());
        assertEquals("2", ((org.w3c.dom.Element)timed.getElementsByTagName("token").item(0)).getAttribute("age"));
        assertEquals(0, untimed.getElementsByTagName("token").getLength());
        assertEquals(1, untimed.getElementsByTagName("numberof").getLength());
    }

}
