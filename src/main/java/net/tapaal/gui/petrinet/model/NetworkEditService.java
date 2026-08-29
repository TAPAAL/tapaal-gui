package net.tapaal.gui.petrinet.model;

import java.util.LinkedHashSet;

import dk.aau.cs.model.tapn.Constant;
import dk.aau.cs.model.tapn.RealConstant;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import net.tapaal.gui.petrinet.undo.AddConstantEditCommand;
import net.tapaal.gui.petrinet.undo.AddRealConstantEditCommand;
import net.tapaal.gui.petrinet.undo.Command;
import net.tapaal.gui.petrinet.undo.RemoveConstantEditCommand;
import net.tapaal.gui.petrinet.undo.RemoveRealConstantEditCommand;
import net.tapaal.gui.petrinet.undo.UpdateConstantEditCommand;
import net.tapaal.gui.petrinet.undo.UpdateRealConstantEditCommand;

/**
 * Application-layer coordination for constant edits. The network owns model
 * mutation; this class is the GUI boundary that adds undo commands around it.
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
}
