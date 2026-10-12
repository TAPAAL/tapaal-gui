package net.tapaal.gui.petrinet;

import dk.aau.cs.model.tapn.IntBound;
import dk.aau.cs.model.tapn.LocalTimedPlace;
import dk.aau.cs.model.tapn.TimeInterval;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.model.tapn.TransportArc;
import net.tapaal.gui.petrinet.undo.AddTransportArcCommand;
import net.tapaal.gui.petrinet.undo.DeleteTransportArcCommand;
import org.junit.jupiter.api.Test;
import pipe.gui.petrinet.dataLayer.DataLayer;
import pipe.gui.petrinet.graphicElements.tapn.TimedPlaceComponent;
import pipe.gui.petrinet.graphicElements.tapn.TimedTransitionComponent;
import pipe.gui.petrinet.graphicElements.tapn.TimedTransportArcComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TransportArcIndexTest {

    @Test
    public void indexIsHiddenForASinglePairAndShownWhenAnotherPairIsAdded() {
        TimedArcPetriNet net = new TimedArcPetriNet("net");
        LocalTimedPlace source = new LocalTimedPlace("P0");
        LocalTimedPlace destination = new LocalTimedPlace("P1");
        LocalTimedPlace otherSource = new LocalTimedPlace("P2");
        LocalTimedPlace otherDestination = new LocalTimedPlace("P3");
        TimedTransition transition = new TimedTransition("T0");
        net.add(source);
        net.add(destination);
        net.add(otherSource);
        net.add(otherDestination);
        net.add(transition);

        TAPNLens lens = new TAPNLens(true, false, false, false);
        DataLayer gui = new DataLayer();
        TimedPlaceComponent sourceComponent = new TimedPlaceComponent(0, 0, source, lens);
        TimedPlaceComponent destinationComponent = new TimedPlaceComponent(100, 0, destination, lens);
        TimedPlaceComponent otherSourceComponent = new TimedPlaceComponent(0, 100, otherSource, lens);
        TimedPlaceComponent otherDestinationComponent = new TimedPlaceComponent(100, 100, otherDestination, lens);
        TimedTransitionComponent transitionComponent = new TimedTransitionComponent(50, 50, transition, lens);

        TransportArc first = new TransportArc(source, transition, destination, TimeInterval.ZERO_INF);
        TimedTransportArcComponent firstIn = new TimedTransportArcComponent(sourceComponent, transitionComponent, first, 1, lens);
        TimedTransportArcComponent firstOut = new TimedTransportArcComponent(transitionComponent, destinationComponent, first, 1, lens);
        firstIn.setConnectedTo(firstOut);
        firstOut.setConnectedTo(firstIn);
        new AddTransportArcCommand(firstOut, first, net, gui).redo();

        assertFalse(firstIn.getNameLabel().getText().contains(":"));
        assertFalse(firstOut.getNameLabel().getText().contains(":"));
        assertEquals(1, firstIn.getGroupNr());

        TimeInterval restricted = new TimeInterval(true, new IntBound(0), new IntBound(5), true);
        TransportArc second = new TransportArc(otherSource, transition, otherDestination, restricted);
        TimedTransportArcComponent secondIn = new TimedTransportArcComponent(otherSourceComponent, transitionComponent, second, 2, lens);
        TimedTransportArcComponent secondOut = new TimedTransportArcComponent(transitionComponent, otherDestinationComponent, second, 2, lens);
        secondIn.setConnectedTo(secondOut);
        secondOut.setConnectedTo(secondIn);
        AddTransportArcCommand addSecond = new AddTransportArcCommand(secondOut, second, net, gui);
        addSecond.redo();

        assertTrue(firstIn.getNameLabel().getText().contains(": 1"));
        assertEquals("  : 1", firstOut.getNameLabel().getText());
        assertEquals(" [0,5] : 2", secondIn.getNameLabel().getText());
        assertEquals("  : 2", secondOut.getNameLabel().getText());

        addSecond.undo();

        assertFalse(firstIn.getNameLabel().getText().contains(":"));
        assertFalse(firstOut.getNameLabel().getText().contains(":"));
    }

    @Test
    public void deletingDownToOnePairHidesTheRemainingIndexWithoutRenumbering() {
        TimedArcPetriNet net = new TimedArcPetriNet("net");
        LocalTimedPlace source = new LocalTimedPlace("P0");
        LocalTimedPlace destination = new LocalTimedPlace("P1");
        LocalTimedPlace otherSource = new LocalTimedPlace("P2");
        LocalTimedPlace otherDestination = new LocalTimedPlace("P3");
        TimedTransition transition = new TimedTransition("T0");
        net.add(source);
        net.add(destination);
        net.add(otherSource);
        net.add(otherDestination);
        net.add(transition);

        TAPNLens lens = new TAPNLens(true, false, false, false);
        DataLayer gui = new DataLayer();
        TimedTransitionComponent transitionComponent = new TimedTransitionComponent(50, 50, transition, lens);

        TimeInterval firstInterval = new TimeInterval(true, new IntBound(1), new IntBound(2), false);
        TransportArc first = new TransportArc(source, transition, destination, firstInterval);
        TimedTransportArcComponent firstIn = new TimedTransportArcComponent(
            new TimedPlaceComponent(0, 0, source, lens), transitionComponent, first, 1, lens);
        TimedTransportArcComponent firstOut = new TimedTransportArcComponent(
            transitionComponent, new TimedPlaceComponent(100, 0, destination, lens), first, 1, lens);
        firstIn.setConnectedTo(firstOut);
        firstOut.setConnectedTo(firstIn);
        new AddTransportArcCommand(firstOut, first, net, gui).redo();

        TimeInterval secondInterval = new TimeInterval(true, new IntBound(3), new IntBound(4), true);
        TransportArc second = new TransportArc(otherSource, transition, otherDestination, secondInterval);
        TimedTransportArcComponent secondIn = new TimedTransportArcComponent(
            new TimedPlaceComponent(0, 100, otherSource, lens), transitionComponent, second, 2, lens);
        TimedTransportArcComponent secondOut = new TimedTransportArcComponent(
            transitionComponent, new TimedPlaceComponent(100, 100, otherDestination, lens), second, 2, lens);
        secondIn.setConnectedTo(secondOut);
        secondOut.setConnectedTo(secondIn);
        new AddTransportArcCommand(secondOut, second, net, gui).redo();

        DeleteTransportArcCommand deleteFirst = new DeleteTransportArcCommand(firstIn, first, net, gui);
        deleteFirst.redo();

        assertEquals(" [3,4]", secondIn.getNameLabel().getText());
        assertEquals(" ", secondOut.getNameLabel().getText());
        assertEquals(2, secondIn.getGroupNr());
        assertEquals(2, secondOut.getGroupNr());

        deleteFirst.undo();

        assertEquals(" [1,2) : 1", firstIn.getNameLabel().getText());
        assertEquals("  : 1", firstOut.getNameLabel().getText());
        assertEquals(" [3,4] : 2", secondIn.getNameLabel().getText());
        assertEquals("  : 2", secondOut.getNameLabel().getText());
    }
}
