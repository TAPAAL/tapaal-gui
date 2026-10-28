package net.tapaal.gui.petrinet.undo.Colored;

import net.tapaal.gui.petrinet.undo.Command;
import dk.aau.cs.model.CPN.Variable;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;

public class AddVariableCommand implements Command {
    private final Variable variable;
    private final TimedArcPetriNetNetwork network;
    private final Runnable onChange;
    private final int index;

    public AddVariableCommand(Variable variable, TimedArcPetriNetNetwork network, int index, Runnable onChange) {
        this.variable = variable;
        this.network = network;
        this.index = index;
        this.onChange = onChange == null ? () -> {} : onChange;
    }
    @Override
    public void undo() {
        new RemoveVariableFromNetworkCommand(variable, network, index, onChange).redo();
    }

    @Override
    public void redo() {
        new RemoveVariableFromNetworkCommand(variable, network, index, onChange).undo();
    }
}
