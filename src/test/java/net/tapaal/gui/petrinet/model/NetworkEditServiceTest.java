package net.tapaal.gui.petrinet.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.LinkedHashSet;

import org.junit.jupiter.api.Test;

import dk.aau.cs.model.tapn.Constant;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import net.tapaal.gui.petrinet.undo.Command;

class NetworkEditServiceTest {

    @Test
    void constantCommandsCanUndoAndRedoWithoutASelectedGuiTab() {
        TimedArcPetriNetNetwork network = new TimedArcPetriNetNetwork();
        NetworkEditService edits = new NetworkEditService(network);
        LinkedHashSet<Integer> values = new LinkedHashSet<>();
        values.add(2);

        Command add = edits.addConstant("k", values);
        assertNotNull(add);
        assertEquals(2, network.getConstant("k").value());

        add.undo();
        assertEquals(null, network.getConstant("k"));
        add.redo();
        assertEquals(2, network.getConstant("k").value());
    }

    @Test
    void updatingAConstantPreservesUndoableModelReferenceChanges() {
        TimedArcPetriNetNetwork network = new TimedArcPetriNetNetwork();
        NetworkEditService edits = new NetworkEditService(network);
        edits.addConstant("k", new LinkedHashSet<>(java.util.List.of(2)));

        Command update = edits.updateConstant("k", new Constant("k", 3));
        assertNotNull(update);
        assertEquals(3, network.getConstant("k").value());

        update.undo();
        assertEquals(2, network.getConstant("k").value());
        update.redo();
        assertEquals(3, network.getConstant("k").value());
    }
}
