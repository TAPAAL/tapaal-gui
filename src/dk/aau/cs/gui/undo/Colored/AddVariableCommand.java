package dk.aau.cs.gui.undo.Colored;

import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.CPN.Variable;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import pipe.gui.editor.ConstantsPane;

public class AddVariableCommand extends Command {
    private final Variable variable;
    private final TimedArcPetriNetNetwork network;
    private final ConstantsPane.VariablesListModel variablesListModel;
    private final int index;

    public AddVariableCommand(Variable variable, TimedArcPetriNetNetwork network, ConstantsPane.VariablesListModel variablesListModel, int index) {
        this.variable = variable;
        this.network = network;
        this.variablesListModel = variablesListModel;
        this.index = index;
    }
    @Override
    public void undo() {
        new RemoveVariableFromNetworkCommand(variable, network, variablesListModel, index).redo();
    }

    @Override
    public void redo() {
        new RemoveVariableFromNetworkCommand(variable, network, variablesListModel, index).undo();
    }
}
