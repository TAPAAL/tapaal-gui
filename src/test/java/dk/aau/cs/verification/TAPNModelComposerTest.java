package dk.aau.cs.verification;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.tapn.LocalTimedPlace;
import dk.aau.cs.model.tapn.TimeInterval;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.model.tapn.TimedInputArc;
import dk.aau.cs.model.tapn.TimedOutputArc;
import dk.aau.cs.model.tapn.TimedToken;
import dk.aau.cs.model.tapn.TimedTransition;

class TAPNModelComposerTest {

    @Test
    void composesDomainModelWithoutCreatingGuiObjects() {
        TimedArcPetriNetNetwork network = new TimedArcPetriNetNetwork();
        TimedArcPetriNet template = new TimedArcPetriNet("Template");
        LocalTimedPlace source = new LocalTimedPlace("Source");
        LocalTimedPlace target = new LocalTimedPlace("Target");
        TimedTransition transition = new TimedTransition("Transition");
        TimeInterval interval = new TimeInterval(true, new dk.aau.cs.model.tapn.IntBound(0), new dk.aau.cs.model.tapn.IntBound(5), true);

        template.add(source);
        template.add(target);
        template.add(transition);
        template.add(new TimedInputArc(source, transition, interval));
        template.add(new TimedOutputArc(transition, target));
        source.addToken(new TimedToken(source, new BigDecimal("1.5"), ColorType.COLORTYPE_DOT.getFirstColor()));
        network.add(template);

        var composed = new TAPNModelComposer(true).transformModel(network);

        assertEquals(2, composed.value1().places().size());
        assertEquals(1, composed.value1().transitions().size());
        assertEquals(1, count(composed.value1().inputArcs()));
        assertEquals(1, count(composed.value1().outputArcs()));
        assertEquals(new BigDecimal("1.5"), composed.value1().getPlaceByName("Source").tokens().get(0).age());
        assertEquals("Source", composed.value2().map("Template", "Source"));
        assertEquals("Transition", composed.value2().map("Template", "Transition"));
    }

    private static int count(Iterable<?> values) {
        int count = 0;
        for (Object ignored : values) count++;
        return count;
    }
}
