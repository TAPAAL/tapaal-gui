package net.tapaal.gui.petrinet.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.Variable;
import dk.aau.cs.model.tapn.Constant;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import net.tapaal.gui.petrinet.undo.Command;
import pipe.gui.petrinet.undo.UndoManager;

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

    @Test
    void colorTypeRenameUsesApplicationUndoAndRefreshCallback() {
        TimedArcPetriNetNetwork network = new TimedArcPetriNetNetwork();
        ColorType oldType = new ColorType("old");
        oldType.addColor("red");
        network.add(oldType);
        ColorType newType = new ColorType("new");
        newType.addColor("red");
        AtomicInteger refreshes = new AtomicInteger();
        NetworkEditService edits = new NetworkEditService(network, refreshes::incrementAndGet);
        UndoManager undoManager = new UndoManager(null);

        undoManager.newEdit();
        edits.renameColorType(oldType, newType, undoManager);
        assertEquals(newType, network.getColorTypeByName("new"));
        assertEquals(1, refreshes.get());

        undoManager.undo();
        assertEquals(oldType, network.getColorTypeByName("old"));
        undoManager.redo();
        assertEquals(newType, network.getColorTypeByName("new"));
        assertEquals(3, refreshes.get());
    }

    @Test
    void colorTypeAndVariableRemovalUsesNetworkValidationAndUndo() {
        TimedArcPetriNetNetwork network = new TimedArcPetriNetNetwork();
        ColorType colorType = new ColorType("colors");
        colorType.addColor("red");
        network.add(colorType);
        Variable variable = new Variable("v", colorType);
        network.add(variable);
        AtomicInteger refreshes = new AtomicInteger();
        NetworkEditService edits = new NetworkEditService(network, refreshes::incrementAndGet);
        UndoManager undoManager = new UndoManager(null);

        undoManager.newEdit();
        ArrayList<String> colorMessages = new ArrayList<>();
        assertTrue(edits.removeColorType(colorType, undoManager, colorMessages) == false);
        assertEquals(1, colorMessages.size());
        assertEquals(colorType, network.getColorTypeByName("colors"));

        undoManager.newEdit();
        ArrayList<String> variableMessages = new ArrayList<>();
        assertTrue(edits.removeVariable(variable, undoManager, variableMessages));
        assertTrue(network.variables().isEmpty());
        undoManager.undo();
        assertEquals(variable, network.getVariableByIndex(0));
        undoManager.redo();
        assertTrue(network.variables().isEmpty());
        assertEquals(3, refreshes.get());
    }
}
