package net.tapaal.gui.petrinet.undo.Colored;

import net.tapaal.gui.petrinet.undo.Command;
import dk.aau.cs.model.CPN.Variable;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import net.tapaal.gui.petrinet.editor.ConstantsPane;

public class RemoveVariableFromNetworkCommand extends Command {

    private final Variable variable;
    private final TimedArcPetriNetNetwork network;
    private final ConstantsPane.VariablesListModel variablesListModel;
    private final int index;

    public RemoveVariableFromNetworkCommand(Variable variable, TimedArcPetriNetNetwork network, ConstantsPane.VariablesListModel variablesListModel, int index) {
        this.variable = variable;
        this.network = network;
        this.variablesListModel = variablesListModel;
        this.index = index;
    }

    @Override
    public void undo() {
        network.variables().add(index, variable);
        variablesListModel.updateName();
    }

    @Override
    public void redo() {
        network.variables().remove(variable);
        variablesListModel.updateName();
    }
}
