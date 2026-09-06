package net.tapaal.gui.petrinet.undo.Colored;

import net.tapaal.gui.petrinet.undo.Command;
import dk.aau.cs.model.CPN.Variable;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;

public class RemoveVariableFromNetworkCommand implements Command {

    private final Variable variable;
    private final TimedArcPetriNetNetwork network;
    private final Runnable onChange;
    private final int index;

    public RemoveVariableFromNetworkCommand(Variable variable, TimedArcPetriNetNetwork network, int index, Runnable onChange) {
        this.variable = variable;
        this.network = network;
        this.index = index;
        this.onChange = onChange == null ? () -> {} : onChange;
    }

    @Override
    public void undo() {
        network.variables().add(index, variable);
        onChange.run();
    }

    @Override
    public void redo() {
        network.variables().remove(variable);
        onChange.run();
    }
}
