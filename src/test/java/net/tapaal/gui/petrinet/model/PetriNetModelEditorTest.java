package net.tapaal.gui.petrinet.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import dk.aau.cs.model.tapn.LocalTimedPlace;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedInputArc;
import dk.aau.cs.model.tapn.TimedOutputArc;
import dk.aau.cs.model.tapn.TimedTransition;

class PetriNetModelEditorTest {

    @Test
    void createsAndMutatesDomainObjectsWithoutAView() {
        PetriNetModelEditor editor = new PetriNetModelEditor();
        TimedArcPetriNet model = new TimedArcPetriNet("Template");
        LocalTimedPlace source = editor.createPlace("Source");
        LocalTimedPlace target = editor.createPlace("Target");
        TimedTransition transition = editor.createTransition("Transition", true, false);

        editor.addPlace(model, source);
        editor.addPlace(model, target);
        editor.addTransition(model, transition);
        TimedInputArc input = editor.createInputArc(source, transition);
        TimedOutputArc output = editor.createOutputArc(transition, target);
        editor.addInputArc(model, input);
        editor.addOutputArc(model, output);

        assertEquals(2, model.places().size());
        assertEquals(1, model.transitions().size());
        assertEquals(1, count(model.inputArcs()));
        assertEquals(1, count(model.outputArcs()));
        assertTrue(transition.isUrgent());

        editor.removeOutputArc(output);
        editor.removeInputArc(input);
        assertEquals(0, count(model.inputArcs()));
        assertEquals(0, count(model.outputArcs()));
    }

    private static int count(Iterable<?> values) {
        int count = 0;
        for (Object ignored : values) count++;
        return count;
    }
}
