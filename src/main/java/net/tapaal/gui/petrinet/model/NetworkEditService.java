package net.tapaal.gui.petrinet.model;

import java.util.LinkedHashSet;
import java.util.List;

import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.ProductType;
import dk.aau.cs.model.CPN.Variable;
import dk.aau.cs.model.tapn.Constant;
import dk.aau.cs.model.tapn.RealConstant;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import net.tapaal.gui.petrinet.undo.AddConstantEditCommand;
import net.tapaal.gui.petrinet.undo.AddRealConstantEditCommand;
import net.tapaal.gui.petrinet.undo.Command;
import net.tapaal.gui.petrinet.undo.Colored.AddColorTypeCommand;
import net.tapaal.gui.petrinet.undo.Colored.AddVariableCommand;
import net.tapaal.gui.petrinet.undo.Colored.RemoveColorTypeFromNetworkCommand;
import net.tapaal.gui.petrinet.undo.Colored.RemoveVariableFromNetworkCommand;
import net.tapaal.gui.petrinet.undo.Colored.UpdateColorTypeCommand;
import net.tapaal.gui.petrinet.undo.Colored.UpdatePTColorTypeCommand;
import net.tapaal.gui.petrinet.undo.RemoveConstantEditCommand;
import net.tapaal.gui.petrinet.undo.RemoveRealConstantEditCommand;
import net.tapaal.gui.petrinet.undo.UpdateConstantEditCommand;
import net.tapaal.gui.petrinet.undo.UpdateRealConstantEditCommand;
import net.tapaal.gui.petrinet.undo.Colored.UpdateVariableCommand;
import pipe.gui.petrinet.undo.UndoManager;

/**
 * Application-layer coordination for model edits. The network owns model
 * mutation and validation; this class is the GUI boundary that adds undo
 * commands and view-refresh callbacks around it.
 */
public final class NetworkEditService {
    private final TimedArcPetriNetNetwork network;
    private final Runnable onChange;

    public NetworkEditService(TimedArcPetriNetNetwork network) {
		this(network, () -> {});
	}

	public NetworkEditService(TimedArcPetriNetNetwork network, Runnable onChange) {
        if (network == null) throw new IllegalArgumentException("network cannot be null");
        this.network = network;
		this.onChange = onChange == null ? () -> {} : onChange;
    }

    public Command addConstant(String name, LinkedHashSet<Integer> values) {
        Constant constant = network.addConstant(name, values);
        return constant == null ? null : new AddConstantEditCommand(constant, network.getConstantStore(), onChange);
    }

    public Command removeConstant(String name) {
        Constant constant = network.removeConstant(name);
        return constant == null ? null : new RemoveConstantEditCommand(constant, network.getConstantStore(), onChange);
    }

    public Command updateConstant(String oldName, Constant replacement) {
        Constant old = network.getConstant(oldName);
        Constant updated = network.updateConstant(oldName, replacement);
        return updated == null ? null : new UpdateConstantEditCommand(old, updated, network.getConstantStore(), network, onChange);
    }

    public Command addRealConstant(String name, LinkedHashSet<Double> values) {
        RealConstant constant = network.addRealConstant(name, values);
        return constant == null ? null : new AddRealConstantEditCommand(constant, network.getConstantStore(), onChange);
    }

    public Command removeRealConstant(String name) {
        RealConstant constant = network.removeRealConstant(name);
        return constant == null ? null : new RemoveRealConstantEditCommand(constant, network.getConstantStore(), onChange);
    }

    public Command updateRealConstant(String oldName, RealConstant replacement) {
        RealConstant old = network.getRealConstant(oldName);
        RealConstant updated = network.updateRealConstant(oldName, replacement);
        return updated == null ? null : new UpdateRealConstantEditCommand(old, updated, network.getConstantStore(), network, onChange);
    }

    public void renameColorType(ColorType oldColorType, ColorType newColorType, UndoManager undoManager) {
        Integer index = network.getColorTypeIndex(oldColorType.getName());
        Command command = new UpdateColorTypeCommand(network, oldColorType, newColorType, index, onChange);
        command.redo();
        undoManager.addEdit(command);
        updateProductTypes(oldColorType, newColorType, undoManager);
    }

    public void updateColorType(ColorType oldColorType, ColorType newColorType, UndoManager undoManager) {
        undoManager.newEdit();
        renameColorType(oldColorType, newColorType, undoManager);
    }

    private void updateProductTypes(ColorType oldColorType, ColorType newColorType, UndoManager undoManager) {
        for (ColorType colorType : network.colorTypes()) {
            if (colorType instanceof ProductType) {
                Command command = new UpdatePTColorTypeCommand(oldColorType, newColorType, (ProductType) colorType);
                command.redo();
                undoManager.addEdit(command);
            }
        }
    }

    public boolean removeColorType(ColorType colorType, UndoManager undoManager, List<String> messages) {
        Integer index = network.getColorTypeIndex(colorType.getName());
        if (!network.canColorTypeBeRemoved(colorType, messages)) {
            return false;
        }

        Command command = new RemoveColorTypeFromNetworkCommand(colorType, network, index, onChange);
        command.redo();
        undoManager.addEdit(command);
        return true;
    }

    public boolean removeVariable(Variable variable, UndoManager undoManager, List<String> messages) {
        if (!network.canVariableBeRemoved(variable, messages)) {
            return false;
        }

        Integer index = network.getVariableIndex(variable.getName());
        Command command = new RemoveVariableFromNetworkCommand(variable, network, index, onChange);
        command.redo();
        undoManager.addEdit(command);
        return true;
    }

    public Command addColorType(ColorType colorType, int index) {
        return new AddColorTypeCommand(colorType, network, index, onChange);
    }

    public Command addVariable(Variable variable, int index) {
        return new AddVariableCommand(variable, network, index, onChange);
    }

    public Command updateVariable(Variable variable, String newName, ColorType colorType) {
        return new UpdateVariableCommand(variable, newName, colorType, onChange);
    }
}
